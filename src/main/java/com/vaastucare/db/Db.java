package com.vaastucare.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Small JDBC helper: one shared connection (re-opened if it drops), PreparedStatement helpers
 * and transaction support. The desktop app uses it from the Swing thread only.
 */
public final class Db {
    private static AppConfig config;
    private static Connection conn;
    private static int depth;

    private Db() {}

    @FunctionalInterface public interface Binder { void bind(PreparedStatement ps) throws SQLException; }
    @FunctionalInterface public interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }

    public static synchronized void init(AppConfig cfg) {
        config = cfg;
        close();
        connection();   // fail early with a clear message
    }

    public static synchronized void close() {
        if (conn != null) { try { conn.close(); } catch (SQLException ignored) { } }
        conn = null;
        depth = 0;
    }

    private static Connection connection() {
        try {
            if (config == null) throw new DataAccessException("Database is not configured.");
            if (conn == null || conn.isClosed() || (depth == 0 && !conn.isValid(2))) {
                conn = DriverManager.getConnection(config.url, config.user, config.password);
                conn.setAutoCommit(true);
                depth = 0;
            }
            return conn;
        } catch (SQLException e) {
            throw wrap("Cannot connect to the MySQL database. Check that MySQL is running and that config/db.properties is correct.", e);
        }
    }

    public static synchronized <T> List<T> query(String sql, Binder binder, RowMapper<T> mapper) {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(mapper.map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw wrap("Database query failed.", e);
        }
    }

    public static <T> T queryOne(String sql, Binder binder, RowMapper<T> mapper) {
        List<T> list = query(sql, binder, mapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public static synchronized int update(String sql, Binder binder) {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw wrap("Database update failed.", e);
        }
    }

    /** Executes an INSERT and returns the generated auto-increment id. */
    public static synchronized int insert(String sql, Binder binder) {
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (binder != null) binder.bind(ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new DataAccessException("Insert did not return an id.");
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw wrap("Database insert failed.", e);
        }
    }

    /** Runs the work in one transaction; nested calls join the outer transaction. */
    public static synchronized void transaction(Runnable work) {
        if (depth > 0) { work.run(); return; }
        Connection c = connection();
        try {
            c.setAutoCommit(false);
            depth++;
            work.run();
            c.commit();
        } catch (RuntimeException e) {
            rollback(c);
            throw e;
        } catch (SQLException e) {
            rollback(c);
            throw wrap("Could not save your changes.", e);
        } finally {
            depth--;
            try { c.setAutoCommit(true); } catch (SQLException ignored) { }
        }
    }

    private static void rollback(Connection c) {
        try { c.rollback(); } catch (SQLException ignored) { }
    }

    private static DataAccessException wrap(String message, SQLException e) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        if (state.startsWith("08") || e instanceof SQLNonTransientConnectionException)
            return new DataAccessException("Lost connection to the MySQL database. Make sure MySQL is running, then try again.", e);
        if (state.startsWith("23"))
            return new DataAccessException("That data conflicts with an existing record (duplicate or missing related item).", e);
        if (state.equals("42S02") || state.equals("42S22") || state.equals("3D000"))
            return new DataAccessException("The database tables are missing or outdated. Run the database setup (see README).", e);
        return new DataAccessException(message + " (" + e.getMessage() + ")", e);
    }
}
