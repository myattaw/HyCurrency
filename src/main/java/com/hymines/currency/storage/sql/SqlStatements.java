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

package com.hymines.currency.storage.sql;

/**
 * Contains all SQL statements used by the currency storage system.
 * Use {table} as a placeholder for the table name.
 * Use {columns} as a placeholder for dynamic column names.
 * Use {values} as a placeholder for dynamic values.
 * Use {updates} as a placeholder for dynamic update clauses.
 */
public final class SqlStatements {

    // Table creation
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS {table} (player_uuid {pk_type} PRIMARY KEY, player_name {name_type})";

    // Index creation
    public static final String CREATE_NAME_INDEX_MYSQL = "CREATE INDEX IF NOT EXISTS idx_player_name ON {table} (player_name)";
    public static final String CREATE_NAME_INDEX_POSTGRES = "CREATE INDEX IF NOT EXISTS idx_player_name ON {table} (player_name)";
    public static final String CREATE_NAME_INDEX_SQLITE = "CREATE INDEX IF NOT EXISTS idx_player_name ON {table} (player_name)";

    // Column operations
    public static final String ALTER_TABLE_ADD_COLUMN = "ALTER TABLE {table} ADD COLUMN {column} {type} DEFAULT 0";
    public static final String ALTER_TABLE_ADD_COLUMN_IF_NOT_EXISTS = "ALTER TABLE {table} ADD COLUMN IF NOT EXISTS {column} {type} DEFAULT 0";
    public static final String ALTER_TABLE_DROP_COLUMN = "ALTER TABLE {table} DROP COLUMN {column}";
    public static final String ALTER_TABLE_ADD_NAME_COLUMN = "ALTER TABLE {table} ADD COLUMN player_name {type}";
    public static final String ALTER_TABLE_ADD_NAME_COLUMN_IF_NOT_EXISTS = "ALTER TABLE {table} ADD COLUMN IF NOT EXISTS player_name {type}";

    // Select queries
    public static final String SELECT_PLAYER_CURRENCIES = "SELECT player_name, {columns} FROM {table} WHERE player_uuid = ?";
    public static final String SELECT_TOP_BALANCES = "SELECT player_uuid, player_name, {column} FROM {table} ORDER BY {column} DESC LIMIT ?";
    public static final String SELECT_PLAYER_BY_NAME = "SELECT player_uuid, player_name, {columns} FROM {table} WHERE player_name = ?";

    // Upsert queries (database-specific)
    public static final String UPSERT_MYSQL = "INSERT INTO {table} ({columns}) VALUES ({values}) ON DUPLICATE KEY UPDATE {updates}";
    public static final String UPSERT_SQLITE = "INSERT OR REPLACE INTO {table} ({columns}) VALUES ({values})";
    public static final String UPSERT_POSTGRES = "INSERT INTO {table} ({columns}) VALUES ({values}) ON CONFLICT (player_uuid) DO UPDATE SET {updates}";

    // Connection URLs
    public static final String MYSQL_URL = "jdbc:mysql://{host}:{port}/{database}?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
    public static final String POSTGRES_URL = "jdbc:postgresql://{host}:{port}/{database}";
    public static final String SQLITE_URL = "jdbc:sqlite:{path}";

    // Driver class names
    public static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    public static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    // Column types
    public static final String MYSQL_CURRENCY_TYPE = "DECIMAL(19,4)";
    public static final String MYSQL_PK_TYPE = "VARCHAR(36)";
    public static final String MYSQL_NAME_TYPE = "VARCHAR(32)";
    public static final String POSTGRES_CURRENCY_TYPE = "DECIMAL(19,4)";
    public static final String POSTGRES_PK_TYPE = "VARCHAR(36)";
    public static final String POSTGRES_NAME_TYPE = "VARCHAR(32)";
    public static final String SQLITE_CURRENCY_TYPE = "REAL";
    public static final String SQLITE_PK_TYPE = "TEXT";
    public static final String SQLITE_NAME_TYPE = "TEXT";

}

