package com.hymines.currency.storage.impl.sql;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.storage.sql.ConnectionPool;
import com.hymines.currency.storage.sql.PreparedStatementBuilder;
import com.hymines.currency.storage.sql.SqlStatements;

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
}
