package com.hymines.currency;

import com.hymines.currency.service.CurrencyService;
import com.hymines.currency.api.Economy;
import com.hymines.currency.command.CurrencyCommand;
import com.hymines.currency.config.PluginConfig;
import com.hymines.currency.config.CurrencyConfig;
import com.hymines.currency.listener.PlayerCurrencyHandler;
import com.hymines.currency.model.CurrencyManager;
import com.hymines.currency.model.CurrencyModel;
import com.hymines.currency.storage.StorageFactory;
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
        loadConfig();
        initializeStorage();
        getCommandRegistry().registerCommand(new CurrencyCommand(this));
        PlayerCurrencyHandler.register(this);
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
        this.economy = new CurrencyService(this, currencyManager);
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

    /**
     * Gets the Economy API implementation.
     *
     * @return The Economy provider for this plugin
     */
    @Nonnull
    public Economy getEconomy() {
        return economy;
    }

}
