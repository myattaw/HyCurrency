package com.hymines.currency.storage.sql;

import com.hymines.currency.HyCurrencyPlugin;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class SQLiteStorage extends JDBCStorage {

    private final Path databasePath;

    public SQLiteStorage(HyCurrencyPlugin plugin, Path databasePath) {
        super(plugin);
        this.databasePath = databasePath;
    }

    @Override
    public boolean openConnection() {
        try {
            Class.forName(SqlStatements.SQLITE_DRIVER);
            String url = SqlStatements.SQLITE_URL.replace("{path}", databasePath.toAbsolutePath().toString());
            connection = DriverManager.getConnection(url);
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().atSevere().log("Failed to connect to SQLite: " + e.getMessage());
            return false;
        }
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
        // SQLite doesn't support IF NOT EXISTS for columns
        return SqlStatements.ALTER_TABLE_ADD_COLUMN;
    }

    @Override
    protected void addUpsertParameters(PreparedStatement stmt, int startIndex, Map<String, BigDecimal> currencies) throws SQLException {
        // SQLite INSERT OR REPLACE doesn't need extra parameters
    }

}
