package com.hymines.currency.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hymines.currency.model.CurrencyMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class CurrencyConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Preserve insertion order for predictable JSON output
    private Map<String, CurrencyMetadata> currencies = new LinkedHashMap<>();

    public CurrencyConfig() {
    }

    public Map<String, CurrencyMetadata> getCurrencies() {
        return currencies;
    }

    public CurrencyMetadata getCurrency(String id) {
        return currencies.get(id);
    }

    // Load currency.json from plugin data folder; if missing, copy default from resources
    public static CurrencyConfig load(Path dataFolder) throws IOException {
        Path configFile = dataFolder.resolve("currency.json");

        if (!Files.exists(configFile)) {
            Files.createDirectories(dataFolder);
            // Try copying default resource
            try (InputStream in = CurrencyConfig.class.getClassLoader().getResourceAsStream("currency.json")) {
                if (in != null) {
                    Files.copy(in, configFile);
                } else {
                    // Create a programmatic default if resource not found
                    CurrencyConfig defaultCfg = createDefault();
                    try (Writer writer = Files.newBufferedWriter(configFile)) {
                        GSON.toJson(defaultCfg, writer);
                    }
                }
            }
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            CurrencyConfig cfg = GSON.fromJson(reader, CurrencyConfig.class);
            if (cfg == null) {
                // fallback to empty/default
                cfg = createDefault();
            }
            return cfg;
        }
    }

    public void save(Path dataFolder) throws IOException {
        Path configFile = dataFolder.resolve("currency.json");
        Files.createDirectories(dataFolder);
        try (Writer writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(this, writer);
        }
    }

    private static CurrencyConfig createDefault() {
        CurrencyConfig cfg = new CurrencyConfig();
        cfg.currencies.put("money", new CurrencyMetadata("money", "Money", "$", "%symbol%%amount%", true, true, BigDecimal.ZERO));
        cfg.currencies.put("vote_points", new CurrencyMetadata("vote_points", "Vote Points", "FP", "%amount% %symbol%", false, false, BigDecimal.ZERO));
        return cfg;
    }

}
