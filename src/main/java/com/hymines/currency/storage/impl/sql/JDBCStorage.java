package com.hymines.currency.storage.impl.sql;

import com.hymines.currency.HyCurrencyPlugin;
import com.hymines.currency.model.CurrencyModel;
import com.hymines.currency.storage.CurrencyStorage;
import com.hymines.currency.storage.sql.ConnectionPool;
import com.hymines.currency.storage.sql.PreparedStatementBuilder;
import com.hymines.currency.storage.sql.SqlStatements;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class JDBCStorage implements CurrencyStorage {

    protected final HyCurrencyPlugin plugin;
    protected ConnectionPool connectionPool;
    protected final String tableName;

    public JDBCStorage(HyCurrencyPlugin plugin) {
        this.plugin = plugin;
        this.tableName = "player_currencies";
    }

    /**
     * Create and configure the connection pool for this database type.
     */
    protected abstract ConnectionPool createConnectionPool();

    /**
     * Get the SQL type for currency columns.
     */
    protected abstract String getCurrencyColumnType();

    /**
     * Get the SQL type for the primary key (player UUID).
     */
    protected abstract String getPrimaryKeyType();

    /**
     * Get the database-specific upsert SQL template.
     */
    protected abstract String getUpsertTemplate();

    /**
     * Get the database-specific add column SQL template.
     */
    protected abstract String getAddColumnTemplate();

    /**
     * Get the update clause builder function for this database type.
     */
    protected Function<List<String>, String> getUpdateClauseBuilder() {
        return PreparedStatementBuilder.UpsertBuilder::mysqlUpdateClause;
    }

    /**
     * Get a connection from the pool.
     */
    protected Connection getConnection() throws SQLException {
        if (connectionPool == null || !connectionPool.isRunning()) {
            throw new SQLException("Connection pool is not available");
        }
        return connectionPool.getConnection();
    }

    @Override
    public void initialize() {
        try {
            this.connectionPool = createConnectionPool();
            if (connectionPool == null || !connectionPool.isRunning()) {
                throw new RuntimeException("Failed to create connection pool");
            }
            plugin.getLogger().atInfo().log("Database connection pool initialized");
            createTableIfNotExists();
            syncCurrencyColumns();
        } catch (Exception e) {
            plugin.getLogger().atSevere().log("Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    protected void createTableIfNotExists() {
        String sql = SqlStatements.CREATE_TABLE
                .replace("{table}", tableName)
                .replace("{pk_type}", getPrimaryKeyType());

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to create table: " + e.getMessage());
        }
    }

    protected void syncCurrencyColumns() {
        var currencies = plugin.getCurrencyConfig().getCurrencies();
        if (currencies == null) return;
        currencies.keySet().forEach(this::addCurrencyColumn);
    }

    protected void addCurrencyColumn(String currencyId) {
        String columnName = sanitizeColumnName(currencyId);
        String sql = getAddColumnTemplate()
                .replace("{table}", tableName)
                .replace("{column}", columnName)
                .replace("{type}", getCurrencyColumnType());

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (!msg.contains("duplicate") && !msg.contains("already exists")) {
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
        if (currencies == null || currencies.isEmpty()) return model;

        Set<String> currencyIds = currencies.keySet();
        String columns = buildColumnList(currencyIds);
        String sql = SqlStatements.SELECT_PLAYER_CURRENCIES
                .replace("{table}", tableName)
                .replace("{columns}", columns);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    for (String currencyId : currencyIds) {
                        BigDecimal amount = rs.getBigDecimal(sanitizeColumnName(currencyId));
                        if (amount != null) {
                            model.setCurrency(currencyId, amount);
                        }
                    }
                } else {
                    currencyIds.forEach(model::addCurrency);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to load player data: " + e.getMessage());
            currencyIds.forEach(model::addCurrency);
        }
        return model;
    }

    private String buildColumnList(Collection<String> currencyIds) {
        StringJoiner joiner = new StringJoiner(", ");
        currencyIds.forEach(id -> joiner.add(sanitizeColumnName(id)));
        return joiner.toString();
    }

    @Override
    public CompletableFuture<Void> saveAsync(String playerId, CurrencyModel model) {
        return CompletableFuture.runAsync(() -> save(playerId, model), plugin.getDbExecutor());
    }

    protected void save(String playerId, CurrencyModel model) {
        if (model.getCurrencies().isEmpty()) return;

        String sql = buildUpsertSql(model.getCurrencies().keySet());

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setUpsertParameters(stmt, playerId, model.getCurrencies());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to save player data: " + e.getMessage());
        }
    }

    private String buildUpsertSql(Collection<String> currencyIds) {
        return PreparedStatementBuilder.upsert(tableName)
                .withPrimaryKey("player_uuid")
                .withColumns(currencyIds, this::sanitizeColumnName)
                .withTemplate(getUpsertTemplate())
                .buildSql(getUpdateClauseBuilder());
    }

    private void setUpsertParameters(PreparedStatement stmt, String playerId,
                                     Map<String, BigDecimal> currencies) throws SQLException {
        stmt.setString(1, playerId);
        int index = 2;
        for (BigDecimal amount : currencies.values()) {
            stmt.setBigDecimal(index++, amount);
        }
    }

    @Override
    public void saveAll() {
        saveAll(plugin.getCurrencyDataMap());
    }

    private void saveAll(Map<String, CurrencyModel> allPlayersData) {
        if (allPlayersData.isEmpty()) return;

        Set<String> allCurrencyIds = collectAllCurrencyIds(allPlayersData);
        if (allCurrencyIds.isEmpty()) return;

        String sql = buildUpsertSql(allCurrencyIds);
        List<String> currencyOrder = new ArrayList<>(allCurrencyIds);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, CurrencyModel> entry : allPlayersData.entrySet()) {
                addBatchEntry(stmt, entry.getKey(), entry.getValue().getCurrencies(), currencyOrder);
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to bulk save player data: " + e.getMessage());
        }
    }

    private Set<String> collectAllCurrencyIds(Map<String, CurrencyModel> allPlayersData) {
        Set<String> ids = new LinkedHashSet<>();
        allPlayersData.values().forEach(model -> ids.addAll(model.getCurrencies().keySet()));
        return ids;
    }

    private void addBatchEntry(PreparedStatement stmt, String playerId,
                               Map<String, BigDecimal> currencies,
                               List<String> currencyOrder) throws SQLException {
        stmt.setString(1, playerId);
        int index = 2;
        for (String currencyId : currencyOrder) {
            stmt.setBigDecimal(index++, currencies.getOrDefault(currencyId, BigDecimal.ZERO));
        }
        stmt.addBatch();
    }

    @Override
    public void addCurrency(String currencyId) {
        addCurrencyColumn(currencyId);
    }

    @Override
    public void removeCurrency(String currencyId, boolean deleteData) {
        if (!deleteData) return;

        String sql = SqlStatements.ALTER_TABLE_DROP_COLUMN
                .replace("{table}", tableName)
                .replace("{column}", sanitizeColumnName(currencyId));

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().atSevere().log("Failed to remove currency column: " + e.getMessage());
        }
    }

    @Override
    public void unload() {
        saveAll();
        if (connectionPool != null) {
            plugin.getLogger().atInfo().log("Closing database connection pool. Stats: " + connectionPool.getPoolStats());
            connectionPool.close();
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

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.put(rs.getString("player_uuid"), rs.getBigDecimal(columnName).intValue());
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().atSevere().log("Failed to get top balances: " + e.getMessage());
            }
            return results;
        }, plugin.getDbExecutor());
    }
}
