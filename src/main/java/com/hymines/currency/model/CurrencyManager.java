package com.hymines.currency.model;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.storage.CurrencyStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyManager {

    private final HyCurrencyPlugin plugin;
    private final CurrencyStorage storage;

    // Cache for leaderboards - Map of currency ID to leaderboard entries (player name to amount)
    private final Map<String, Map<String, Integer>> leaderboardCache = new ConcurrentHashMap<>();
    private static final int DEFAULT_LEADERBOARD_LIMIT = 1000;

    public CurrencyManager(HyCurrencyPlugin plugin, CurrencyStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }



}
