package com.vaastucare.data.jdbc;

import com.vaastucare.db.Db;
import com.vaastucare.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** users + providers tables. Every query uses PreparedStatement parameters. */
final class UserDao {
    private static final String SELECT =
        "SELECT u.user_id, u.role, u.full_name, u.email, u.username, u.password_hash, u.phone, u.city, "
        + "c.name AS category_name, p.service_area, p.experience_years, p.description, p.availability, "
        + "p.approval_status, p.rejection_reason, p.is_suspended, p.applied_on "
        + "FROM users u LEFT JOIN providers p ON p.user_id = u.user_id "
        + "LEFT JOIN categories c ON c.category_id = p.category_id ";

    private static User map(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        String id = Ids.user(rs.getInt("user_id"));
        if (role == Role.PROVIDER) {
            Provider p = new Provider(id, rs.getString("full_name"), rs.getString("email"), rs.getString("username"),
                rs.getString("password_hash"), rs.getString("phone"), rs.getString("city"), rs.getString("category_name"),
                rs.getString("service_area"), rs.getInt("experience_years"), rs.getString("description"),
                rs.getString("availability"), rs.getString("approval_status"));
            p.rejectionReason = rs.getString("rejection_reason");
            p.suspended = rs.getBoolean("is_suspended");
            p.appliedOn = rs.getObject("applied_on", LocalDate.class);
            return p;
        }
        return new User(id, role, rs.getString("full_name"), rs.getString("email"), rs.getString("username"),
            rs.getString("password_hash"), rs.getString("phone"), rs.getString("city"));
    }

    User findByLogin(String login) {
        return Db.queryOne(SELECT + "WHERE u.email = ? OR u.username = ? LIMIT 1",
            ps -> { ps.setString(1, login); ps.setString(2, login); }, UserDao::map);
    }

    boolean identifierTaken(String value) {
        Integer n = Db.queryOne("SELECT COUNT(*) FROM users WHERE email = ? OR username = ?",
            ps -> { ps.setString(1, value); ps.setString(2, value); }, rs -> rs.getInt(1));
        return n != null && n > 0;
    }

    User get(String id) {
        return Db.queryOne(SELECT + "WHERE u.user_id = ?", ps -> ps.setInt(1, Ids.num(id)), UserDao::map);
    }

    List<User> all() { return Db.query(SELECT + "ORDER BY u.user_id", null, UserDao::map); }

    List<User> owners() { return Db.query(SELECT + "WHERE u.role = 'OWNER' ORDER BY u.user_id", null, UserDao::map); }

    List<Provider> providers() {
        return Db.query(SELECT + "WHERE u.role = 'PROVIDER' ORDER BY u.user_id", null, rs -> (Provider) map(rs));
    }

    Provider provider(String id) {
        User u = get(id);
        return u instanceof Provider p ? p : null;
    }

    void add(User u) {
        Db.transaction(() -> {
            int id = Db.insert("INSERT INTO users (role, full_name, email, username, password_hash, phone, city) VALUES (?, ?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setString(1, u.role.name()); ps.setString(2, u.name); ps.setString(3, u.email);
                    ps.setString(4, u.username); ps.setString(5, u.password); ps.setString(6, u.phone); ps.setString(7, u.city);
                });
            if (u instanceof Provider p) {
                Db.update("INSERT INTO providers (user_id, category_id, service_area, experience_years, description, availability, "
                    + "approval_status, rejection_reason, is_suspended, applied_on) "
                    + "VALUES (?, (SELECT category_id FROM categories WHERE name = ?), ?, ?, ?, ?, ?, ?, ?, ?)", ps -> {
                        ps.setInt(1, id); ps.setString(2, p.category); ps.setString(3, p.area); ps.setInt(4, p.experienceYears);
                        ps.setString(5, p.description); ps.setString(6, p.availability); ps.setString(7, p.approval);
                        ps.setString(8, p.rejectionReason == null ? "" : p.rejectionReason); ps.setBoolean(9, p.suspended);
                        ps.setObject(10, p.appliedOn);
                    });
            }
            u.id = Ids.user(id);
        });
    }

    void saveProvider(Provider p) {
        int id = Ids.num(p.id);
        Db.transaction(() -> {
            Db.update("UPDATE users SET full_name = ?, phone = ?, city = ? WHERE user_id = ?",
                ps -> { ps.setString(1, p.name); ps.setString(2, p.phone); ps.setString(3, p.city); ps.setInt(4, id); });
            Db.update("UPDATE providers SET category_id = (SELECT category_id FROM categories WHERE name = ?), service_area = ?, "
                + "experience_years = ?, description = ?, availability = ?, approval_status = ?, rejection_reason = ?, is_suspended = ? "
                + "WHERE user_id = ?", ps -> {
                    ps.setString(1, p.category); ps.setString(2, p.area); ps.setInt(3, p.experienceYears);
                    ps.setString(4, p.description); ps.setString(5, p.availability); ps.setString(6, p.approval);
                    ps.setString(7, p.rejectionReason == null ? "" : p.rejectionReason); ps.setBoolean(8, p.suspended); ps.setInt(9, id);
                });
        });
    }
}
