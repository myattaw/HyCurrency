package com.hymines.currency.storage;

import java.util.List;

public enum StorageType {

    // FILE
    YAML("YAML", "yaml", "yml"),
    JSON("JSON", "json"),

    // SQL
    MYSQL("MySQL", "mysql"),
    POSTGRESQL("PostgreSQL", "postgres", "postgresql"),
    SQLITE("SQLite", "sqlite");

    private final String name;
    private final List<String> aliases;

    StorageType(String name, String... aliases) {
        this.name = name;
        this.aliases = List.of(aliases);
    }

    public static StorageType getStorageType(String name, StorageType defaultType) {
        for (StorageType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
            for (String alias : type.aliases) {
                if (alias.equalsIgnoreCase(name)) {
                    return type;
                }
            }
        }
        return defaultType;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
