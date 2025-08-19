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

package pw.novit.asnblacklist;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import pw.novit.asnblacklist.registry.AsnBlacklistRegistry;
import pw.novit.asnblacklist.registry.InMemoryAsnBlacklistRegistry;
import pw.novit.asnlookup.AsnLookupService;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleAsnBlacklistService implements AsnBlacklistService {

    public static @NotNull AsnBlacklistService create(
            @NotNull ExecutorService executorService,
            @NotNull AsnLookupService asnLookupService
    ) {
        return create(executorService, asnLookupService, InMemoryAsnBlacklistRegistry.create());
    }

    public static @NotNull AsnBlacklistService create(
            @NotNull ExecutorService executorService,
            @NotNull AsnLookupService asnLookupService,
            @NotNull AsnBlacklistRegistry asnBlacklistRegistry
    ) {
        return new SimpleAsnBlacklistService(executorService, asnLookupService, asnBlacklistRegistry);
    }

    ExecutorService executorService;
    AsnLookupService asnLookupService;
    AsnBlacklistRegistry asnBlacklistRegistry;

    @Override
    public @NotNull CompletableFuture<@NotNull Boolean> tryCheckAsync(@NotNull InetAddress address) {
        return CompletableFuture.supplyAsync(() -> tryCheck(address), executorService);
    }

    @Override
    public boolean tryCheck(@NotNull InetAddress address) {
        val response = asnLookupService.lookup(address);
        return asnBlacklistRegistry.contains(response.autonomousSystemNumber());
    }

}
