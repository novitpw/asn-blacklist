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

package pw.novit.asnblacklist.registry;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InMemoryAsnBlacklistRegistry implements AsnBlacklistRegistry {

    public static @NotNull AsnBlacklistRegistry create() {
        return new InMemoryAsnBlacklistRegistry(new HashSet<>());
    }

    public static @NotNull AsnBlacklistRegistry create(@NotNull Set<@NotNull Long> blacklistedAsn) {
        return new InMemoryAsnBlacklistRegistry(blacklistedAsn);
    }

    Set<Long> blacklistedAsn;

    @Override
    public void reload() {
        blacklistedAsn.clear();
    }

    @Override
    public boolean add(long asn) {
        return blacklistedAsn.add(asn);
    }

    @Override
    public boolean remove(long asn) {
        return blacklistedAsn.remove(asn);
    }

    @Override
    public boolean contains(long asn) {
        return blacklistedAsn.contains(asn);
    }

    @Override
    public @NotNull Set<Long> getBlacklistedAsn() {
        return Set.copyOf(blacklistedAsn);
    }
}
