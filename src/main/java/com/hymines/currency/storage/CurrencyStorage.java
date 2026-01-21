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

package com.hymines.currency.storage;

import com.hymines.currency.model.CurrencyModel;

import javax.annotation.Nullable;
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
     * Load a player's currency data by their username.
     * This is useful for offline players not in the cache.
     *
     * @param playerName The player's username (case-insensitive)
     * @return A future containing the CurrencyModel, or null if not found
     */
    CompletableFuture<CurrencyModel> loadByNameAsync(String playerName);

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

