package com.hymines.currency.model;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.storage.CurrencyStorage;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyManager {

    private final HyCurrencyPlugin plugin;
    private final CurrencyStorage storage;

    // Cache for leaderboards - Map of currency ID to leaderboard entries (player UUID to amount)
    private final Map<String, Map<String, Integer>> leaderboardCache = new ConcurrentHashMap<>();
    private static final int DEFAULT_LEADERBOARD_LIMIT = 1000;

    public CurrencyManager(HyCurrencyPlugin plugin, CurrencyStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public CurrencyStorage getStorage() {
        return storage;
    }

    public CompletableFuture<CurrencyModel> loadPlayer(String playerUuid) {
        return storage.loadAsync(playerUuid);
    }

    public CompletableFuture<Void> savePlayer(String playerUuid) {
        CurrencyModel model = plugin.getCurrencyDataMap().get(playerUuid);
        if (model != null) {
            return storage.saveAsync(playerUuid, model);
        }
        return CompletableFuture.completedFuture(null);
    }

    public CurrencyModel getPlayerData(String playerUuid) {
        return plugin.getCurrencyDataMap().get(playerUuid);
    }

    public BigDecimal getBalance(String playerUuid, String currencyId) {
        CurrencyModel model = getPlayerData(playerUuid);
        return model != null ? model.getCurrency(currencyId) : BigDecimal.ZERO;
    }

    public void setBalance(String playerUuid, String currencyId, BigDecimal amount) {
        CurrencyModel model = getPlayerData(playerUuid);
        if (model != null) {
            model.setCurrency(currencyId, amount);
        }
    }

    public void addBalance(String playerUuid, String currencyId, BigDecimal amount) {
        CurrencyModel model = getPlayerData(playerUuid);
        if (model != null) {
            model.addAmount(currencyId, amount);
        }
    }

    public boolean hasBalance(String playerUuid, String currencyId, BigDecimal amount) {
        return getBalance(playerUuid, currencyId).compareTo(amount) >= 0;
    }

    public CompletableFuture<Map<String, Integer>> getTopBalances(String currencyId) {
        return getTopBalances(currencyId, DEFAULT_LEADERBOARD_LIMIT);
    }

    public CompletableFuture<Map<String, Integer>> getTopBalances(String currencyId, int limit) {
        return storage.getTopBalances(currencyId, limit)
                .thenApply(results -> {
                    leaderboardCache.put(currencyId, results);
                    return results;
                });
    }

    public Map<String, Integer> getCachedLeaderboard(String currencyId) {
        return leaderboardCache.get(currencyId);
    }

    public void shutdown() {
        storage.unload();
    }
}
