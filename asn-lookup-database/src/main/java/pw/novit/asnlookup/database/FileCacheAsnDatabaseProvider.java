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

package pw.novit.asnlookup.database;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileCacheAsnDatabaseProvider implements AsnDatabaseProvider {

    public static @NotNull AsnDatabaseProvider cache(
            @NotNull AsnDatabaseProvider delegate,
            @NotNull Path destination,
            @Nullable Duration ttl
    ) {
        return new FileCacheAsnDatabaseProvider(delegate, destination, ttl);
    }

    public static @NotNull AsnDatabaseProvider cache(
            @NotNull AsnDatabaseProvider delegate,
            @NotNull Path destination
    ) {
        return cache(delegate, destination, null);
    }

    AsnDatabaseProvider delegate;

    Path destination;
    Duration ttl;

    Lock lock = new ReentrantLock();

    private InputStream openStream0() throws IOException {
        if (!Files.exists(destination) || !checkTTL()) {
            try (val is = delegate.openStream();
                 val os = Files.newOutputStream(destination)) {
                is.transferTo(os);
            }
        }

        return Files.newInputStream(destination);
    }

    @Override
    public @NotNull InputStream openStream() throws IOException {
        lock.lock();

        try {
            return openStream0();
        } finally {
            lock.unlock();
        }
    }

    private boolean checkTTL() throws IOException {
        if (ttl == null) return true;

        val timestamp = Files.getLastModifiedTime(destination)
                .toInstant();

        return Duration.between(timestamp, Instant.now()).compareTo(ttl) < 0;
    }
}

