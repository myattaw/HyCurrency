package com.hymines.currency.storage.sql;

import com.hymines.currency.HyCurrencyPlugin;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

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
    public boolean openConnection() {
        try {
            Class.forName(SqlStatements.MYSQL_DRIVER);
            String url = SqlStatements.MYSQL_URL
                    .replace("{host}", host)
                    .replace("{port}", String.valueOf(port))
                    .replace("{database}", database);
            connection = DriverManager.getConnection(url, username, password);
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().atSevere().log("MySQL driver not found: " + SqlStatements.MYSQL_DRIVER + ". Add MySQL Connector/J to the server/plugin classpath or your build dependencies.");
            return false;
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to connect to MySQL (check host/port/database/credentials): " + e.getMessage());
            return false;
        }
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
        // MySQL doesn't support IF NOT EXISTS for columns
        return SqlStatements.ALTER_TABLE_ADD_COLUMN;
    }

    @Override
    protected void addUpsertParameters(PreparedStatement stmt, int startIndex, Map<String, BigDecimal> currencies) throws SQLException {
        // MySQL ON DUPLICATE KEY UPDATE uses VALUES() function, no extra parameters needed
    }

}
