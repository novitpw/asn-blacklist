/*
 * Copyright (C) 2025 _Novit_ (github.com/novitpw)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pw.novit.asnblacklist.bungee;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import pw.novit.asnblacklist.AsnBlacklistDisconnectObserver;
import pw.novit.asnblacklist.AsnBlacklistService;
import pw.novit.asnlookup.AsnLookupExecutors;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class AsnBlacklistBungeeDisconnectObserver implements AsnBlacklistDisconnectObserver {

    Logger logger;
    ProxyServer proxyServer;
    AsnBlacklistService asnBlacklistService;

    @Override
    public @NotNull CompletableFuture<@NotNull Integer> executeAsync() {
        return CompletableFuture.supplyAsync(this::disconnectBanned, AsnLookupExecutors.polledDefault())
                .exceptionally(cause -> {
                    logger.error("Error while executing disconnect", cause);
                    return 0;
                });
    }

    private int disconnectBanned() {
        int counter = 0;

        for (val player : proxyServer.getPlayers()) {
            val socketAddress = player.getPendingConnection().getSocketAddress();
            val inetAddress = ((InetSocketAddress) socketAddress).getAddress();

            if (asnBlacklistService.tryCheck(inetAddress)) {
                player.disconnect(new TextComponent(BungeeComponentSerializer.get().serialize(
                        GlobalTranslator.render(Component.translatable("asnblacklist.message.kick"),
                                player.getLocale()))));
                counter++;
            }
        }

        return counter;
    }

}
