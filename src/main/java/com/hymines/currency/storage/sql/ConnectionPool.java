package com.hymines.currency.storage.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages HikariCP connection pool for database operations.
 */
public class ConnectionPool {

    private final HikariDataSource dataSource;

    private ConnectionPool(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Get a connection from the pool.
     * Remember to close the connection when done to return it to the pool.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Check if the pool is running and healthy.
     */
    public boolean isRunning() {
        return dataSource != null && dataSource.isRunning() && !dataSource.isClosed();
    }

    /**
     * Close the connection pool and release all resources.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Get pool statistics for monitoring.
     */
    public String getPoolStats() {
        if (dataSource == null) return "Pool not initialized";
        return String.format("Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }

    /**
     * Builder for creating ConnectionPool instances with HikariCP configuration.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final HikariConfig config = new HikariConfig();

        private Builder() {
            // Set sensible defaults
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000); // 5 minutes
            config.setConnectionTimeout(10000); // 10 seconds
            config.setMaxLifetime(1800000); // 30 minutes
            config.setLeakDetectionThreshold(60000); // 1 minute
        }

        public Builder jdbcUrl(String url) {
            config.setJdbcUrl(url);
            return this;
        }

        public Builder username(String username) {
            config.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            config.setPassword(password);
            return this;
        }

        public Builder driverClassName(String driverClassName) {
            config.setDriverClassName(driverClassName);
            return this;
        }

        public Builder poolName(String poolName) {
            config.setPoolName(poolName);
            return this;
        }

        public Builder maximumPoolSize(int size) {
            config.setMaximumPoolSize(size);
            return this;
        }

        public Builder minimumIdle(int minIdle) {
            config.setMinimumIdle(minIdle);
            return this;
        }

        public Builder connectionTimeout(long timeoutMs) {
            config.setConnectionTimeout(timeoutMs);
            return this;
        }

        public Builder idleTimeout(long timeoutMs) {
            config.setIdleTimeout(timeoutMs);
            return this;
        }

        public Builder maxLifetime(long lifetimeMs) {
            config.setMaxLifetime(lifetimeMs);
            return this;
        }

        public Builder addDataSourceProperty(String name, Object value) {
            config.addDataSourceProperty(name, value);
            return this;
        }

        public ConnectionPool build() {
            return new ConnectionPool(new HikariDataSource(config));
        }
    }
}
