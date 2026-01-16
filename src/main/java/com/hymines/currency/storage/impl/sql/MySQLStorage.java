package com.hymines.currency.storage.impl.sql;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.storage.sql.SqlStatements;

import java.sql.DriverManager;
import java.sql.SQLException;

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
            plugin.getLogger().atSevere().log("MySQL driver not found: " + SqlStatements.MYSQL_DRIVER);
            return false;
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to connect to MySQL: " + e.getMessage());
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
        return SqlStatements.ALTER_TABLE_ADD_COLUMN;
    }

}
