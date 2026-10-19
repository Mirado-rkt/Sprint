package mg.itu.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Reusable prepared-statement CRUD primitives; concrete DAOs supply SQL and row mapping. */
public abstract class GenericDAO<T> {
    private final RowMapper<T> rowMapper;

    protected GenericDAO(RowMapper<T> rowMapper) { this.rowMapper = rowMapper; }

    protected List<T> query(String sql, Object... parameters) throws SQLException {
        try (Connection connection = DbConnection.open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet rows = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rows.next()) results.add(rowMapper.map(rows));
                return results;
            }
        }
    }

    protected int insert(String sql, Object... parameters) throws SQLException { return execute(sql, parameters); }
    protected int update(String sql, Object... parameters) throws SQLException { return execute(sql, parameters); }
    protected int delete(String sql, Object... parameters) throws SQLException { return execute(sql, parameters); }

    private int execute(String sql, Object... parameters) throws SQLException {
        try (Connection connection = DbConnection.open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            return statement.executeUpdate();
        }
    }

    private void bind(PreparedStatement statement, Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
    }
}
