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

package pw.novit.asnblacklist.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.slf4j.Logger;
import pw.novit.asnblacklist.SimpleAsnBlacklistService;
import pw.novit.asnblacklist.config.FileConfigReader;
import pw.novit.asnblacklist.config.FileConfigValues;
import pw.novit.asnblacklist.registry.FileConfigAsnBlacklistRegistry;
import pw.novit.asnblacklist.registry.InMemoryAsnBlacklistRegistry;
import pw.novit.asnblacklist.translation.TranslationRegistrar;
import pw.novit.asnlookup.AsnLookupExecutors;
import pw.novit.asnlookup.cache.CaffeineCachedAsnLookupService;
import pw.novit.asnlookup.database.FileCacheAsnDatabaseProvider;
import pw.novit.asnlookup.database.maxmind.MaxmindAsnDatabaseProviders;
import pw.novit.asnlookup.database.maxmind.MaxmindAsnLookupService;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author _Novit_ (novitpw)
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class AsnBlacklistVelocity {

    @Inject
    public void configure(
            @DataDirectory Path path,
            Logger logger,
            PluginContainer pluginContainer,
            EventManager eventManager,
            CommandManager commandManager
    ) throws IOException {
        TranslationRegistrar.registerGlobal();

        val fileConfig = FileConfigReader.read(path.resolve("config.yaml"));
        val fileConfigValues = FileConfigValues.create(fileConfig);

        val registry = FileConfigAsnBlacklistRegistry.create(fileConfigValues,
                InMemoryAsnBlacklistRegistry.create(ConcurrentHashMap.newKeySet()));

        logger.info("Loading database...");
        val currentMillis = System.currentTimeMillis();

        val asnBlacklistService = SimpleAsnBlacklistService.create(
                AsnLookupExecutors.polled(),
                CaffeineCachedAsnLookupService.create(
                        MaxmindAsnLookupService.create(
                                FileCacheAsnDatabaseProvider.cache(
                                        MaxmindAsnDatabaseProviders.download(
                                                HttpClient.newBuilder()
                                                        .followRedirects(HttpClient.Redirect.NORMAL)
                                                        .build(),
                                                fileConfigValues.getMaxmindKey()
                                        ),
                                        path.resolve(fileConfigValues.getMaxmindDatabaseFile())
                                )
                        )
                ),
                registry
        );

        logger.info("Database loaded ({}ms).", System.currentTimeMillis() - currentMillis);

        eventManager.register(pluginContainer, new AsnBlacklistVelocityListener(logger,
                fileConfigValues,
                asnBlacklistService));

        commandManager.register(commandManager.metaBuilder("asnblacklist")
                        .aliases("asnbl")
                        .build(),
                AsnBlacklistVelocityCommand.create(fileConfigValues, registry));
    }

}
