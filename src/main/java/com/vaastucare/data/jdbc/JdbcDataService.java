package com.vaastucare.data.jdbc;

import com.vaastucare.data.DataService;
import com.vaastucare.db.Db;
import com.vaastucare.model.*;

import java.util.List;

/** MySQL implementation of DataService (JDBC + PreparedStatement). Delegates to one DAO per table group. */
public class JdbcDataService implements DataService {
    private final UserDao users = new UserDao();
    private final PropertyDao properties = new PropertyDao();
    private final RequestDao requests = new RequestDao();
    private final ReviewDao reviews = new ReviewDao();
    private final CategoryDao categories = new CategoryDao();
    private final NotificationDao notifications = new NotificationDao();

    @Override public User findUserByLogin(String login) { return users.findByLogin(login); }
    @Override public boolean isIdentifierTaken(String value) { return users.identifierTaken(value); }
    @Override public void addUser(User user) { users.add(user); }
    @Override public User getUser(String id) { return users.get(id); }
    @Override public List<User> getAllUsers() { return users.all(); }
    @Override public List<User> getOwners() { return users.owners(); }
    @Override public List<Provider> getProviders() { return users.providers(); }
    @Override public Provider getProvider(String id) { return users.provider(id); }
    @Override public void saveProvider(Provider provider) { users.saveProvider(provider); }

    @Override public List<Property> getPropertiesByOwner(String ownerId) { return properties.byOwner(ownerId); }
    @Override public Property getProperty(String id) { return properties.get(id); }
    @Override public void saveProperty(Property property) { properties.save(property); }

    @Override public List<ServiceRequest> getAllRequests() { return requests.all(); }
    @Override public List<ServiceRequest> getRequestsByOwner(String ownerId) { return requests.byOwner(ownerId); }
    @Override public List<ServiceRequest> getRequestsByProvider(String providerId) { return requests.byProvider(providerId); }
    @Override public ServiceRequest getRequest(String id) { return requests.get(id); }
    @Override public void saveRequest(ServiceRequest request) { requests.save(request); }

    @Override public List<Review> getAllReviews() { return reviews.all(); }
    @Override public List<Review> getReviewsByProvider(String providerId) { return reviews.byProvider(providerId); }
    @Override public void saveReview(Review review) { reviews.save(review); }

    @Override public List<Category> getCategories() { return categories.all(); }
    @Override public void saveCategory(Category category) { categories.save(category); }
    @Override public void deleteCategory(String id) { categories.delete(id); }

    @Override public List<Notification> getNotifications(String userId) { return notifications.byUser(userId); }
    @Override public void saveNotification(Notification n) { notifications.save(n); }
    @Override public void markNotificationRead(String id) { notifications.markRead(id); }
    @Override public void markAllNotificationsRead(String userId) { notifications.markAllRead(userId); }

    @Override public void inTransaction(Runnable work) { Db.transaction(work); }
}
