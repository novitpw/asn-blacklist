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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.config.ConfigProvider;
import w.config.FileConfig;
import w.config.JacksonConfigProvider;
import w.config.SimpleFileConfig;

import java.nio.file.Path;

/**
 * @author _Novit_ (novitpw)
 */
@UtilityClass
public final class FileConfigReader {

    public static @NotNull FileConfig read(@NotNull Path path) {
        val configProvider = JacksonConfigProvider.create(YAMLMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
                .configure(YAMLParser.Feature.EMPTY_STRING_AS_NULL, false)
                .build());

        return read(path, configProvider);
    }

    public static @NotNull FileConfig read(@NotNull Path path, @NotNull ConfigProvider configProvider) {
        val source = SimpleFileConfig.create(path, configProvider);
        source.saveDefaults("config-defaults.yaml");

        return source;
    }

}
