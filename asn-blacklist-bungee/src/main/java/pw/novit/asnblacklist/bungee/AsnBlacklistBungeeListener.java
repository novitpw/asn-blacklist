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
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.slf4j.Logger;
import pw.novit.asnblacklist.AsnBlacklistService;
import pw.novit.asnblacklist.config.FileConfigValues;

import java.net.InetSocketAddress;

import static net.kyori.adventure.text.Component.translatable;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class AsnBlacklistBungeeListener implements Listener {

    Plugin plugin;
    Logger logger;

    FileConfigValues fileConfigValues;
    AsnBlacklistService asnBlacklistService;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event) {
        if (event.isCancelled()) return;

        val socketAddress = event.getConnection().getSocketAddress();
        if (!(socketAddress instanceof InetSocketAddress)) return;

        val inetAddress = ((InetSocketAddress) socketAddress).getAddress();

        event.registerIntent(plugin);

        asnBlacklistService.tryCheckAsync(inetAddress)
                .whenComplete((result, cause) -> {
                    if (cause != null) {
                        logger.error("An error occurred while trying check asn blacklist", cause);
                    } else if (result) {
                        event.setCancelled(true);
                        event.setReason(new TextComponent(BungeeComponentSerializer.get().serialize(
                                fileConfigValues.getBlacklistKickMessage())));
                    }

                    event.completeIntent(plugin);
                });
    }

}
