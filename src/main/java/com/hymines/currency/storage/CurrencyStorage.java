package com.hymines.currency.storage;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class CurrencyStorage {

    private final HyCurrencyPlugin plugin;

    public CurrencyStorage(HyCurrencyPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void initialize();

    /**
     * Open the database connection
     */
    public abstract boolean openConnection();

    /**
     * Check if the connection is still valid
     */
    public abstract boolean isConnectionValid();

    /**
     * Load player's currency data asynchronously
     */
    public CompletableFuture<CurrencyModel> loadAsync(String playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            CurrencyModel model = loadFromStorage(playerUuid);
            plugin.getCurrencyDataMap().put(playerUuid, model);
            return model;
        }, plugin.getDbExecutor());
    }

    /**
     * Implementation-specific loading from storage
     */
    protected abstract CurrencyModel loadFromStorage(String playerUuid);

    /**
     * Save player's currency data asynchronously
     */
    public CompletableFuture<Void> saveAsync(String playerId, CurrencyModel model) {
        return CompletableFuture.runAsync(() -> {
            save(playerId, model);
        }, plugin.getDbExecutor());
    }

    /**
     * Implementation-specific saving to storage
     */
    protected abstract void save(String playerId, CurrencyModel model);

    /**
     * Save all players' currency data
     */
    public abstract void saveAll();

    /**
     * Add a new currency to the storage schema
     */
    public abstract void addCurrency(String currencyId);

    /**
     * Remove a currency from the storage schema
     *
     * @param deleteData whether to delete existing data for this currency
     */
    public abstract void removeCurrency(String currencyId, boolean deleteData);

    /**
     * Get the active database connection
     */
    public abstract Connection getConnection();

    /**
     * Close all resources and connections
     */
    public abstract void unload();

    /**
     * Get top players for a specific currency
     *
     * @param currencyId the currency to get top players for
     * @param limit      maximum number of results
     * @return A map of player names to amounts, ordered by amount (descending)
     */
    public abstract CompletableFuture<Map<String, Integer>> getTopBalances(String currencyId, int limit);

}


