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
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS {table} (player_uuid {pk_type} PRIMARY KEY)";

    // Column operations
    public static final String ALTER_TABLE_ADD_COLUMN = "ALTER TABLE {table} ADD COLUMN {column} {type} DEFAULT 0";
    public static final String ALTER_TABLE_ADD_COLUMN_IF_NOT_EXISTS = "ALTER TABLE {table} ADD COLUMN IF NOT EXISTS {column} {type} DEFAULT 0";
    public static final String ALTER_TABLE_DROP_COLUMN = "ALTER TABLE {table} DROP COLUMN {column}";

    // Select queries
    public static final String SELECT_PLAYER_CURRENCIES = "SELECT {columns} FROM {table} WHERE player_uuid = ?";
    public static final String SELECT_TOP_BALANCES = "SELECT player_uuid, {column} FROM {table} ORDER BY {column} DESC LIMIT ?";

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
    public static final String POSTGRES_CURRENCY_TYPE = "DECIMAL(19,4)";
    public static final String POSTGRES_PK_TYPE = "VARCHAR(36)";
    public static final String SQLITE_CURRENCY_TYPE = "REAL";
    public static final String SQLITE_PK_TYPE = "TEXT";

}

