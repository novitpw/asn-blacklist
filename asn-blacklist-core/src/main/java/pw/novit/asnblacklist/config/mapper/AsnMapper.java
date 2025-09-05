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

package pw.novit.asnblacklist.config.mapper;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.config.mapper.AbstractMapper;

/**
 * @author _Novit_ (novitpw)
 */
public final class AsnMapper extends AbstractMapper<Long> {

    public static @NotNull AsnMapper create() {
        return new AsnMapper();
    }

    private AsnMapper() {
        super(Long.class);
    }

    @Override
    protected Long doMap(Object o) {
        if (o instanceof String value) {
            if (value.isEmpty() || value.isBlank()) return null;
            if (!value.startsWith("AS")) {
                val longValue = tryParseLong(value);
                if (longValue != null) return longValue;

                throw new IllegalArgumentException("Value must start with AS");
            }

            val asnValue = value.substring(2);
            if (!asnValue.matches("\\d+")) throw new IllegalArgumentException("AS must contain an number value");

            return Long.parseLong(asnValue);
        } else if (o instanceof Number value) {
            return value.longValue();
        }

        return null;
    }

    private static @Nullable Long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
