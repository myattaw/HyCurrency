/*
 * MIT License
 *
 * Copyright (c) 2026 Michael Yattaw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * See the LICENSE file in the project root for full license information.
 */

package com.reliableplugins.currency.storage;

import com.reliableplugins.currency.HyCurrencyPlugin;
import com.reliableplugins.currency.config.PluginConfig;
import com.reliableplugins.currency.storage.impl.file.JsonStorage;
import com.reliableplugins.currency.storage.impl.sql.MySQLStorage;
import com.reliableplugins.currency.storage.impl.sql.PostgresStorage;
import com.reliableplugins.currency.storage.impl.sql.SQLiteStorage;

import java.nio.file.Path;

public class StorageFactory {

    private final HyCurrencyPlugin plugin;
    private final PluginConfig config;

    public StorageFactory(HyCurrencyPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public CurrencyStorage createAndInitialize() {
        StorageType type = config.getStorageType();
        try {
            CurrencyStorage storage = createStorage(type);
            storage.initialize();
            plugin.getLogger().atInfo().log("Initialized " + type.getName() + " storage");
            return storage;
        } catch (Exception e) {
            plugin.getLogger().atWarning().log("Failed to initialize " + type.getName() + " storage: " + e.getMessage());
            if (type != StorageType.JSON) {
                plugin.getLogger().atInfo().log("Falling back to JSON storage");
                CurrencyStorage fallback = createJsonStorage();
                fallback.initialize();
                return fallback;
            }
            throw e;
        }
    }

    private CurrencyStorage createStorage(StorageType type) {
        PluginConfig.DatabaseSettings dbSettings = config.getDatabase();

        return switch (type) {
            case MYSQL -> new MySQLStorage(
                    plugin,
                    dbSettings.getHost(),
                    dbSettings.getPort(),
                    dbSettings.getDatabase(),
                    dbSettings.getUsername(),
                    dbSettings.getPassword()
            );
            case POSTGRESQL -> new PostgresStorage(
                    plugin,
                    dbSettings.getHost(),
                    dbSettings.getPort(),
                    dbSettings.getDatabase(),
                    dbSettings.getUsername(),
                    dbSettings.getPassword()
            );
            case SQLITE -> new SQLiteStorage(plugin, plugin.getDataDirectory());
            case YAML -> {
                plugin.getLogger().atWarning().log("YAML storage not yet implemented, using JSON");
                yield createJsonStorage();
            }
            default -> createJsonStorage();
        };
    }

    private CurrencyStorage createJsonStorage() {
        Path playerDataFolder = plugin.getDataDirectory().resolve("playerdata");
        return new JsonStorage(plugin, playerDataFolder);
    }
}

