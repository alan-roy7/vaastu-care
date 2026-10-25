package com.vaastucare.data.jdbc;

import com.vaastucare.db.Db;
import com.vaastucare.model.Notification;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

final class NotificationDao {
    List<Notification> byUser(String userId) {
        return Db.query("SELECT notification_id, user_id, message, request_id, created_at, is_read FROM notifications "
            + "WHERE user_id = ? ORDER BY created_at DESC, notification_id DESC", ps -> ps.setInt(1, Ids.num(userId)), rs -> {
                int req = rs.getInt("request_id");
                String requestId = rs.wasNull() ? null : Ids.request(req);
                return new Notification(Ids.notification(rs.getInt("notification_id")), Ids.user(rs.getInt("user_id")),
                    rs.getString("message"), requestId, rs.getObject("created_at", LocalDateTime.class), rs.getBoolean("is_read"));
            });
    }

    void save(Notification n) {
        int id = Db.insert("INSERT INTO notifications (user_id, message, request_id, created_at, is_read) VALUES (?, ?, ?, ?, ?)", ps -> {
            ps.setInt(1, Ids.num(n.userId)); ps.setString(2, n.text);
            if (n.requestId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, Ids.num(n.requestId));
            ps.setObject(4, n.time); ps.setBoolean(5, n.read);
        });
        n.id = Ids.notification(id);
    }

    void markRead(String id) {
        Db.update("UPDATE notifications SET is_read = TRUE WHERE notification_id = ?", ps -> ps.setInt(1, Ids.num(id)));
    }

    void markAllRead(String userId) {
        Db.update("UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE", ps -> ps.setInt(1, Ids.num(userId)));
    }
}
