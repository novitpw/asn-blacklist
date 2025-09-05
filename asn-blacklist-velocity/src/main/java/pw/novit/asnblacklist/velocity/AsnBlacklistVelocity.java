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
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.slf4j.Logger;
import pw.novit.asnblacklist.AsnBlacklistService;
import pw.novit.asnblacklist.SimpleAsnBlacklistService;
import pw.novit.asnblacklist.config.FileConfigMigrations;
import pw.novit.asnblacklist.config.FileConfigReader;
import pw.novit.asnblacklist.config.FileConfigValues;
import pw.novit.asnblacklist.registry.AsnBlacklistRegistry;
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
@NoArgsConstructor(onConstructor_ = @Inject)
public final class AsnBlacklistVelocity {

    @NonFinal
    Path dataDirectory;

    @NonFinal
    Logger logger;

    @NonFinal
    FileConfigValues fileConfigValues;

    @NonFinal
    AsnBlacklistRegistry asnBlacklistRegistry;

    @NonFinal
    AsnBlacklistService asnBlacklistService;

    @Inject
    public void configure(
            @DataDirectory Path dataDirectory,
            Logger logger,
            PluginContainer pluginContainer,
            EventManager eventManager,
            CommandManager commandManager
    ) throws Exception {
        if (!hasMiniMessageTranslations()) {
            logger.error("      / \\");
            logger.error("     /   \\");
            logger.error("    /  |  \\");
            logger.error("   /   |   \\          PLUGIN REQUIRES VELOCITY 3.4.0-b516 OR NEWER");
            logger.error("  /         \\      PLEASE UPDATE VELOCITY TO BUILD 3.4.0-b516 OR NEWER");
            logger.error(" /     o     \\");
            logger.error("/_____________\\");

            throw new UnsupportedOperationException("PLUGIN REQUIRES VELOCITY 3.4.0-b516 OR NEWER");
        }

        this.dataDirectory = dataDirectory;
        this.logger = logger;

        val fileConfig = FileConfigReader.read(dataDirectory.resolve("config.yaml"));
        FileConfigMigrations.migrate(logger, fileConfig, fileConfig);
        this.fileConfigValues = FileConfigValues.create(fileConfig);

        this.asnBlacklistRegistry = FileConfigAsnBlacklistRegistry.create(fileConfigValues,
                InMemoryAsnBlacklistRegistry.create(ConcurrentHashMap.newKeySet()));

        reloadTranslations();
        reloadDatabase();

        eventManager.register(pluginContainer, new AsnBlacklistVelocityListener(logger,
                asnBlacklistService));

        commandManager.register(commandManager.metaBuilder("asnblacklist")
                        .aliases("asnbl")
                        .build(),
                AsnBlacklistVelocityCommand.create(this, asnBlacklistRegistry));
    }

    private boolean hasMiniMessageTranslations() {
        try {
            Class.forName("net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void reloadTranslations() {
        TranslationRegistrar.registerGlobal(dataDirectory.resolve("translations"));
    }

    private void reloadDatabase() throws IOException {
        logger.info("Loading database...");
        val currentMillis = System.currentTimeMillis();

        asnBlacklistService = SimpleAsnBlacklistService.create(
                AsnLookupExecutors.polled(),
                CaffeineCachedAsnLookupService.create(
                        MaxmindAsnLookupService.create(
                                FileCacheAsnDatabaseProvider.cache(
                                        MaxmindAsnDatabaseProviders.download(
                                                HttpClient.newBuilder()
                                                        .followRedirects(HttpClient.Redirect.NORMAL)
                                                        .build(),
                                                fileConfigValues.getMaxmindDatabaseKey()
                                        ),
                                        dataDirectory.resolve(fileConfigValues.getMaxmindDatabaseFile()),
                                        fileConfigValues.getMaxmindDatabaseTTL()
                                )
                        ),
                        fileConfigValues.getCacheTTL()
                ),
                asnBlacklistRegistry
        );

        logger.info("Database loaded ({}ms).", System.currentTimeMillis() - currentMillis);
    }

    @SneakyThrows
    public void reloadAll() {
        fileConfigValues.reload();
        asnBlacklistRegistry.reload();

        reloadTranslations();
        reloadDatabase();
    }

}
