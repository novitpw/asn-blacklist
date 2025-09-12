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

package pw.novit.asnblacklist.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import pw.novit.asnblacklist.AsnBlacklistDisconnectObserver;
import pw.novit.asnblacklist.AsnBlacklistService;
import pw.novit.asnlookup.AsnLookupExecutors;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.translatable;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class AsnBlacklistVelocityDisconnectObserver implements AsnBlacklistDisconnectObserver {

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

        for (val player : proxyServer.getAllPlayers()) {
            if (asnBlacklistService.tryCheck(player.getRemoteAddress().getAddress())) {
                player.disconnect(translatable("asnblacklist.message.kick"));
                counter++;
            }
        }

        return counter;
    }

}
