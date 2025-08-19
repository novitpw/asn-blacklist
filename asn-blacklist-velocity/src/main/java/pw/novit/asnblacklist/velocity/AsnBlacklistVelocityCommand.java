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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.jetbrains.annotations.NotNull;
import pw.novit.asnblacklist.config.FileConfigValues;
import pw.novit.asnblacklist.registry.AsnBlacklistRegistry;
import pw.novit.asnblacklist.translation.TranslationRegistrar;

import java.util.Comparator;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

/**
 * @author _Novit_ (novitpw)
 */
@UtilityClass
final class AsnBlacklistVelocityCommand {

    public static @NotNull BrigadierCommand create(
            @NotNull FileConfigValues fileConfigValues,
            @NotNull AsnBlacklistRegistry asnBlacklistRegistry
    ) {
        return new BrigadierCommand(LiteralArgumentBuilder.<CommandSource>literal("asnblacklist")
                .requires(src -> src.hasPermission("asnblacklist.command"))
                .executes(ctx -> {
                    ctx.getSource().sendMessage(translatable("asnblacklist.command.usage"));
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("add")
                        .executes(ctx -> {
                            ctx.getSource().sendMessage(translatable("asnblacklist.command.add.usage"));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, Long>argument("asn", LongArgumentType.longArg())
                                .executes(ctx -> {
                                    val asn = ctx.getArgument("asn", Long.class);

                                    if (!asnBlacklistRegistry.add(asn)) {
                                        ctx.getSource().sendMessage(translatable("asnblacklist.command.add.failed",
                                                text(asn)));
                                        return 0;
                                    }

                                    ctx.getSource().sendMessage(translatable("asnblacklist.command.add.success",
                                            text(asn)));
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .build())
                .then(LiteralArgumentBuilder.<CommandSource>literal("remove")
                        .executes(ctx -> {
                            ctx.getSource().sendMessage(translatable("asnblacklist.command.remove.usage"));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource, Long>argument("asn", LongArgumentType.longArg())
                                .executes(ctx -> {
                                    val asn = ctx.getArgument("asn", Long.class);

                                    if (!asnBlacklistRegistry.remove(asn)) {
                                        ctx.getSource().sendMessage(translatable("asnblacklist.command.remove.failed",
                                                text(asn)));
                                        return 0;
                                    }

                                    ctx.getSource().sendMessage(translatable("asnblacklist.command.remove.success",
                                            text(asn)));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .suggests((ctx, builder) -> {
                                    asnBlacklistRegistry.getBlacklistedAsn().stream()
                                            .map(String::valueOf)
                                            .forEach(builder::suggest);

                                    return builder.buildFuture();
                                })
                                .build())
                        .build())
                .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                        .executes(ctx -> {
                            val blacklistedAsn = asnBlacklistRegistry.getBlacklistedAsn();
                            if (blacklistedAsn.isEmpty()) {
                                ctx.getSource().sendMessage(translatable("asnblacklist.command.list.empty"));
                                return 0;
                            }

                            ctx.getSource().sendMessage(translatable("asnblacklist.command.list.entries",
                                    join(JoinConfiguration.commas(true), blacklistedAsn.stream()
                                            .sorted(Comparator.comparingLong(v -> v))
                                            .map(asn -> translatable("asnblacklist.command.list.entry",
                                                    Argument.tagResolver(Placeholder.parsed("asn", String.valueOf(asn)))))
                                            .toList())));

                            return Command.SINGLE_SUCCESS;
                        })
                        .build())
                .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                        .executes(ctx -> {
                            fileConfigValues.reload();
                            asnBlacklistRegistry.reload();
                            TranslationRegistrar.registerGlobal();

                            ctx.getSource().sendMessage(translatable("asnblacklist.command.reload"));
                            return Command.SINGLE_SUCCESS;
                        })
                        .build())
        );
    }

}
