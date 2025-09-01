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

package pw.novit.asnblacklist.config;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.slf4j.Logger;
import pw.novit.asnblacklist.config.migrations.FileConfigMigration_v1_1;
import w.config.FileConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author _Novit_ (novitpw)
 */
@UtilityClass
public class FileConfigMigrations {

    private List<FileConfigMigration> migrations() {
        val migrations = new ArrayList<FileConfigMigration>();
        migrations.add(new FileConfigMigration_v1_1());

        return migrations;
    }

    public void migrate(Logger logger, FileConfig src, FileConfig dst) {
        for (val migration : migrations()) {
            int changes;
            if ((changes = migration.migrate(src, dst)) > 0) {
                logger.info("Applied config migration {} ({} changes)", migration.version(), changes);
            }
        }
    }

}
