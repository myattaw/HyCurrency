package com.hymines.currency.storage.sql;

import com.hymines.currency.HyCurrencyPlugin;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

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
        // PostgreSQL supports IF NOT EXISTS for columns
        return SqlStatements.ALTER_TABLE_ADD_COLUMN_IF_NOT_EXISTS;
    }

    @Override
    protected String processUpsertTemplate(String columns, String values, String updates) {
        // PostgreSQL uses EXCLUDED instead of VALUES() for conflict resolution
        StringBuilder setClause = new StringBuilder();
        String[] cols = columns.split(", ");
        for (int i = 1; i < cols.length; i++) { // Skip player_uuid
            if (setClause.length() > 0) setClause.append(", ");
            setClause.append(cols[i]).append(" = EXCLUDED.").append(cols[i]);
        }

        return SqlStatements.UPSERT_POSTGRES
                .replace("{table}", tableName)
                .replace("{columns}", columns)
                .replace("{values}", values)
                .replace("{updates}", setClause.toString());
    }

    @Override
    protected void addUpsertParameters(PreparedStatement stmt, int startIndex, Map<String, BigDecimal> currencies) throws SQLException {
        // PostgreSQL ON CONFLICT uses EXCLUDED, no extra parameters needed
    }
}
