package com.vaastucare.db;

import com.vaastucare.data.MockDataService;
import com.vaastucare.data.jdbc.JdbcDataService;
import com.vaastucare.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Copies the built-in sample data (owners, providers, admin, properties, requests, reviews, notifications)
 * into MySQL. Passwords are already hashed by MockDataService. Only runs when the users table is empty.
 */
final class SampleData {
    private SampleData() {}

    static void seedIfEmpty(AppConfig cfg) {
        Db.init(cfg);
        JdbcDataService jdbc = new JdbcDataService();
        if (!jdbc.getAllUsers().isEmpty()) {
            System.out.println("Database already has accounts - sample data was NOT added. Use --reset to start over.");
            return;
        }
        MockDataService src = new MockDataService();
        Map<String, String> ids = new HashMap<>();   // sample id -> database id
        jdbc.inTransaction(() -> {
            for (Category c : src.getCategories()) {
                Category n = new Category(null, c.name, c.description);
                n.active = c.active;
                jdbc.saveCategory(n);
            }
            for (User u : src.getAllUsers()) {
                User n;
                if (u instanceof Provider p) {
                    Provider np = new Provider(null, p.name, p.email, p.username, p.password, p.phone, p.city, p.category,
                        p.area, p.experienceYears, p.description, p.availability, p.approval);
                    np.rejectionReason = p.rejectionReason; np.suspended = p.suspended; np.appliedOn = p.appliedOn;
                    n = np;
                } else {
                    n = new User(null, u.role, u.name, u.email, u.username, u.password, u.phone, u.city);
                }
                jdbc.addUser(n);
                ids.put(u.id, n.id);
            }
            for (User owner : src.getOwners())
                for (Property p : src.getPropertiesByOwner(owner.id)) {
                    Property n = new Property(null, ids.get(p.ownerId), p.name, p.address, p.city, p.type, p.areaSqft, p.bedrooms, p.notes, p.imageSeed);
                    jdbc.saveProperty(n);
                    ids.put(p.id, n.id);
                }
            for (ServiceRequest r : src.getAllRequests()) {
                ServiceRequest n = new ServiceRequest();
                n.propertyId = ids.get(r.propertyId); n.ownerId = ids.get(r.ownerId); n.providerId = ids.get(r.providerId);
                n.category = r.category; n.description = r.description; n.notes = r.notes; n.status = r.status;
                n.accepted = r.accepted; n.preferredDate = r.preferredDate; n.createdOn = r.createdOn;
                n.history.addAll(r.history);
                n.photos.addAll(r.photos);
                jdbc.saveRequest(n);
                ids.put(r.id, n.id);
            }
            for (Review rv : src.getAllReviews())
                jdbc.saveReview(new Review(null, rv.requestId == null ? null : ids.get(rv.requestId), ids.get(rv.ownerId),
                    ids.get(rv.providerId), rv.rating, rv.text, rv.date));
            for (User u : src.getAllUsers())
                for (Notification n : src.getNotifications(u.id))
                    jdbc.saveNotification(new Notification(null, ids.get(n.userId), n.text,
                        n.requestId == null ? null : ids.get(n.requestId), n.time, n.read));
        });
        System.out.println("Sample data added: " + jdbc.getAllUsers().size() + " users, " + jdbc.getAllRequests().size() + " requests.");
    }
}
