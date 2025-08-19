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
import lombok.val;
import org.jetbrains.annotations.NotNull;
import pw.novit.asnblacklist.config.FileConfigValues;

import java.util.Set;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileConfigAsnBlacklistRegistry implements AsnBlacklistRegistry {

    public static @NotNull AsnBlacklistRegistry create(
            @NotNull FileConfigValues configValues,
            @NotNull AsnBlacklistRegistry delegate
    ) {
        val registry = new FileConfigAsnBlacklistRegistry(configValues, delegate);
        registry.reload();

        return registry;
    }

    FileConfigValues fileConfigValues;
    AsnBlacklistRegistry delegate;

    @Override
    public void reload() {
        delegate.reload();

        for (val value : fileConfigValues.getAsnBlacklist()) {
            delegate.add(value);
        }
    }

    @Override
    public boolean add(long asn) {
        val added = delegate.add(asn);
        if (added) {
            fileConfigValues.setAsnBlacklist(delegate.getBlacklistedAsn());
        }

        return added;
    }

    @Override
    public boolean remove(long asn) {
        val removed = delegate.remove(asn);
        if (removed) {
            fileConfigValues.setAsnBlacklist(delegate.getBlacklistedAsn());
        }

        return removed;
    }

    @Override
    public boolean contains(long asn) {
        return delegate.contains(asn);
    }

    @Override
    public @NotNull Set<Long> getBlacklistedAsn() {
        return delegate.getBlacklistedAsn();
    }

}
