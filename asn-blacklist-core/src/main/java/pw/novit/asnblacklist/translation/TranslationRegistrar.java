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

import lombok.experimental.UtilityClass;
import lombok.val;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author _Novit_ (novitpw)
 */
@UtilityClass
public class TranslationRegistrar {
    public static final Locale DEFAULT_LOCALE = new Locale("ru");

    private static MiniMessageTranslationStore translationStore;

    public static void registerGlobal() {
        if (translationStore != null) {
            GlobalTranslator.translator().removeSource(translationStore);
            translationStore = null;
        }

        translationStore = TranslationRegistrar.createStore();
        GlobalTranslator.translator().addSource(translationStore);
    }

    public static @NotNull MiniMessageTranslationStore createStore() {
        val translationStore = MiniMessageTranslationStore.create(Key.key("asnblacklist",
                "translations"));
        translationStore.defaultLocale(DEFAULT_LOCALE);

        val resourceBundle = ResourceBundle.getBundle("messages", DEFAULT_LOCALE,
                UTF8ResourceBundleControl.utf8ResourceBundleControl());

        translationStore.registerAll(DEFAULT_LOCALE, resourceBundle.keySet(), resourceBundle::getString);

        return translationStore;
    }

}
