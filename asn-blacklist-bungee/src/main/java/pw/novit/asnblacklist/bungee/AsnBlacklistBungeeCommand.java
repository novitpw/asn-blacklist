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
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pw.novit.asnblacklist.config.FileConfigValues;
import pw.novit.asnblacklist.registry.AsnBlacklistRegistry;
import pw.novit.asnblacklist.translation.TranslationRegistrar;
import pw.novit.asnblacklist.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
final class AsnBlacklistBungeeCommand extends Command implements TabExecutor {
    BungeeAudiences bungeeAudiences;
    FileConfigValues fileConfigValues;
    AsnBlacklistRegistry asnBlacklistRegistry;

    public AsnBlacklistBungeeCommand(
            @NotNull BungeeAudiences bungeeAudiences,
            @NotNull FileConfigValues fileConfigValues,
            @NotNull AsnBlacklistRegistry asnBlacklistRegistry
    ) {
        super("asnblacklist", "asnblacklist.command", "asnbl");

        this.bungeeAudiences = bungeeAudiences;
        this.fileConfigValues = fileConfigValues;
        this.asnBlacklistRegistry = asnBlacklistRegistry;
    }

    @Override
    public void execute(CommandSender sender, String @NotNull [] args) {
        val length = args.length;
        if (length == 0) {
            sendMessage(sender, translatable("asnblacklist.command.usage"));
        } else {
            switch (args[0]) {
                case "add" -> {
                    if (length != 2) {
                        sendMessage(sender, translatable("asnblacklist.command.add.usage"));
                        return;
                    }

                    val asn = tryParseAsn(args[1]);
                    if (asn == null) {
                        sendMessage(sender, translatable("asnblacklist.command.invalid_asn_argument"));
                        return;
                    }

                    if (!asnBlacklistRegistry.add(asn)) {
                        sendMessage(sender, translatable("asnblacklist.command.add.failed", text(asn)));
                        return;
                    }

                    sendMessage(sender, translatable("asnblacklist.command.add.success", text(asn)));
                }
                case "remove" -> {
                    if (length != 2) {
                        sendMessage(sender, translatable("asnblacklist.command.remove.usage"));
                        return;
                    }

                    val asn = tryParseAsn(args[1]);
                    if (asn == null) {
                        sendMessage(sender, translatable("asnblacklist.command.invalid_asn_argument"));
                        return;
                    }

                    if (!asnBlacklistRegistry.remove(asn)) {
                        sendMessage(sender, translatable("asnblacklist.command.remove.failed", text(asn)));
                        return;
                    }

                    sendMessage(sender, translatable("asnblacklist.command.remove.success", text(asn)));
                }
                case "list" -> {
                    val blacklistedAsn = asnBlacklistRegistry.getBlacklistedAsn();
                    if (blacklistedAsn.isEmpty()) {
                        sendMessage(sender, translatable("asnblacklist.command.list.empty"));
                        return;
                    }

                    sendMessage(sender, translatable("asnblacklist.command.list.entries",
                            join(JoinConfiguration.commas(true), blacklistedAsn.stream()
                                    .sorted(Comparator.comparingLong(v -> v))
                                    .map(asn -> translatable("asnblacklist.command.list.entry",
                                            Argument.tagResolver(Placeholder.parsed("asn", String.valueOf(asn)))))
                                    .toList())));
                }
                case "reload" -> {
                    fileConfigValues.reload();
                    asnBlacklistRegistry.reload();
                    TranslationRegistrar.registerGlobal();

                    sendMessage(sender, translatable("asnblacklist.command.reload"));
                }
                default -> sendMessage(sender, translatable("asnblacklist.command.usage"));
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String @NotNull [] args) {
        val length = args.length;
        if (length == 0) return Collections.emptySet();

        if (length == 1) {
            return List.of("add", "remove", "list", "reload");
        } else if (length == 2 && args[0].equalsIgnoreCase("remove")) {
            val argument = args[1];

            return asnBlacklistRegistry.getBlacklistedAsn().stream()
                    .map(String::valueOf)
                    .filter(value -> StringUtils.startsWithIgnoreCase(value, argument))
                    .toList();
        }

        return Collections.emptySet();
    }

    private @Nullable Long tryParseAsn(String argument) {
        try {
            return Long.parseLong(argument);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void sendMessage(CommandSender sender, Component message) {
        bungeeAudiences.sender(sender).sendMessage(message);
    }
}
