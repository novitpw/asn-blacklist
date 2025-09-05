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

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author _Novit_ (novitpw)
 */
@UtilityClass
public class TranslationRegistrar {

    private static final String TRANSLATIONS_BUNDLE = "translations";
    private static final int TRANSLATION_BUNDLE_LENGTH = TRANSLATIONS_BUNDLE.length();

    @Getter
    private static Locale defaultLocale;

    private static MiniMessageTranslationStore translationStore;

    public static void registerGlobal(@NotNull Locale locale, @NotNull Path path) throws IOException {
        defaultLocale = locale;

        if (translationStore != null) {
            GlobalTranslator.translator().removeSource(translationStore);
            translationStore = null;
        }

        translationStore = TranslationRegistrar.createStore(path);
        GlobalTranslator.translator().addSource(translationStore);
    }

    public static @NotNull MiniMessageTranslationStore createStore(@NotNull Path path) throws IOException {
        Files.createDirectories(path);

        val translationStore = MiniMessageTranslationStore.create(Key.key("asnblacklist",
                "translations"));
        translationStore.defaultLocale(defaultLocale);

        val resourceBundle = ResourceBundle.getBundle("translations", defaultLocale,
                UTF8ResourceBundleControl.utf8ResourceBundleControl(path));

        translationStore.registerAll(defaultLocale, resourceBundle.keySet(), resourceBundle::getString);

        return translationStore;
    }

    private static void processFile(
            Path file,
            ResourceBundle.Control control,
            MiniMessageTranslationStore translationStore
    ) {
        String fileName = file.getFileName().toString();

        val dotSeparator = fileName.lastIndexOf('.');
        if (dotSeparator != -1) {
            fileName = fileName.substring(0, dotSeparator);
        }

        if (fileName.equals(TRANSLATIONS_BUNDLE)) {
            // Это локализация по-умолчанию
            return;
        }

        String localeName = fileName.substring(TRANSLATION_BUNDLE_LENGTH);
        if (localeName.startsWith("_")) {
            localeName = localeName.substring(1);
        }

        localeName = localeName.replace('_', '-');

        val resourceBundle = ResourceBundle.getBundle(TRANSLATIONS_BUNDLE,
                Locale.forLanguageTag(localeName),
                control);

        translationStore.registerAll(resourceBundle.getLocale(),
                resourceBundle.keySet(),
                resourceBundle::getString);
    }

}
