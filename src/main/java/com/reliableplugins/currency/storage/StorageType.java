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

package com.reliableplugins.currency.storage;

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

