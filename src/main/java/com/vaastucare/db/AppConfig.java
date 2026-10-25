package com.vaastucare.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database settings. Order of precedence: environment variables VAASTU_DB_URL / VAASTU_DB_USER /
 * VAASTU_DB_PASSWORD, then config/db.properties (copy it from config/db.properties.example).
 */
public final class AppConfig {
    public final String url, user, password;

    private AppConfig(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }

    public static AppConfig load() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("config/db.properties")) {
            p.load(in);
        } catch (IOException ignored) { /* file is optional when environment variables are set */ }
        String url = pick("VAASTU_DB_URL", p.getProperty("db.url"));
        String user = pick("VAASTU_DB_USER", p.getProperty("db.user"));
        String pass = pick("VAASTU_DB_PASSWORD", p.getProperty("db.password"));
        if (url == null || user == null)
            throw new DataAccessException("Database settings not found. Copy config/db.properties.example to config/db.properties "
                + "and enter your MySQL user and password.");
        return new AppConfig(url.trim(), user.trim(), pass == null ? "" : pass);
    }

    private static String pick(String env, String fallback) {
        String v = System.getenv(env);
        return v != null && !v.isEmpty() ? v : fallback;
    }

    /** Name of the database in the URL, e.g. vaastu_care. */
    public String databaseName() {
        String rest = url.substring(url.indexOf("//") + 2);
        int slash = rest.indexOf('/');
        if (slash < 0) throw new DataAccessException("db.url must include the database name, e.g. jdbc:mysql://localhost:3306/vaastu_care");
        String db = rest.substring(slash + 1);
        int q = db.indexOf('?');
        return q >= 0 ? db.substring(0, q) : db;
    }

    /** Same URL without the database name (used to create the database). */
    public String serverUrl() {
        String rest = url.substring(url.indexOf("//") + 2);
        int slash = rest.indexOf('/');
        String hostPart = url.substring(0, url.indexOf("//") + 2) + rest.substring(0, slash);
        int q = rest.indexOf('?');
        return hostPart + "/" + (q >= 0 ? rest.substring(q) : "");
    }
}
