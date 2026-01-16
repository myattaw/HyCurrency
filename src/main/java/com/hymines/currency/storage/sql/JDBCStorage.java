package com.hymines.currency.storage.sql;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;
import com.hymines.currency.storage.CurrencyStorage;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class JDBCStorage implements CurrencyStorage {

    protected final HyCurrencyPlugin plugin;
    protected Connection connection;
    protected final String tableName;

    public JDBCStorage(HyCurrencyPlugin plugin) {
        this.plugin = plugin;
        this.tableName = "player_currencies";
    }

    /**
     * Open the database connection
     */
    public abstract boolean openConnection();

    /**
     * Get the SQL type for currency columns
     */
    protected abstract String getCurrencyColumnType();

    /**
     * Get the SQL type for the primary key (player UUID)
     */
    protected abstract String getPrimaryKeyType();

    /**
     * Get the database-specific upsert SQL template
     */
    protected abstract String getUpsertTemplate();

    /**
     * Get the database-specific add column SQL template
     */
    protected abstract String getAddColumnTemplate();

    /**
     * Process a SQL template by replacing placeholders
     */
    protected String processTemplate(String template) {
        return template.replace("{table}", tableName);
    }

    /**
     * Process a SQL template with column information
     */
    protected String processTemplate(String template, String columnName, String columnType) {
        return template
                .replace("{table}", tableName)
                .replace("{column}", columnName)
                .replace("{type}", columnType);
    }

    /**
     * Process a SQL template with upsert information
     */
    protected String processUpsertTemplate(String columns, String values, String updates) {
        return getUpsertTemplate()
                .replace("{table}", tableName)
                .replace("{columns}", columns)
                .replace("{values}", values)
                .replace("{updates}", updates);
    }

    /**
     * Check if the connection is still valid
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get the active database connection, reconnecting if necessary
     */
    public Connection getConnection() {
        if (!isConnectionValid()) {
            boolean opened = openConnection();
            if (!opened) {
                return null;
            }
        }
        return connection;
    }

    @Override
    public void initialize() {
        boolean opened = openConnection();
        if (!opened || connection == null) {
            plugin.getLogger().atSevere().log("Failed to open database connection during storage initialization");
            throw new RuntimeException("Failed to open database connection");
        }
        createTableIfNotExists();
        syncCurrencyColumns();
    }

    protected void createTableIfNotExists() {
        Connection conn = getConnection();
        if (conn == null) {
            plugin.getLogger().atWarning().log("No DB connection available, skipping table creation for " + tableName);
            return;
        }

        String sql = SqlStatements.CREATE_TABLE
                .replace("{table}", tableName)
                .replace("{pk_type}", getPrimaryKeyType());

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to create table: " + e.getMessage());
        }
    }

    protected void syncCurrencyColumns() {
        var currencies = plugin.getCurrencyConfig().getCurrencies();
        if (currencies == null) return;

        for (String currencyId : currencies.keySet()) {
            addCurrencyColumn(currencyId);
        }
    }

    protected void addCurrencyColumn(String currencyId) {
        Connection conn = getConnection();
        if (conn == null) {
            plugin.getLogger().atFine().log("No DB connection; skipping addCurrencyColumn for " + currencyId);
            return;
        }

        String columnName = sanitizeColumnName(currencyId);
        String sql = processTemplate(getAddColumnTemplate(), columnName, getCurrencyColumnType());

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (!msg.contains("duplicate") && !msg.contains("already exists") && !msg.contains("duplicate column")) {
                plugin.getLogger().atFine().log("Column " + columnName + " may already exist: " + e.getMessage());
            }
        }
    }

    protected String sanitizeColumnName(String currencyId) {
        return "currency_" + currencyId.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    @Override
    public CompletableFuture<CurrencyModel> loadAsync(String playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            CurrencyModel model = loadFromStorage(playerUuid);
            plugin.getCurrencyDataMap().put(playerUuid, model);
            return model;
        }, plugin.getDbExecutor());
    }

    protected CurrencyModel loadFromStorage(String playerUuid) {
        CurrencyModel model = new CurrencyModel();
        var currencies = plugin.getCurrencyConfig().getCurrencies();
        if (currencies == null || currencies.isEmpty()) {
            return model;
        }

        StringBuilder columns = new StringBuilder();
        for (String currencyId : currencies.keySet()) {
            if (columns.length() > 0) columns.append(", ");
            columns.append(sanitizeColumnName(currencyId));
        }

        String sql = SqlStatements.SELECT_PLAYER_CURRENCIES
                .replace("{table}", tableName)
                .replace("{columns}", columns.toString());

        Connection conn = getConnection();
        if (conn == null) {
            plugin.getLogger().atWarning().log("No DB connection; loading defaults for player " + playerUuid);
            for (String currencyId : currencies.keySet()) {
                model.addCurrency(currencyId);
            }
            return model;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                for (String currencyId : currencies.keySet()) {
                    BigDecimal amount = rs.getBigDecimal(sanitizeColumnName(currencyId));
                    if (amount != null) {
                        model.setCurrency(currencyId, amount);
                    }
                }
            } else {
                for (String currencyId : currencies.keySet()) {
                    model.addCurrency(currencyId);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to load player data: " + e.getMessage());
        }
        return model;
    }

    @Override
    public CompletableFuture<Void> saveAsync(String playerId, CurrencyModel model) {
        return CompletableFuture.runAsync(() -> save(playerId, model), plugin.getDbExecutor());
    }

    protected void save(String playerId, CurrencyModel model) {
        Connection conn = getConnection();
        if (conn == null) {
            plugin.getLogger().atWarning().log("No DB connection; skipping save for player " + playerId);
            return;
        }

        var currencies = model.getCurrencies();
        if (currencies.isEmpty()) return;

        StringBuilder columns = new StringBuilder("player_uuid");
        StringBuilder values = new StringBuilder("?");
        StringBuilder updates = new StringBuilder();

        for (String currencyId : currencies.keySet()) {
            String columnName = sanitizeColumnName(currencyId);
            columns.append(", ").append(columnName);
            values.append(", ?");
            if (updates.length() > 0) updates.append(", ");
            updates.append(columnName).append(" = VALUES(").append(columnName).append(")");
        }

        String sql = processUpsertTemplate(columns.toString(), values.toString(), updates.toString());

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId);
            int index = 2;
            for (BigDecimal amount : currencies.values()) {
                stmt.setBigDecimal(index++, amount);
            }
            addUpsertParameters(stmt, index, currencies);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to save player data: " + e.getMessage());
        }
    }

    /**
     * Add any additional parameters needed for upsert (database-specific)
     */
    protected abstract void addUpsertParameters(PreparedStatement stmt, int startIndex, Map<String, BigDecimal> currencies) throws SQLException;

    @Override
    public void saveAll() {
        for (Map.Entry<String, CurrencyModel> entry : plugin.getCurrencyDataMap().entrySet()) {
            save(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addCurrency(String currencyId) {
        addCurrencyColumn(currencyId);
    }

    @Override
    public void removeCurrency(String currencyId, boolean deleteData) {
        if (deleteData) {
            Connection conn = getConnection();
            if (conn == null) {
                plugin.getLogger().atWarning().log("No DB connection; skipping removeCurrency for " + currencyId);
                return;
            }
            String columnName = sanitizeColumnName(currencyId);
            String sql = SqlStatements.ALTER_TABLE_DROP_COLUMN
                    .replace("{table}", tableName)
                    .replace("{column}", columnName);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                plugin.getLogger().atSevere().log("Failed to remove currency column: " + e.getMessage());
            }
        }
    }

    @Override
    public void unload() {
        saveAll();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to close connection: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Map<String, Integer>> getTopBalances(String currencyId, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Integer> results = new LinkedHashMap<>();
            String columnName = sanitizeColumnName(currencyId);
            String sql = SqlStatements.SELECT_TOP_BALANCES
                    .replace("{table}", tableName)
                    .replace("{column}", columnName);

            Connection conn = getConnection();
            if (conn == null) {
                plugin.getLogger().atWarning().log("No DB connection; returning empty leaderboard for " + currencyId);
                return results;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String uuid = rs.getString("player_uuid");
                    int amount = rs.getBigDecimal(columnName).intValue();
                    results.put(uuid, amount);
                }
            } catch (SQLException e) {
                plugin.getLogger().atSevere().log("Failed to get top balances: " + e.getMessage());
            }
            return results;
        }, plugin.getDbExecutor());
    }
}
