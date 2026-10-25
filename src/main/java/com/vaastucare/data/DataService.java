package com.vaastucare.data;

import com.vaastucare.model.*;
import java.util.List;

/**
 * Data-access contract used by the controller and views.
 * Implementations: JdbcDataService (MySQL, default) and MockDataService (in-memory, "--mock" flag).
 * Every save... method inserts when the id is null and updates otherwise.
 * Implementations throw DataAccessException (unchecked) when the data store fails.
 */
public interface DataService {
    // users
    /** Finds an account by email or username (case-insensitive). The password field holds the stored hash. */
    User findUserByLogin(String emailOrUsername);
    boolean isIdentifierTaken(String emailOrUsername);
    void addUser(User user);
    User getUser(String id);
    List<User> getAllUsers();
    List<User> getOwners();
    List<Provider> getProviders();
    Provider getProvider(String id);
    void saveProvider(Provider provider);

    // properties
    List<Property> getPropertiesByOwner(String ownerId);
    Property getProperty(String id);
    void saveProperty(Property property);

    // service requests (history and photos are appended, never rewritten)
    List<ServiceRequest> getAllRequests();
    List<ServiceRequest> getRequestsByOwner(String ownerId);
    List<ServiceRequest> getRequestsByProvider(String providerId);
    ServiceRequest getRequest(String id);
    void saveRequest(ServiceRequest request);

    // reviews
    List<Review> getAllReviews();
    List<Review> getReviewsByProvider(String providerId);
    void saveReview(Review review);

    // categories
    List<Category> getCategories();
    void saveCategory(Category category);
    void deleteCategory(String id);

    // notifications
    List<Notification> getNotifications(String userId);
    void saveNotification(Notification notification);
    void markNotificationRead(String notificationId);
    void markAllNotificationsRead(String userId);

    /** Runs several changes as one unit: all succeed or all are rolled back. */
    void inTransaction(Runnable work);
}
