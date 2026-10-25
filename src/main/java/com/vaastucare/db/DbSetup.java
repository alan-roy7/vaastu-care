package com.vaastucare.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database setup tool. Run it from the project folder:
 *   DbSetup            creates the database and tables from database/schema.sql
 *   DbSetup --seed     same, then adds the sample accounts and data if the database is empty
 *   DbSetup --reset    DELETES the database, recreates it and adds the sample data again
 */
public class DbSetup {
    public static void main(String[] args) {
        boolean reset = false, seed = false;
        for (String a : args) { if (a.equals("--reset")) reset = true; if (a.equals("--seed")) seed = true; }
        try {
            AppConfig cfg = AppConfig.load();
            String db = cfg.databaseName();
            if (!db.matches("[A-Za-z0-9_]+")) throw new DataAccessException("Database name may only contain letters, digits and underscores.");
            try (Connection c = DriverManager.getConnection(cfg.serverUrl(), cfg.user, cfg.password); Statement st = c.createStatement()) {
                if (reset) {
                    st.execute("DROP DATABASE IF EXISTS " + db);
                    System.out.println("Dropped database " + db + ".");
                }
                int n = 0;
                for (String sql : readStatements(db)) { st.execute(sql); n++; }
                System.out.println("Schema ready: ran " + n + " statements from database/schema.sql.");
            }
            if (seed || reset) SampleData.seedIfEmpty(cfg);
            System.out.println("Done.");
        } catch (SQLException | IOException | DataAccessException e) {
            System.err.println("SETUP FAILED: " + e.getMessage());
            System.err.println("Check that MySQL is running and that config/db.properties has the right user and password.");
            System.exit(1);
        }
    }

    /** Reads database/schema.sql and splits it into single statements (comments removed). */
    private static List<String> readStatements(String dbName) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String line : Files.readAllLines(Paths.get("database", "schema.sql"), StandardCharsets.UTF_8))
            if (!line.trim().startsWith("--")) sb.append(line).append('\n');
        List<String> out = new ArrayList<>();
        for (String part : sb.toString().replace("vaastu_care", dbName).split(";"))
            if (!part.trim().isEmpty()) out.add(part.trim());
        return out;
    }
}
