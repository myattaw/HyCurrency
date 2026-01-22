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

package com.reliableplugins.currency;

import com.reliableplugins.currency.api.Economy;
import com.reliableplugins.currency.api.EconomyProviderRegistry;
import com.reliableplugins.currency.command.CurrencyCommand;
import com.reliableplugins.currency.config.CurrencyConfig;
import com.reliableplugins.currency.config.PluginConfig;
import com.reliableplugins.currency.listener.PlayerCurrencyHandler;
import com.reliableplugins.currency.model.CurrencyManager;
import com.reliableplugins.currency.model.CurrencyModel;
import com.reliableplugins.currency.service.CurrencyService;
import com.reliableplugins.currency.storage.StorageFactory;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HyCurrencyPlugin extends JavaPlugin {

    private static volatile HyCurrencyPlugin instance;

    private final Map<String, CurrencyModel> currencyDataMap = new ConcurrentHashMap<>();
    private ExecutorService dbExecutor;

    // Configurations
    private PluginConfig pluginConfig;
    private CurrencyConfig currencyConfig;

    private CurrencyManager currencyManager;
    private Economy economy;

    public HyCurrencyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        // set static instance
        instance = this;

        loadConfig();
        initializeStorage();

        getCommandRegistry().registerCommand(new CurrencyCommand(this));
        PlayerCurrencyHandler.register(this);

        // Register Economy provider for other plugins
        if (this.economy == null) {
            throw new IllegalStateException("Economy failed to initialize in HyCurrency");
        }
        EconomyProviderRegistry.register(this.economy);

        getLogger().atInfo().log("HyCurrency economy registered");
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        if (currencyManager != null) {
            currencyManager.shutdown();
        }
        if (dbExecutor != null) {
            dbExecutor.shutdown();
            try {
                if (!dbExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    dbExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                dbExecutor.shutdownNow();
            }
        }

        // clear static instance
        instance = null;
    }

    private void loadConfig() {
        try {
            Path dataFolder = getDataDirectory();
            this.pluginConfig = PluginConfig.load(dataFolder);
            getLogger().atInfo().log("Loaded plugin configuration (storage: " + pluginConfig.getStorageType().getName() + ")");
        } catch (IOException e) {
            getLogger().atSevere().log("Failed to load plugin config: " + e.getMessage());
            this.pluginConfig = new PluginConfig();
        }

        try {
            Path dataFolder = getDataDirectory();
            this.currencyConfig = CurrencyConfig.load(dataFolder);
            getLogger().atInfo().log("Loaded " + (currencyConfig.getCurrencies() == null ? 0 : currencyConfig.getCurrencies().size()) + " currencies from currency.json");
        } catch (IOException e) {
            getLogger().atSevere().log("Failed to load currency config: " + e.getMessage());
            this.currencyConfig = new CurrencyConfig();
        }
    }

    private void initializeStorage() {
        this.dbExecutor = Executors.newFixedThreadPool(pluginConfig.getStorageThreads());
        StorageFactory storageFactory = new StorageFactory(this, pluginConfig);
        this.currencyManager = new CurrencyManager(this, storageFactory.createAndInitialize());
        economy = new CurrencyService(this, currencyManager);
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public CurrencyConfig getCurrencyConfig() {
        return currencyConfig;
    }

    public ExecutorService getDbExecutor() {
        return dbExecutor;
    }

    public Map<String, CurrencyModel> getCurrencyDataMap() {
        return currencyDataMap;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    @Nonnull
    public static Economy getEconomy() {
        return EconomyProviderRegistry.get();
    }

}

