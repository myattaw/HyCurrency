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

package com.hymines.currency.storage.impl.sql;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.storage.sql.ConnectionPool;
import com.hymines.currency.storage.sql.SqlStatements;

public class MySQLStorage extends JDBCStorage {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MySQLStorage(HyCurrencyPlugin plugin, String host, int port, String database,
                        String username, String password) {
        super(plugin);
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    protected ConnectionPool createConnectionPool() {
        String url = SqlStatements.MYSQL_URL
                .replace("{host}", host)
                .replace("{port}", String.valueOf(port))
                .replace("{database}", database);

        return ConnectionPool.builder()
                .poolName("HyCurrency-MySQL")
                .jdbcUrl(url)
                .driverClassName(SqlStatements.MYSQL_DRIVER)
                .username(username)
                .password(password)
                .maximumPoolSize(10)
                .minimumIdle(10)
                // MySQL-specific optimizations
                .addDataSourceProperty("cachePrepStmts", "true")
                .addDataSourceProperty("prepStmtCacheSize", "250")
                .addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                .addDataSourceProperty("useServerPrepStmts", "true")
                .addDataSourceProperty("useLocalSessionState", "true")
                .addDataSourceProperty("rewriteBatchedStatements", "true")
                .addDataSourceProperty("cacheResultSetMetadata", "true")
                .addDataSourceProperty("cacheServerConfiguration", "true")
                .addDataSourceProperty("elideSetAutoCommits", "true")
                .addDataSourceProperty("maintainTimeStats", "false")
                .build();
    }

    @Override
    protected String getCurrencyColumnType() {
        return SqlStatements.MYSQL_CURRENCY_TYPE;
    }

    @Override
    protected String getPrimaryKeyType() {
        return SqlStatements.MYSQL_PK_TYPE;
    }

    @Override
    protected String getUpsertTemplate() {
        return SqlStatements.UPSERT_MYSQL;
    }

    @Override
    protected String getAddColumnTemplate() {
        return SqlStatements.ALTER_TABLE_ADD_COLUMN;
    }

    @Override
    protected String getNameColumnType() {
        return SqlStatements.MYSQL_NAME_TYPE;
    }

    @Override
    protected String getCreateNameIndexSql() {
        return SqlStatements.CREATE_NAME_INDEX_MYSQL;
    }

    @Override
    protected String getAddNameColumnTemplate() {
        return SqlStatements.ALTER_TABLE_ADD_NAME_COLUMN;
    }
}

