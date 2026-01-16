package com.hymines.currency.storage.sql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

/**
 * Builder for creating and executing prepared statements with dynamic columns.
 */
public class PreparedStatementBuilder {

    private final Connection connection;
    private final List<String> columns = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();

    public PreparedStatementBuilder(Connection connection) {
        this.connection = connection;
    }

    public PreparedStatementBuilder addColumn(String column, Object value) {
        columns.add(column);
        values.add(value);
        return this;
    }

    public PreparedStatementBuilder addColumns(Map<String, BigDecimal> columnValues, Function<String, String> columnNameMapper) {
        for (Map.Entry<String, BigDecimal> entry : columnValues.entrySet()) {
            columns.add(columnNameMapper.apply(entry.getKey()));
            values.add(entry.getValue());
        }
        return this;
    }

    public PreparedStatementBuilder addColumnsWithDefault(Collection<String> columnIds, Map<String, BigDecimal> values, Function<String, String> columnNameMapper, BigDecimal defaultValue) {
        for (String id : columnIds) {
            columns.add(columnNameMapper.apply(id));
            this.values.add(values.getOrDefault(id, defaultValue));
        }
        return this;
    }

    public String buildColumnList() {
        return String.join(", ", columns);
    }

    public String buildPlaceholders() {
        return String.join(", ", Collections.nCopies(columns.size(), "?"));
    }

    public String buildMySqlUpdateClause() {
        return UpsertBuilder.mysqlUpdateClause(columns);
    }

    public String buildPostgresUpdateClause() {
        return UpsertBuilder.postgresUpdateClause(columns);
    }

    public PreparedStatement prepare(String sql) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        setParameters(stmt, 1);
        return stmt;
    }

    public void setParameters(PreparedStatement stmt, int startIndex) throws SQLException {
        int index = startIndex;
        for (Object value : values) {
            switch (value) {
                case String s -> stmt.setString(index++, s);
                case BigDecimal bigDecimal -> stmt.setBigDecimal(index++, bigDecimal);
                case Integer i -> stmt.setInt(index++, i);
                case null, default -> stmt.setObject(index++, value);
            }
        }
    }

    public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(values);
    }

    public int size() {
        return columns.size();
    }

    /**
     * Creates an upsert builder for batch operations.
     */
    public static UpsertBuilder upsert(String tableName) {
        return new UpsertBuilder(tableName);
    }

    /**
     * Builder specifically for upsert operations with batch support.
     */
    public static class UpsertBuilder {

        private final String tableName;
        private final List<String> columns = new ArrayList<>();
        private String upsertTemplate;

        private UpsertBuilder(String tableName) {
            this.tableName = tableName;
        }

        public UpsertBuilder withPrimaryKey(String pkColumn) {
            if (columns.isEmpty()) {
                columns.add(pkColumn);
            }
            return this;
        }

        public UpsertBuilder withColumns(Collection<String> currencyIds, Function<String, String> columnNameMapper) {
            for (String id : currencyIds) {
                columns.add(columnNameMapper.apply(id));
            }
            return this;
        }

        public UpsertBuilder withTemplate(String template) {
            this.upsertTemplate = template;
            return this;
        }

        public String buildSql(Function<List<String>, String> updateClauseBuilder) {
            String columnList = String.join(", ", columns);
            String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
            String updates = updateClauseBuilder.apply(columns);

            return upsertTemplate.replace("{table}", tableName).replace("{columns}", columnList).replace("{values}", placeholders).replace("{updates}", updates);
        }

        public List<String> getColumns() {
            return columns;
        }

        public static String mysqlUpdateClause(List<String> columns) {
            StringBuilder updates = new StringBuilder();
            for (int i = 1; i < columns.size(); i++) {
                if (!updates.isEmpty()) updates.append(", ");
                String col = columns.get(i);
                updates.append(col).append(" = VALUES(").append(col).append(")");
            }
            return updates.toString();
        }

        public static String postgresUpdateClause(List<String> columns) {
            StringBuilder updates = new StringBuilder();
            for (int i = 1; i < columns.size(); i++) {
                if (!updates.isEmpty()) updates.append(", ");
                String col = columns.get(i);
                updates.append(col).append(" = EXCLUDED.").append(col);
            }
            return updates.toString();
        }

        public static String noUpdateClause(List<String> columns) {
            return "";
        }
    }

}
