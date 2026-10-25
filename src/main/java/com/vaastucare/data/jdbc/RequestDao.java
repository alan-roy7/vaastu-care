package com.vaastucare.data.jdbc;

import com.vaastucare.db.Db;
import com.vaastucare.model.ServiceRequest;
import com.vaastucare.model.StatusEntry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** service_requests, request_status_history and request_photos. History and photos are append-only. */
final class RequestDao {
    private static final String SELECT =
        "SELECT r.request_id, r.property_id, pr.owner_id, r.provider_id, c.name AS category_name, r.description, r.notes, "
        + "r.preferred_date, r.created_on, r.status, r.is_accepted, "
        + "EXISTS(SELECT 1 FROM reviews rv WHERE rv.request_id = r.request_id) AS reviewed "
        + "FROM service_requests r JOIN properties pr ON pr.property_id = r.property_id "
        + "JOIN categories c ON c.category_id = r.category_id ";

    private static ServiceRequest map(ResultSet rs) throws SQLException {
        ServiceRequest r = new ServiceRequest();
        r.id = Ids.request(rs.getInt("request_id"));
        r.propertyId = Ids.property(rs.getInt("property_id"));
        r.ownerId = Ids.user(rs.getInt("owner_id"));
        r.providerId = Ids.user(rs.getInt("provider_id"));
        r.category = rs.getString("category_name");
        r.description = rs.getString("description");
        r.notes = rs.getString("notes");
        r.preferredDate = rs.getObject("preferred_date", LocalDate.class);
        r.createdOn = rs.getObject("created_on", LocalDate.class);
        r.status = rs.getString("status");
        r.accepted = rs.getBoolean("is_accepted");
        r.reviewed = rs.getBoolean("reviewed");
        return r;
    }

    private List<ServiceRequest> withDetails(List<ServiceRequest> list) {
        if (list.isEmpty()) return list;
        Map<Integer, ServiceRequest> byId = new HashMap<>();
        StringBuilder in = new StringBuilder();
        for (ServiceRequest r : list) {
            byId.put(Ids.num(r.id), r);
            in.append(in.length() == 0 ? "?" : ", ?");
        }
        List<Integer> ids = new ArrayList<>(byId.keySet());
        Db.query("SELECT h.request_id, h.status, h.note, h.changed_by, u.full_name, h.changed_at FROM request_status_history h "
            + "JOIN users u ON u.user_id = h.changed_by WHERE h.request_id IN (" + in + ") ORDER BY h.changed_at, h.history_id",
            ps -> { for (int i = 0; i < ids.size(); i++) ps.setInt(i + 1, ids.get(i)); },
            rs -> {
                byId.get(rs.getInt("request_id")).history.add(new StatusEntry(rs.getString("status"), rs.getString("note"),
                    rs.getString("full_name"), Ids.user(rs.getInt("changed_by")), rs.getObject("changed_at", LocalDateTime.class)));
                return Boolean.TRUE;
            });
        Db.query("SELECT request_id, photo_ref FROM request_photos WHERE request_id IN (" + in + ") ORDER BY photo_id",
            ps -> { for (int i = 0; i < ids.size(); i++) ps.setInt(i + 1, ids.get(i)); },
            rs -> { byId.get(rs.getInt("request_id")).photos.add(rs.getString("photo_ref")); return Boolean.TRUE; });
        return list;
    }

    List<ServiceRequest> all() { return withDetails(Db.query(SELECT + "ORDER BY r.request_id", null, RequestDao::map)); }

    List<ServiceRequest> byOwner(String ownerId) {
        return withDetails(Db.query(SELECT + "WHERE pr.owner_id = ? ORDER BY r.request_id", ps -> ps.setInt(1, Ids.num(ownerId)), RequestDao::map));
    }

    List<ServiceRequest> byProvider(String providerId) {
        return withDetails(Db.query(SELECT + "WHERE r.provider_id = ? ORDER BY r.request_id", ps -> ps.setInt(1, Ids.num(providerId)), RequestDao::map));
    }

    ServiceRequest get(String id) {
        List<ServiceRequest> list = withDetails(Db.query(SELECT + "WHERE r.request_id = ?", ps -> ps.setInt(1, Ids.num(id)), RequestDao::map));
        return list.isEmpty() ? null : list.get(0);
    }

    void save(ServiceRequest r) {
        Db.transaction(() -> {
            int id;
            int knownHistory = 0, knownPhotos = 0;
            if (r.id == null) {
                id = Db.insert("INSERT INTO service_requests (property_id, provider_id, category_id, description, notes, preferred_date, created_on, status, is_accepted) "
                    + "VALUES (?, ?, (SELECT category_id FROM categories WHERE name = ?), ?, ?, ?, ?, ?, ?)", ps -> {
                        ps.setInt(1, Ids.num(r.propertyId)); ps.setInt(2, Ids.num(r.providerId)); ps.setString(3, r.category);
                        ps.setString(4, r.description); ps.setString(5, r.notes == null ? "" : r.notes);
                        ps.setObject(6, r.preferredDate); ps.setObject(7, r.createdOn); ps.setString(8, r.status); ps.setBoolean(9, r.accepted);
                    });
                r.id = Ids.request(id);
            } else {
                id = Ids.num(r.id);
                Db.update("UPDATE service_requests SET status = ?, is_accepted = ?, description = ?, notes = ?, preferred_date = ? WHERE request_id = ?", ps -> {
                    ps.setString(1, r.status); ps.setBoolean(2, r.accepted); ps.setString(3, r.description);
                    ps.setString(4, r.notes == null ? "" : r.notes); ps.setObject(5, r.preferredDate); ps.setInt(6, id);
                });
                knownHistory = count("SELECT COUNT(*) FROM request_status_history WHERE request_id = ?", id);
                knownPhotos = count("SELECT COUNT(*) FROM request_photos WHERE request_id = ?", id);
            }
            for (int i = knownHistory; i < r.history.size(); i++) {
                StatusEntry e = r.history.get(i);
                if (e.byId != null) {
                    Db.update("INSERT INTO request_status_history (request_id, status, note, changed_by, changed_at) VALUES (?, ?, ?, ?, ?)", ps -> {
                        ps.setInt(1, id); ps.setString(2, e.status); ps.setString(3, e.note); ps.setInt(4, Ids.num(e.byId)); ps.setObject(5, e.time);
                    });
                } else {
                    Db.update("INSERT INTO request_status_history (request_id, status, note, changed_by, changed_at) "
                        + "VALUES (?, ?, ?, (SELECT user_id FROM users WHERE full_name = ? LIMIT 1), ?)", ps -> {
                            ps.setInt(1, id); ps.setString(2, e.status); ps.setString(3, e.note); ps.setString(4, e.by); ps.setObject(5, e.time);
                        });
                }
            }
            for (int i = knownPhotos; i < r.photos.size(); i++) {
                String ref = r.photos.get(i);
                Db.update("INSERT INTO request_photos (request_id, photo_ref, added_at) VALUES (?, ?, ?)", ps -> {
                    ps.setInt(1, id); ps.setString(2, ref); ps.setObject(3, LocalDateTime.now());
                });
            }
        });
    }

    private static int count(String sql, int id) {
        Integer n = Db.queryOne(sql, ps -> ps.setInt(1, id), rs -> rs.getInt(1));
        return n == null ? 0 : n;
    }
}
