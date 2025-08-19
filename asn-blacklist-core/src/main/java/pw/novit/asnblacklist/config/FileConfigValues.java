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

package pw.novit.asnblacklist.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import w.config.FileConfig;

import java.util.Set;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileConfigValues {

    public static @NotNull FileConfigValues create(@NotNull FileConfig fileConfig) {
        val fileConfigValues = new FileConfigValues(fileConfig);
        fileConfigValues.reload();

        return fileConfigValues;
    }

    private static final String MAXMIND_KEY = "maxmind-key";
    private static final String MAXMIND_DATABASE_FILE = "maxmind-database-file";
    private static final String ASN_BLACKLIST = "asn-blacklist";
    private static final String KICK_MESSAGE = "kick-message";

    FileConfig fileConfig;

    @Getter
    @NonFinal
    String maxmindKey;

    @Getter
    @NonFinal
    String maxmindDatabaseFile;

    @Getter
    @NonFinal
    Set<Long> asnBlacklist;

    @Getter
    @NonFinal
    Component blacklistKickMessage;

    public void reload() {
        maxmindKey = fileConfig.getString(MAXMIND_KEY);
        maxmindDatabaseFile = fileConfig.getString(MAXMIND_DATABASE_FILE);

        asnBlacklist = fileConfig.getLongList(ASN_BLACKLIST).stream()
                .collect(Collectors.toUnmodifiableSet());

        blacklistKickMessage = miniMessage().deserialize(
                fileConfig.getString(KICK_MESSAGE));
    }

    public void setAsnBlacklist(@NotNull Set<Long> asnBlacklist) {
        this.asnBlacklist = asnBlacklist;

        fileConfig.set(ASN_BLACKLIST, asnBlacklist);
        fileConfig.save();
    }

}
