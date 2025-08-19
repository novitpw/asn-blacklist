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

package pw.novit.asnlookup.database.maxmind;

import com.maxmind.db.NoCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import pw.novit.asnlookup.AsnLookupException;
import pw.novit.asnlookup.AsnLookupService;
import pw.novit.asnlookup.database.AsnDatabaseProvider;
import pw.novit.asnlookup.model.AsnResponse;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaxmindAsnLookupService implements AsnLookupService {
    DatabaseReader reader;

    public static @NotNull AsnLookupService create(@NotNull AsnDatabaseProvider provider) throws IOException {
        DatabaseReader reader;

        try (val is = provider.openStream()) {
            reader = new DatabaseReader.Builder(is)
                    .fileMode(Reader.FileMode.MEMORY)
                    .withCache(NoCache.getInstance())
                    .build();
        }

        return new MaxmindAsnLookupService(reader);
    }

    @Override
    public @NotNull AsnResponse lookup(@NotNull InetAddress address) throws AsnLookupException {
        try {
            return reader.tryAsn(address)
                    .map(this::mapResponse)
                    .orElse(AsnResponse.empty());
        } catch (IOException | GeoIp2Exception e) {
            throw new AsnLookupException("Failed to lookup IP address: " + address, e);
        }
    }

    private AsnResponse mapResponse(com.maxmind.geoip2.model.AsnResponse asnResponse) {
        return new AsnResponse(
                asnResponse.getAutonomousSystemNumber(),
                asnResponse.getAutonomousSystemOrganization()
        );
    }

}
