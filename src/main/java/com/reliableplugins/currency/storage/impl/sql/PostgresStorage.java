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

import java.util.List;
import java.util.function.Function;

public class PostgresStorage extends JDBCStorage {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public PostgresStorage(HyCurrencyPlugin plugin, String host, int port, String database,
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
        String url = SqlStatements.POSTGRES_URL
                .replace("{host}", host)
                .replace("{port}", String.valueOf(port))
                .replace("{database}", database);

        //TODO: make these configurable
        return ConnectionPool.builder()
                .poolName("HyCurrency-PostgreSQL")
                .jdbcUrl(url)
                .driverClassName(SqlStatements.POSTGRES_DRIVER)
                .username(username)
                .password(password)
                .maximumPoolSize(10)
                .minimumIdle(10)
                .build();
    }

    @Override
    protected String getCurrencyColumnType() {
        return SqlStatements.POSTGRES_CURRENCY_TYPE;
    }

    @Override
    protected String getPrimaryKeyType() {
        return SqlStatements.POSTGRES_PK_TYPE;
    }

    @Override
    protected String getUpsertTemplate() {
        return SqlStatements.UPSERT_POSTGRES;
    }

    @Override
    protected String getAddColumnTemplate() {
        return SqlStatements.ALTER_TABLE_ADD_COLUMN_IF_NOT_EXISTS;
    }

    @Override
    protected Function<List<String>, String> getUpdateClauseBuilder() {
        return PreparedStatementBuilder.UpsertBuilder::postgresUpdateClause;
    }

    @Override
    protected String getNameColumnType() {
        return SqlStatements.POSTGRES_NAME_TYPE;
    }

    @Override
    protected String getCreateNameIndexSql() {
        return SqlStatements.CREATE_NAME_INDEX_POSTGRES;
    }

    @Override
    protected String getAddNameColumnTemplate() {
        return SqlStatements.ALTER_TABLE_ADD_NAME_COLUMN_IF_NOT_EXISTS;
    }
}

