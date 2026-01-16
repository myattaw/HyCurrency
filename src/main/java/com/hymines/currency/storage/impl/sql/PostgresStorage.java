package com.hymines.currency.storage.impl.sql;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.storage.sql.PreparedStatementBuilder;
import com.hymines.currency.storage.sql.SqlStatements;

import java.sql.DriverManager;
import java.sql.SQLException;
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
    public boolean openConnection() {
        try {
            Class.forName(SqlStatements.POSTGRES_DRIVER);
            String url = SqlStatements.POSTGRES_URL
                    .replace("{host}", host)
                    .replace("{port}", String.valueOf(port))
                    .replace("{database}", database);
            connection = DriverManager.getConnection(url, username, password);
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().atSevere().log("Failed to connect to PostgreSQL: " + e.getMessage());
            return false;
        }
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
