package com.vaastucare.data.jdbc;

import com.vaastucare.db.Db;
import com.vaastucare.model.Property;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

final class PropertyDao {
    private static final String SELECT =
        "SELECT property_id, owner_id, name, address, city, property_type, area_sqft, bedrooms, notes, image_seed FROM properties ";

    private static Property map(ResultSet rs) throws SQLException {
        return new Property(Ids.property(rs.getInt("property_id")), Ids.user(rs.getInt("owner_id")), rs.getString("name"),
            rs.getString("address"), rs.getString("city"), rs.getString("property_type"), rs.getInt("area_sqft"),
            rs.getInt("bedrooms"), rs.getString("notes"), rs.getInt("image_seed"));
    }

    List<Property> byOwner(String ownerId) {
        return Db.query(SELECT + "WHERE owner_id = ? ORDER BY property_id", ps -> ps.setInt(1, Ids.num(ownerId)), PropertyDao::map);
    }

    Property get(String id) {
        return Db.queryOne(SELECT + "WHERE property_id = ?", ps -> ps.setInt(1, Ids.num(id)), PropertyDao::map);
    }

    void save(Property p) {
        if (p.id == null) {
            int id = Db.insert("INSERT INTO properties (owner_id, name, address, city, property_type, area_sqft, bedrooms, notes, image_seed) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", ps -> {
                    ps.setInt(1, Ids.num(p.ownerId));
                    fill(ps, p, 2);
                });
            p.id = Ids.property(id);
        } else {
            Db.update("UPDATE properties SET name = ?, address = ?, city = ?, property_type = ?, area_sqft = ?, bedrooms = ?, notes = ?, image_seed = ? "
                + "WHERE property_id = ?", ps -> { fill(ps, p, 1); ps.setInt(9, Ids.num(p.id)); });
        }
    }

    private static void fill(PreparedStatement ps, Property p, int start) throws SQLException {
        ps.setString(start, p.name); ps.setString(start + 1, p.address); ps.setString(start + 2, p.city);
        ps.setString(start + 3, p.type); ps.setInt(start + 4, p.areaSqft); ps.setInt(start + 5, p.bedrooms);
        ps.setString(start + 6, p.notes == null ? "" : p.notes); ps.setInt(start + 7, p.imageSeed);
    }
}
