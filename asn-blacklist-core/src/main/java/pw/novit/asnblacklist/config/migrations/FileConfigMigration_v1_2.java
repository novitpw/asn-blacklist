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

package pw.novit.asnblacklist.config.migrations;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import pw.novit.asnblacklist.config.FileConfigMigration;
import w.config.FileConfig;
import w.config.MutableConfig;

import java.util.function.Function;

/**
 * @author _Novit_ (novitpw)
 */
public final class FileConfigMigration_v1_2 implements FileConfigMigration {

    @Override
    public @NotNull String version() {
        return "v1.2.x";
    }

    @Override
    public int migrate(@NotNull FileConfig src, @NotNull FileConfig dst) {
        int changes = 0;

        // region Localization
        val defaultLocale = src.getRaw("default-locale", null);
        if (defaultLocale == null) {
            dst.set("default-locale", "ru");
            changes++;
        }
        // endregion

        if (changes > 0) {
            dst.save();
        }

        return changes;
    }

}
