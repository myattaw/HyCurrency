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

package com.reliableplugins.currency.storage.impl.sql;

import com.reliableplugins.currency.HyCurrencyPlugin;
import com.reliableplugins.currency.storage.sql.ConnectionPool;
import com.reliableplugins.currency.storage.sql.PreparedStatementBuilder;
import com.reliableplugins.currency.storage.sql.SqlStatements;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class SQLiteStorage extends JDBCStorage {

    private final Path databasePath;

    public SQLiteStorage(HyCurrencyPlugin plugin, Path databasePath) {
        super(plugin);
        this.databasePath = databasePath;
    }

    @Override
    protected ConnectionPool createConnectionPool() {
        String url = SqlStatements.SQLITE_URL
                .replace("{path}", databasePath.toAbsolutePath().toString());

        // SQLite works best with a single connection due to file locking
        return ConnectionPool.builder()
                .poolName("HyCurrency-SQLite")
                .jdbcUrl(url)
                .driverClassName(SqlStatements.SQLITE_DRIVER)
                .maximumPoolSize(1)  // SQLite is single-threaded for writes
                .minimumIdle(1)
                .connectionTimeout(30000)
                // SQLite-specific pragmas for better performance
                .addDataSourceProperty("journal_mode", "WAL")
                .addDataSourceProperty("synchronous", "NORMAL")
                .addDataSourceProperty("cache_size", "10000")
                .addDataSourceProperty("foreign_keys", "ON")
                .build();
    }

    @Override
    protected String getCurrencyColumnType() {
        return SqlStatements.SQLITE_CURRENCY_TYPE;
    }

    @Override
    protected String getPrimaryKeyType() {
        return SqlStatements.SQLITE_PK_TYPE;
    }

    @Override
    protected String getUpsertTemplate() {
        return SqlStatements.UPSERT_SQLITE;
    }

    @Override
    protected String getAddColumnTemplate() {
        return SqlStatements.ALTER_TABLE_ADD_COLUMN;
    }

    @Override
    protected Function<List<String>, String> getUpdateClauseBuilder() {
        return PreparedStatementBuilder.UpsertBuilder::noUpdateClause;
    }

    @Override
    protected String getNameColumnType() {
        return SqlStatements.SQLITE_NAME_TYPE;
    }

    @Override
    protected String getCreateNameIndexSql() {
        return SqlStatements.CREATE_NAME_INDEX_SQLITE;
    }

    @Override
    protected String getAddNameColumnTemplate() {
        return SqlStatements.ALTER_TABLE_ADD_NAME_COLUMN;
    }
}

