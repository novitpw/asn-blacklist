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

package pw.novit.asnlookup;

import org.jetbrains.annotations.NotNull;
import pw.novit.asnlookup.model.AsnResponse;

import java.net.InetAddress;

/**
 * @author _Novit_ (novitpw)
 */
public interface AsnLookupService {

    @NotNull AsnLookupService NOOP = __ -> AsnResponse.empty();

    @NotNull AsnResponse lookup(@NotNull InetAddress address) throws AsnLookupException;

}
