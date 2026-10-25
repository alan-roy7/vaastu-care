package com.vaastucare.data.jdbc;

import com.vaastucare.db.Db;
import com.vaastucare.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

final class ReviewDao {
    private static final String SELECT =
        "SELECT review_id, request_id, owner_id, provider_id, rating, review_text, review_date FROM reviews ";

    private static Review map(ResultSet rs) throws SQLException {
        int req = rs.getInt("request_id");
        String requestId = rs.wasNull() ? null : Ids.request(req);
        return new Review(Ids.review(rs.getInt("review_id")), requestId, Ids.user(rs.getInt("owner_id")),
            Ids.user(rs.getInt("provider_id")), rs.getInt("rating"), rs.getString("review_text"),
            rs.getObject("review_date", LocalDate.class));
    }

    List<Review> all() { return Db.query(SELECT + "ORDER BY review_id", null, ReviewDao::map); }

    List<Review> byProvider(String providerId) {
        return Db.query(SELECT + "WHERE provider_id = ? ORDER BY review_date DESC, review_id DESC",
            ps -> ps.setInt(1, Ids.num(providerId)), ReviewDao::map);
    }

    void save(Review r) {
        int id = Db.insert("INSERT INTO reviews (request_id, owner_id, provider_id, rating, review_text, review_date) VALUES (?, ?, ?, ?, ?, ?)", ps -> {
            if (r.requestId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, Ids.num(r.requestId));
            ps.setInt(2, Ids.num(r.ownerId)); ps.setInt(3, Ids.num(r.providerId)); ps.setInt(4, r.rating);
            ps.setString(5, r.text); ps.setObject(6, r.date);
        });
        r.id = Ids.review(id);
    }
}
