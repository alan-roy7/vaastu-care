package com.vaastucare.controller;

import com.vaastucare.data.DataService;
import com.vaastucare.model.*;
import com.vaastucare.security.PasswordHasher;
import com.vaastucare.view.MainFrame;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Controller / service layer. It holds the session, enforces role and ownership rules on every
 * change (not just by hiding buttons), reloads records from the data store before changing them,
 * and groups multi-step changes in transactions.
 * Rule violations throw SecurityException or IllegalStateException; Main shows their message to the user.
 */
public class AppController {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("[+0-9 ()-]{7,20}");
    private static final int MAX_FAILED_LOGINS = 5;
    private static final long LOCK_MILLIS = 30_000;

    private final DataService data;
    private MainFrame frame;
    private User current;
    private final Map<String, Integer> failedLogins = new HashMap<>();
    private final Map<String, Long> lockedUntil = new HashMap<>();

    public AppController(DataService data) { this.data = data; }

    public void attach(MainFrame frame) { this.frame = frame; }
    public DataService data() { return data; }
    public User user() { return current; }

    // ---------------------------------------------------------------- guards
    private User me() {
        if (current == null) throw new SecurityException("Please log in first.");
        return current;
    }
    private User requireRole(Role role) {
        User u = me();
        if (u.role != role) throw new SecurityException("Your account (" + u.roleLabel() + ") is not allowed to do this.");
        return u;
    }
    private static boolean blank(String s, int min) { return s == null || s.trim().length() < min; }

    // ---------------------------------------------------------------- session / navigation
    /** @return the logged-in user, or null when the credentials are wrong. */
    public User login(String login, String password) {
        String key = login.trim().toLowerCase(Locale.ROOT);
        Long until = lockedUntil.get(key);
        if (until != null && until > System.currentTimeMillis())
            throw new IllegalStateException("Too many failed attempts. Please wait 30 seconds and try again.");
        User u = data.findUserByLogin(login.trim());
        if (u == null || !PasswordHasher.verify(password, u.password)) {
            int n = failedLogins.merge(key, 1, Integer::sum);
            if (n >= MAX_FAILED_LOGINS) { lockedUntil.put(key, System.currentTimeMillis() + LOCK_MILLIS); failedLogins.remove(key); }
            current = null;
            return null;
        }
        failedLogins.remove(key);
        current = u;
        return u;
    }
    public void logout() { current = null; if (frame != null) frame.showLogin(); }
    public void navigate(String screen) { frame.shell().show(screen, null); }
    public void openTracking(String requestId) { frame.shell().show("tracking", requestId); }
    public void openProvider(String providerId) { frame.shell().show("providerProfile", providerId); }
    public void toast(String msg) { if (frame != null) frame.toast(msg, false); }
    public void toastError(String msg) { if (frame != null) frame.toast(msg, true); }
    private void changed() { if (frame != null && frame.shell() != null) frame.shell().refreshBell(); }

    // ---------------------------------------------------------------- registration
    /** Self-registration for owners and providers only. @return error message, or null when registered. */
    public String register(User u) {
        if (u.role != Role.OWNER && u.role != Role.PROVIDER) throw new SecurityException("Only owner and provider accounts can be registered here.");
        if (blank(u.name, 3) || blank(u.city, 1)) return "Name and city are required.";
        if (u.email == null || !EMAIL.matcher(u.email.trim()).matches()) return "Enter a valid email address.";
        if (blank(u.username, 3) || u.username.contains(" ")) return "Username needs 3+ characters and no spaces.";
        if (u.phone == null || !PHONE.matcher(u.phone.trim()).matches()) return "Enter a valid phone number.";
        if (u.password == null || u.password.length() < 6) return "Password must be at least 6 characters.";
        if (u instanceof Provider p) {
            if (blank(p.area, 1) || blank(p.description, 10)) return "Providers must enter a service area and a description (10+ characters).";
            boolean validCategory = data.getCategories().stream().anyMatch(c -> c.active && c.name.equals(p.category));
            if (!validCategory) return "Choose a valid service category.";
            p.approval = Provider.PENDING;          // providers can never approve themselves
            p.suspended = false;
            p.rejectionReason = "";
            p.appliedOn = LocalDate.now();
        }
        u.name = u.name.trim(); u.email = u.email.trim(); u.username = u.username.trim();
        if (data.isIdentifierTaken(u.email) || data.isIdentifierTaken(u.username))
            return "That email or username is already registered.";
        u.password = PasswordHasher.hash(u.password);
        data.inTransaction(() -> {
            data.addUser(u);
            if (u instanceof Provider p)
                for (User a : data.getAllUsers())
                    if (a.role == Role.ADMIN) notify(a.id, "New provider application from " + p.name + ".", null);
        });
        return null;
    }

    // ---------------------------------------------------------------- ratings
    public double avgRating(String providerId) {
        List<Review> list = data.getReviewsByProvider(providerId);
        return list.isEmpty() ? 0 : list.stream().mapToInt(r -> r.rating).average().orElse(0);
    }
    public int reviewCount(String providerId) { return data.getReviewsByProvider(providerId).size(); }
    public String ratingText(String providerId) {
        List<Review> list = data.getReviewsByProvider(providerId);
        if (list.isEmpty()) return "No reviews yet";
        double avg = list.stream().mapToInt(r -> r.rating).average().orElse(0);
        return String.format("★ %.1f  (%d review%s)", avg, list.size(), list.size() == 1 ? "" : "s");
    }

    // ---------------------------------------------------------------- reading a request (access controlled)
    /** Loads a request if the logged-in user may see it: admins any, owners their own, providers their own jobs. */
    public ServiceRequest request(String id) {
        User u = me();
        ServiceRequest r = data.getRequest(id);
        if (r == null) throw new IllegalStateException("That request no longer exists.");
        boolean ok = u.role == Role.ADMIN || (u.role == Role.OWNER && r.ownerId.equals(u.id))
            || (u.role == Role.PROVIDER && r.providerId.equals(u.id));
        if (!ok) throw new SecurityException("You do not have access to request " + id + ".");
        return r;
    }

    // ---------------------------------------------------------------- owner actions
    public void saveProperty(Property p) {
        User u = requireRole(Role.OWNER);
        if (blank(p.name, 3) || blank(p.address, 5) || blank(p.city, 1) || p.areaSqft <= 0) throw new IllegalStateException("Please fill in the property name, address, city and a positive area.");
        if (p.id == null) {
            p.ownerId = u.id;
        } else {
            Property stored = data.getProperty(p.id);
            if (stored == null || !stored.ownerId.equals(u.id)) throw new SecurityException("You can only edit your own properties.");
            p.ownerId = stored.ownerId;
        }
        boolean isNew = p.id == null;
        data.saveProperty(p);
        toast(isNew ? "Property added." : "Property updated.");
    }

    public ServiceRequest createRequest(Property prop, String category, Provider provider,
                                        String description, LocalDate date, String notes) {
        User u = requireRole(Role.OWNER);
        Property stored = data.getProperty(prop.id);
        if (stored == null || !stored.ownerId.equals(u.id)) throw new SecurityException("You can only request services for your own properties.");
        Provider prov = data.getProvider(provider.id);
        if (prov == null || !prov.isListed()) throw new IllegalStateException("That provider is not available (not approved or suspended).");
        if (!prov.category.equals(category)) throw new IllegalStateException("The provider does not offer that service category.");
        boolean active = data.getCategories().stream().anyMatch(c -> c.active && c.name.equals(category));
        if (!active) throw new IllegalStateException("That service category is currently disabled.");
        if (blank(description, 10)) throw new IllegalStateException("Please describe the work in at least 10 characters.");
        if (date == null || date.isBefore(LocalDate.now())) throw new IllegalStateException("The preferred date cannot be in the past.");

        ServiceRequest r = new ServiceRequest();
        r.propertyId = stored.id; r.ownerId = u.id; r.providerId = prov.id;
        r.category = category; r.description = description.trim(); r.preferredDate = date; r.notes = notes == null ? "" : notes.trim();
        r.status = Status.SCHEDULED; r.createdOn = LocalDate.now();
        r.history.add(new StatusEntry(Status.SCHEDULED, "Request submitted", u.name, u.id, LocalDateTime.now()));
        data.inTransaction(() -> {
            data.saveRequest(r);
            notify(prov.id, "New service request " + r.id + " from " + u.name + ".", r.id);
        });
        changed();
        return r;
    }

    /** @return error message, or null on success. */
    public String submitReview(ServiceRequest passed, int rating, String text) {
        User u = requireRole(Role.OWNER);
        ServiceRequest r = request(passed.id);
        if (!r.ownerId.equals(u.id)) return "Only the owner of this request can review it.";
        if (!Status.COMPLETED.equals(r.status)) return "Reviews can only be submitted after the service is completed.";
        if (r.reviewed) return "This request has already been reviewed.";
        if (rating < 1 || rating > 5) return "Choose a star rating from 1 to 5.";
        if (blank(text, 10)) return "Write a review of at least 10 characters.";
        data.inTransaction(() -> {
            data.saveReview(new Review(null, r.id, u.id, r.providerId, rating, text.trim(), LocalDate.now()));
            notify(r.providerId, u.name + " rated you " + rating + " star" + (rating == 1 ? "" : "s") + ".", r.id);
        });
        changed();
        return null;
    }

    // ---------------------------------------------------------------- provider workflow
    private ServiceRequest providerJob(String id) {
        User u = requireRole(Role.PROVIDER);
        ServiceRequest r = request(id);   // already checks that it is this provider's job
        if (!r.providerId.equals(u.id)) throw new SecurityException("This request is assigned to another provider.");
        return r;
    }

    public void acceptRequest(ServiceRequest passed) {
        ServiceRequest r = providerJob(passed.id);
        if (!r.awaitingProvider()) throw new IllegalStateException("This request is no longer waiting for a response.");
        r.accepted = true;
        data.inTransaction(() -> {
            addHistory(r, Status.SCHEDULED, "Request accepted by provider");
            data.saveRequest(r);
            notify(r.ownerId, "Your request " + r.id + " was accepted by " + me().name + ".", r.id);
        });
        changed();
    }

    public void rejectRequest(ServiceRequest passed, String reason) {
        ServiceRequest r = providerJob(passed.id);
        if (!r.awaitingProvider()) throw new IllegalStateException("This request is no longer waiting for a response.");
        if (blank(reason, 3)) throw new IllegalStateException("Please give a short reason for rejecting.");
        r.status = Status.REJECTED;
        data.inTransaction(() -> {
            addHistory(r, Status.REJECTED, "Declined: " + reason.trim());
            data.saveRequest(r);
            notify(r.ownerId, "Your request " + r.id + " was declined by " + me().name + ".", r.id);
        });
        changed();
    }

    public void startWork(ServiceRequest passed) {
        ServiceRequest r = providerJob(passed.id);
        if (!Status.SCHEDULED.equals(r.status) || !r.accepted) throw new IllegalStateException("Only accepted, scheduled jobs can be started.");
        r.status = Status.IN_PROGRESS;
        data.inTransaction(() -> {
            addHistory(r, Status.IN_PROGRESS, "Work started on site");
            data.saveRequest(r);
            notify(r.ownerId, r.id + ": work has started on site.", r.id);
        });
        changed();
    }

    /** ref is "DEMO:n" (sample image) or "FILE:uploads/name" (stored by addPhotoFile). */
    public void addPhoto(ServiceRequest passed, String ref) {
        if (ref == null || !(ref.startsWith("DEMO:") || ref.startsWith("FILE:uploads/"))) throw new IllegalStateException("Invalid photo reference.");
        ServiceRequest r = providerJob(passed.id);
        if (!Status.IN_PROGRESS.equals(r.status)) throw new IllegalStateException("Photos can only be added while the job is In Progress.");
        r.photos.add(ref);
        data.inTransaction(() -> {
            addHistory(r, r.status, ref.startsWith("DEMO:") ? "Demo photo added (sample image)" : "Photo uploaded");
            data.saveRequest(r);
        });
    }

    /** Copies the chosen picture into the uploads folder and attaches it to the job. */
    public void addPhotoFile(ServiceRequest passed, File file) throws IOException {
        ServiceRequest r = providerJob(passed.id);
        if (!Status.IN_PROGRESS.equals(r.status)) throw new IllegalStateException("Photos can only be added while the job is In Progress.");
        String name = file.getName().toLowerCase(Locale.ROOT);
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
        if (!List.of("jpg", "jpeg", "png", "gif").contains(ext)) throw new IOException("Please choose a JPG, PNG or GIF picture.");
        if (file.length() > 10L * 1024 * 1024) throw new IOException("Please choose a picture smaller than 10 MB.");
        if (ImageIO.read(file) == null) throw new IOException("That file could not be read as an image.");
        Path dir = Paths.get("uploads");
        Files.createDirectories(dir);
        String stored = r.id + "-" + System.currentTimeMillis() + "." + ext;
        Files.copy(file.toPath(), dir.resolve(stored));
        addPhoto(r, "FILE:uploads/" + stored);
    }

    /** @return error message, or null on success. */
    public String completeRequest(ServiceRequest passed) {
        ServiceRequest r = providerJob(passed.id);
        if (!Status.IN_PROGRESS.equals(r.status)) return "Only jobs that are In Progress can be completed.";
        if (r.photos.isEmpty()) return "Add at least one completion photo before marking the job as completed.";
        r.status = Status.COMPLETED;
        data.inTransaction(() -> {
            addHistory(r, Status.COMPLETED, "Work completed; photo proof added");
            data.saveRequest(r);
            notify(r.ownerId, r.id + " is completed. Please rate " + me().name + ".", r.id);
        });
        changed();
        return null;
    }

    /** Providers can change their profile details, but never their own approval or suspension. */
    public void updateProviderProfile(Provider edited) {
        User u = requireRole(Role.PROVIDER);
        if (!edited.id.equals(u.id)) throw new SecurityException("You can only edit your own profile.");
        Provider stored = data.getProvider(u.id);
        if (blank(edited.name, 3) || edited.phone == null || !PHONE.matcher(edited.phone.trim()).matches()
            || blank(edited.area, 1) || blank(edited.availability, 1) || blank(edited.description, 10))
            throw new IllegalStateException("Please check the profile fields (name, phone, area, availability, description).");
        boolean categoryOk = edited.category.equals(stored.category)
            || data.getCategories().stream().anyMatch(c -> c.active && c.name.equals(edited.category));
        if (!categoryOk) throw new IllegalStateException("Choose an active service category.");
        stored.name = edited.name.trim(); stored.phone = edited.phone.trim(); stored.city = edited.city.trim();
        stored.category = edited.category; stored.area = edited.area.trim(); stored.availability = edited.availability.trim();
        stored.description = edited.description.trim(); stored.experienceYears = Math.max(0, edited.experienceYears);
        data.saveProvider(stored);
        u.name = stored.name;
        toast("Profile saved.");
    }

    // ---------------------------------------------------------------- admin actions
    public void approveProvider(Provider passed) {
        requireRole(Role.ADMIN);
        Provider p = data.getProvider(passed.id);
        p.approval = Provider.APPROVED; p.rejectionReason = ""; p.suspended = false;
        data.inTransaction(() -> {
            data.saveProvider(p);
            notify(p.id, "Your provider profile was approved. You now appear in the verified directory.", null);
        });
        changed();
    }
    public void rejectProvider(Provider passed, String reason) {
        requireRole(Role.ADMIN);
        if (blank(reason, 3)) throw new IllegalStateException("Please give a reason.");
        Provider p = data.getProvider(passed.id);
        p.approval = Provider.REJECTED; p.rejectionReason = reason.trim();
        data.inTransaction(() -> {
            data.saveProvider(p);
            notify(p.id, "Your provider application was rejected: " + reason.trim(), null);
        });
        changed();
    }
    public void setSuspended(Provider passed, boolean suspended) {
        requireRole(Role.ADMIN);
        Provider p = data.getProvider(passed.id);
        p.suspended = suspended;
        data.inTransaction(() -> {
            data.saveProvider(p);
            notify(p.id, suspended ? "Your listing was suspended by an administrator." : "Your listing was reinstated.", null);
        });
        changed();
    }
    public void saveCategory(Category c) {
        requireRole(Role.ADMIN);
        if (blank(c.name, 3) || blank(c.description, 5)) throw new IllegalStateException("A category needs a name (3+ characters) and a short description.");
        if (c.id == null && data.getCategories().stream().anyMatch(x -> x.name.equalsIgnoreCase(c.name.trim())))
            throw new IllegalStateException("That category already exists.");
        c.name = c.name.trim();
        data.saveCategory(c);
    }
    /** @return error message, or null when deleted. */
    public String deleteCategory(Category c) {
        requireRole(Role.ADMIN);
        boolean used = data.getProviders().stream().anyMatch(p -> c.name.equals(p.category))
            || data.getAllRequests().stream().anyMatch(r -> c.name.equals(r.category));
        if (used) return "This category is used by providers or requests. Disable it instead of deleting.";
        data.deleteCategory(c.id);
        return null;
    }

    // ---------------------------------------------------------------- notifications
    public int unreadCount() {
        if (current == null) return 0;
        return (int) data.getNotifications(current.id).stream().filter(n -> !n.read).count();
    }
    public void markAllRead() {
        data.markAllNotificationsRead(me().id);
        changed();
    }
    public void markRead(Notification n) {
        if (!n.userId.equals(me().id)) throw new SecurityException("That notification belongs to another user.");
        data.markNotificationRead(n.id);
        changed();
    }

    private void notify(String userId, String text, String requestId) {
        data.saveNotification(new Notification(null, userId, text, requestId, LocalDateTime.now(), false));
    }
    private void addHistory(ServiceRequest r, String status, String note) {
        User u = me();
        r.history.add(new StatusEntry(status, note, u.name, u.id, LocalDateTime.now()));
    }
}
