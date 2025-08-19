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

package pw.novit.asnlookup.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import pw.novit.asnlookup.AsnLookupException;
import pw.novit.asnlookup.AsnLookupService;
import pw.novit.asnlookup.model.AsnResponse;

import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CaffeineCachedAsnLookupService implements AsnLookupService {

    LoadingCache<InetAddress, AsnResponse> cache;

    public static @NotNull AsnLookupService create(
            @NotNull AsnLookupService delegate
    ) {
        return new CaffeineCachedAsnLookupService(
                Caffeine.newBuilder()
                        .softValues()
                        .expireAfterAccess(12, TimeUnit.HOURS)
                        .build(delegate::lookup)
        );
    }

    public static @NotNull AsnLookupService create(
            @NotNull AsnLookupService delegate,
            @NotNull Consumer<@NotNull Caffeine<?, ?>> builderInitializer
    ) {
        val builder = Caffeine.newBuilder();
        builderInitializer.accept(builder);

        return new CaffeineCachedAsnLookupService(builder.build(delegate::lookup));
    }

    @Override
    public @NotNull AsnResponse lookup(@NotNull InetAddress address) throws AsnLookupException {
        return Objects.requireNonNull(cache.get(address));
    }
}
