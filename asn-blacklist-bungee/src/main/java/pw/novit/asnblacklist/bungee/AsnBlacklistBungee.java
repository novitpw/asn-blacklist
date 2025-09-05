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

package pw.novit.asnblacklist.bungee;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public final class AsnBlacklistBungee extends Plugin {

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

    @Override
    @SneakyThrows
    public void onEnable() {
        val dataDirectory = this.dataDirectory = getDataFolder().toPath();
        val logger = this.logger = LoggerFactory.getLogger("AsnBlacklist");

        val fileConfig = FileConfigReader.read(dataDirectory.resolve("config.yaml"));
        FileConfigMigrations.migrate(logger, fileConfig, fileConfig);
        val fileConfigValues = this.fileConfigValues = FileConfigValues.create(fileConfig);

        this.asnBlacklistRegistry = FileConfigAsnBlacklistRegistry.create(fileConfigValues,
                InMemoryAsnBlacklistRegistry.create(ConcurrentHashMap.newKeySet()));

        reloadTranslations();
        reloadDatabase();

        val bungeeAudiences = BungeeAudiences.builder(this)
                .build();

        val pluginManager = getProxy().getPluginManager();
        pluginManager.registerListener(this, new AsnBlacklistBungeeListener(this, logger,
                asnBlacklistService));
        pluginManager.registerCommand(this, new AsnBlacklistBungeeCommand(bungeeAudiences, this,
                asnBlacklistRegistry));
    }

    private void reloadTranslations() throws IOException {
        TranslationRegistrar.registerGlobal(fileConfigValues.getDefaultLocale(),
                dataDirectory.resolve("translations"));
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
