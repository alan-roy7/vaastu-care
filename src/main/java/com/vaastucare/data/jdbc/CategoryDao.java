package com.vaastucare.data.jdbc;

import com.vaastucare.db.Db;
import com.vaastucare.model.Category;

import java.util.List;

final class CategoryDao {
    List<Category> all() {
        return Db.query("SELECT category_id, name, description, is_active FROM categories ORDER BY category_id", null, rs -> {
            Category c = new Category(Ids.category(rs.getInt("category_id")), rs.getString("name"), rs.getString("description"));
            c.active = rs.getBoolean("is_active");
            return c;
        });
    }

    void save(Category c) {
        if (c.id == null) {
            int id = Db.insert("INSERT INTO categories (name, description, is_active) VALUES (?, ?, ?)",
                ps -> { ps.setString(1, c.name); ps.setString(2, c.description); ps.setBoolean(3, c.active); });
            c.id = Ids.category(id);
        } else {
            Db.update("UPDATE categories SET description = ?, is_active = ? WHERE category_id = ?",
                ps -> { ps.setString(1, c.description); ps.setBoolean(2, c.active); ps.setInt(3, Ids.num(c.id)); });
        }
    }

    void delete(String id) {
        Db.update("DELETE FROM categories WHERE category_id = ?", ps -> ps.setInt(1, Ids.num(id)));
    }
}
