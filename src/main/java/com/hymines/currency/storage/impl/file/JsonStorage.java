package com.hymines.currency.storage.impl.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;
import com.hymines.currency.storage.CurrencyStorage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JsonStorage implements CurrencyStorage {

    private final HyCurrencyPlugin plugin;
    private final Path dataFolder;
    private final Gson gson;
    private final Map<String, Map<String, BigDecimal>> dataCache = new ConcurrentHashMap<>();

    private static final Type DATA_TYPE = new TypeToken<Map<String, BigDecimal>>() {}.getType();

    public JsonStorage(HyCurrencyPlugin plugin, Path dataFolder) {
        this.plugin = plugin;
        this.dataFolder = dataFolder;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void initialize() {
        try {
            Files.createDirectories(dataFolder);
        } catch (IOException e) {
            plugin.getLogger().atSevere().log("Failed to create data folder: " + e.getMessage());
        }
    }

    private Path getPlayerFile(String playerUuid) {
        return dataFolder.resolve(playerUuid + ".json");
    }

    @Override
    public CompletableFuture<CurrencyModel> loadAsync(String playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            CurrencyModel model = loadFromStorage(playerUuid);
            plugin.getCurrencyDataMap().put(playerUuid, model);
            return model;
        }, plugin.getDbExecutor());
    }

    private CurrencyModel loadFromStorage(String playerUuid) {
        CurrencyModel model = new CurrencyModel();
        Path playerFile = getPlayerFile(playerUuid);

        if (Files.exists(playerFile)) {
            try {
                String json = Files.readString(playerFile);
                Map<String, BigDecimal> data = gson.fromJson(json, DATA_TYPE);
                if (data != null) {
                    dataCache.put(playerUuid, new ConcurrentHashMap<>(data));
                    for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
                        model.setCurrency(entry.getKey(), entry.getValue());
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().atSevere().log("Failed to load player data for " + playerUuid + ": " + e.getMessage());
            }
        } else {
            // Initialize with default currencies
            var currencies = plugin.getCurrencyConfig().getCurrencies();
            if (currencies != null) {
                for (String currencyId : currencies.keySet()) {
                    model.addCurrency(currencyId);
                }
            }
        }
        return model;
    }

    @Override
    public CompletableFuture<Void> saveAsync(String playerId, CurrencyModel model) {
        return CompletableFuture.runAsync(() -> save(playerId, model), plugin.getDbExecutor());
    }

    private void save(String playerId, CurrencyModel model) {
        Path playerFile = getPlayerFile(playerId);
        Map<String, BigDecimal> data = model.getCurrencies();
        dataCache.put(playerId, new ConcurrentHashMap<>(data));

        try {
            String json = gson.toJson(data);
            Files.writeString(playerFile, json);
        } catch (IOException e) {
            plugin.getLogger().atSevere().log("Failed to save player data for " + playerId + ": " + e.getMessage());
        }
    }

    @Override
    public void saveAll() {
        for (Map.Entry<String, CurrencyModel> entry : plugin.getCurrencyDataMap().entrySet()) {
            save(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addCurrency(String currencyId) {
        // JSON storage doesn't need schema changes
        // New currencies are added dynamically when saved
    }

    @Override
    public void removeCurrency(String currencyId, boolean deleteData) {
        if (deleteData) {
            // Remove from cache
            for (Map<String, BigDecimal> playerData : dataCache.values()) {
                playerData.remove(currencyId);
            }
            // Remove from loaded models
            for (CurrencyModel model : plugin.getCurrencyDataMap().values()) {
                // Note: Would need to add a removeCurrency method to CurrencyModel
            }
            // Save all to persist changes
            saveAll();
        }
    }

    @Override
    public void unload() {
        saveAll();
        dataCache.clear();
    }

    @Override
    public CompletableFuture<Map<String, Integer>> getTopBalances(String currencyId, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            // First, load all player files to ensure cache is complete
            try {
                Files.list(dataFolder).filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                    String uuid = p.getFileName().toString().replace(".json", "");
                    if (!dataCache.containsKey(uuid)) {
                        loadFromStorage(uuid);
                    }
                });
            } catch (IOException e) {
                plugin.getLogger().atSevere().log("Failed to list player files: " + e.getMessage());
            }

            // Sort and return top balances
            return dataCache.entrySet().stream().filter(e -> e.getValue().containsKey(currencyId)).sorted((a, b) -> b.getValue().get(currencyId).compareTo(a.getValue().get(currencyId))).limit(limit).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(currencyId).intValue(), (a, b) -> a, LinkedHashMap::new));
        }, plugin.getDbExecutor());
    }

}

