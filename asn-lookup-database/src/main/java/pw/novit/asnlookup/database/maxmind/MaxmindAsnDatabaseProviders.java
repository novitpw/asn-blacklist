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

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import pw.novit.asnlookup.database.AsnDatabaseLoadException;
import pw.novit.asnlookup.database.AsnDatabaseProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author _Novit_ (novitpw)
 */
@UtilityClass
public final class MaxmindAsnDatabaseProviders {

    private static final String DEFAULT_MAXMIND_DATABASE_URL
            = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-ASN&license_key=%s&suffix=tar.gz";

    public static @NotNull AsnDatabaseProvider download(
            @NotNull HttpClient client,
            @NotNull String licenseKey
    ) {
        return unpackTarGz(fromURL(client, String.format(DEFAULT_MAXMIND_DATABASE_URL,
                URLEncoder.encode(licenseKey, StandardCharsets.UTF_8))));
    }

    public @NotNull AsnDatabaseProvider unpackZip(@NotNull AsnDatabaseProvider delegate) {
        return new Zip(delegate);
    }

    public @NotNull AsnDatabaseProvider unpackTar(@NotNull AsnDatabaseProvider delegate) {
        return new Tar(delegate);
    }

    public @NotNull AsnDatabaseProvider unpackTarGz(@NotNull AsnDatabaseProvider delegate) {
        return unpackTar(decompressGZIP(delegate));
    }

    public @NotNull AsnDatabaseProvider decompressGZIP(@NotNull AsnDatabaseProvider delegate) {
        return new GZIP(delegate);
    }

    public @NotNull AsnDatabaseProvider fromURL(@NotNull HttpClient client, @NotNull String url) {
        return fromURI(client, URI.create(url));
    }

    public @NotNull AsnDatabaseProvider fromURI(@NotNull HttpClient client, @NotNull URI url) {
        return new FromURL(client, url);
    }

    public @NotNull AsnDatabaseProvider fromFile(@NotNull Path path) {
        return new FromFile(path);
    }

    private record Zip(AsnDatabaseProvider delegate) implements AsnDatabaseProvider {

        @Override
        public @NotNull InputStream openStream() throws IOException {
            val is = delegate.openStream();
            val zis = new ZipInputStream(is);

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".mmdb")) {
                    return zis;
                }
            }

            throw new IllegalStateException("No .mmdb file in ZIP archive from " + delegate);
        }

    }

    private record GZIP(AsnDatabaseProvider delegate) implements AsnDatabaseProvider {

        @Override
        public @NotNull InputStream openStream() throws IOException {
            return new GZIPInputStream(delegate.openStream());
        }

    }

    private record Tar(AsnDatabaseProvider delegate) implements AsnDatabaseProvider {

        @Override
        public @NotNull InputStream openStream() throws IOException {
            val is = delegate.openStream();
            val tis = new TarInputStream(is);

            TarEntry entry;

            while ((entry = tis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".mmdb")) {
                    return tis;
                }
            }

            throw new IllegalStateException("No .mmdb file in TAR archive from " + delegate);
        }

    }

    private record FromFile(Path source) implements AsnDatabaseProvider {
        @Override
        public @NotNull InputStream openStream() throws IOException {
            return Files.newInputStream(source);
        }

        @Override
        public String toString() {
            return source.toString();
        }
    }

    private record FromURL(HttpClient client, URI uri) implements AsnDatabaseProvider {
        @Override
        public @NotNull InputStream openStream() throws IOException {
            val request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<InputStream> response;

            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", e);
            }

            return switch (response.statusCode()) {
                case 200 -> response.body();
                case 401 -> throw new AsnDatabaseLoadException("Invalid key");
                case 403 -> throw new AsnDatabaseLoadException("Access denied");
                case 429 -> throw new AsnDatabaseLoadException("Too many requests");
                default -> throw new AsnDatabaseLoadException("Could not download database, status code: " +
                                                              response.statusCode());
            };
        }

        @Override
        public String toString() {
            return uri.toString();
        }
    }
}
