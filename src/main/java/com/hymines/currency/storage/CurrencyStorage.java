package com.hymines.currency.storage;

import com.hymines.currency.model.CurrencyModel;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CurrencyStorage {

    /**
     * Initialize the storage system
     */
    void initialize();

    /**
     * Load player's currency data asynchronously
     */
    CompletableFuture<CurrencyModel> loadAsync(String playerUuid);

    /**
     * Save player's currency data asynchronously
     */
    CompletableFuture<Void> saveAsync(String playerId, CurrencyModel model);

    /**
     * Save all players' currency data
     */
    void saveAll();

    /**
     * Add a new currency to the storage schema
     */
    void addCurrency(String currencyId);

    /**
     * Remove a currency from the storage schema
     *
     * @param deleteData whether to delete existing data for this currency
     */
    void removeCurrency(String currencyId, boolean deleteData);

    /**
     * Close all resources and connections
     */
    void unload();

    /**
     * Get top players for a specific currency
     *
     * @param currencyId the currency to get top players for
     * @param limit      maximum number of results
     * @return A map of player names to amounts, ordered by amount (descending)
     */
    CompletableFuture<Map<String, Integer>> getTopBalances(String currencyId, int limit);

}
