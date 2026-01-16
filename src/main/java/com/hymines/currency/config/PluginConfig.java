package com.hymines.currency.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hymines.currency.storage.StorageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class PluginConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "config.json";

    // Storage settings
    private String storageType = "json";
    private int storageThreads = 2;
    private DatabaseSettings database = new DatabaseSettings();

    public PluginConfig() {
    }

    public StorageType getStorageType() {
        return StorageType.getStorageType(storageType, StorageType.JSON);
    }

    public String getStorageTypeRaw() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public int getStorageThreads() {
        return Math.max(1, storageThreads);
    }

    public void setStorageThreads(int storageThreads) {
        this.storageThreads = storageThreads;
    }

    public DatabaseSettings getDatabase() {
        return database;
    }

    public static PluginConfig load(Path dataFolder) throws IOException {
        Path configFile = dataFolder.resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configFile)) {
            Files.createDirectories(dataFolder);
            try (InputStream in = PluginConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
                if (in != null) {
                    Files.copy(in, configFile);
                } else {
                    PluginConfig defaultCfg = createDefault();
                    try (Writer writer = Files.newBufferedWriter(configFile)) {
                        GSON.toJson(defaultCfg, writer);
                    }
                }
            }
        }

        try (Reader reader = Files.newBufferedReader(configFile)) {
            PluginConfig cfg = GSON.fromJson(reader, PluginConfig.class);
            if (cfg == null) {
                cfg = createDefault();
            }
            return cfg;
        }
    }

    public void save(Path dataFolder) throws IOException {
        Path configFile = dataFolder.resolve(CONFIG_FILE_NAME);
        Files.createDirectories(dataFolder);
        try (Writer writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(this, writer);
        }
    }

    private static PluginConfig createDefault() {
        return new PluginConfig();
    }

    public static class DatabaseSettings {
        private String host = "localhost";
        private int port = 3306;
        private String database = "hytale";
        private String username = "root";
        private String password = "password";

        public DatabaseSettings() {
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getDatabase() {
            return database;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
