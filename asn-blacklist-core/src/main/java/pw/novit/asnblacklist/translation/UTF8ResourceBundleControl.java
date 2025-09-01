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

package pw.novit.asnblacklist.translation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UTF8ResourceBundleControl extends ResourceBundle.Control {

    public static ResourceBundle.@NotNull Control utf8ResourceBundleControl(@NotNull Path path) {
        return new UTF8ResourceBundleControl(path);
    }

    Path path;

    @Override
    public ResourceBundle newBundle(final String baseName, final Locale locale, final String format, final ClassLoader loader, final boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        if (format.equals("java.properties")) {
            Files.createDirectories(path);

            final String bundle = this.toBundleName(baseName, locale);
            final String resource = this.toResourceName(bundle, "properties");
            final Path filePath = this.path.resolve(resource);

            if (Files.notExists(filePath)) {
                try (final InputStream is = loader.getResourceAsStream(resource)) {
                    if (is != null) {
                        Files.copy(is, filePath);
                    } else {
                        return null;
                    }
                }
            }

            try (final InputStream is = Files.newInputStream(filePath);
                 final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)
            ) {
                return new PropertyResourceBundle(isr);
            }
        } else {
            return super.newBundle(baseName, locale, format, loader, reload);
        }
    }

}
