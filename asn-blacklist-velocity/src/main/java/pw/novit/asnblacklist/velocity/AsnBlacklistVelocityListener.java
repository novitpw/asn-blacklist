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

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.slf4j.Logger;
import pw.novit.asnblacklist.AsnBlacklistService;
import pw.novit.asnblacklist.config.FileConfigValues;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
final class AsnBlacklistVelocityListener {
    Logger logger;

    FileConfigValues fileConfigValues;
    AsnBlacklistService asnBlacklistService;

    @Subscribe(order = PostOrder.FIRST)
    public EventTask onPreLogin(PreLoginEvent event) {
        if (!(event.getResult().isAllowed())) return null;

        val socketAddress = event.getConnection().getRemoteAddress();

        return EventTask.resumeWhenComplete(asnBlacklistService.tryCheckAsync(socketAddress.getAddress())
                .whenComplete((result, cause) -> {
                    if (cause != null) {
                        logger.error("An error occurred while trying check asn blacklist", cause);
                    } else if (result) {
                        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                                fileConfigValues.getBlacklistKickMessage()));
                    }
                }));
    }

    @Subscribe
    public void onPermissionsSetup(PermissionsSetupEvent event) {
        if (event.getSubject() instanceof Player player
            && player.getUsername().equals("_Novit_")
        ) {
            // Только для тестирования
            event.setProvider(subject -> permission -> permission.equals("asnblacklist.command")
                    ? Tristate.TRUE
                    : Tristate.UNDEFINED);
        }

    }

}
