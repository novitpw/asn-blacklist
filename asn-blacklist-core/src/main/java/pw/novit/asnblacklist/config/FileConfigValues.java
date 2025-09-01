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
import org.jetbrains.annotations.NotNull;
import pw.novit.asnblacklist.config.mapper.AsnMapper;
import w.config.FileConfig;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileConfigValues {

    public static @NotNull FileConfigValues create(@NotNull FileConfig fileConfig) {
        val fileConfigValues = new FileConfigValues(fileConfig);
        fileConfigValues.reloadInternal();

        return fileConfigValues;
    }

    private static final String MAXMIND_KEY = "maxmind-database.api-key";
    private static final String MAXMIND_DATABASE_FILE = "maxmind-database.file";
    private static final String MAXMIND_DATABASE_TTL = "maxmind-database.ttl";

    private static final String ASN_BLACKLIST = "asn-blacklist";
    private static final String CACHE_TTL = "cache-ttl";

    FileConfig fileConfig;

    @Getter
    @NonFinal
    String maxmindDatabaseKey;

    @Getter
    @NonFinal
    String maxmindDatabaseFile;

    @Getter
    @NonFinal
    Duration maxmindDatabaseTTL;

    @Getter
    @NonFinal
    Set<Long> asnBlacklist;

    @Getter
    @NonFinal
    Duration cacheTTL;

    public void reload() {
        fileConfig.reload();
        reloadInternal();
    }

    private void reloadInternal() {
        maxmindDatabaseKey = fileConfig.walk(MAXMIND_KEY).asString();
        maxmindDatabaseFile = fileConfig.walk(MAXMIND_DATABASE_FILE).asString();
        maxmindDatabaseTTL = Duration.parse(fileConfig.walk(MAXMIND_DATABASE_TTL).asString());

        asnBlacklist = fileConfig.getList(ASN_BLACKLIST, AsnMapper.create()).stream()
                .collect(Collectors.toUnmodifiableSet());

        cacheTTL = Duration.parse(fileConfig.getString(CACHE_TTL));
    }

    public void setAsnBlacklist(@NotNull Set<Long> asnBlacklist) {
        this.asnBlacklist = Set.copyOf(asnBlacklist);

        fileConfig.set(ASN_BLACKLIST, asnBlacklist);
        fileConfig.save();
    }

}
