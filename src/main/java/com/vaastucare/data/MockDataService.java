package com.vaastucare.data;

import com.vaastucare.model.*;
import com.vaastucare.security.PasswordHasher;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation with fixed, hand-written sample data (all fictional).
 * Changes made while the app runs stay in these lists until the app closes.
 */
public class MockDataService implements DataService {
    private final List<User> users = new ArrayList<>();
    private final List<Property> properties = new ArrayList<>();
    private final List<ServiceRequest> requests = new ArrayList<>();
    private final List<Review> reviews = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final List<Notification> notifications = new ArrayList<>();
    private final Map<String, Integer> counters = new HashMap<>();

    public MockDataService() {
        seedCategories();
        seedUsers();
        seedProperties();
        seedRequests();
        seedReviews();
        seedNotifications();
    }

    private String next(String prefix, int start) {
        int n = counters.merge(prefix, 1, Integer::sum);
        return prefix + (start + n - 1);
    }

    // ------------------------------------------------------------------ seed data
    private void seedCategories() {
        String[][] c = {
            {"C1", "Cleaning", "Deep cleaning, periodic upkeep and pre-arrival cleaning"},
            {"C2", "Security and CCTV", "CCTV installation, monitoring and alarm systems"},
            {"C3", "Watchman Services", "Caretakers and watchmen for homes and land"},
            {"C4", "Plumbing", "Leak repair, pipe fitting, tanks and bathrooms"},
            {"C5", "Electrical Work", "Wiring, inspections, inverters and fittings"},
            {"C6", "Repairs and Renovation", "Painting, carpentry, masonry and refurbishing"}};
        for (String[] r : c) categories.add(new Category(r[0], r[1], r[2]));
        counters.put("C", 6);
    }

    private void seedUsers() {
        users.add(new User("U0", Role.ADMIN, "Vaastu Admin", "admin@vaastucare.test", "admin", "admin123", "+91 90000 00000", "Kochi"));
        users.add(new User("O1", Role.OWNER, "Anil Varghese", "owner@vaastucare.test", "owner", "owner123", "+971 50 000 0001", "Dubai"));
        users.add(new User("O2", Role.OWNER, "Meera Nair", "meera.nair@example.test", "meera", "demo123", "+65 8000 0002", "Singapore"));
        users.add(new User("O3", Role.OWNER, "Rajesh Menon", "rajesh.menon@example.test", "rajesh", "demo123", "+974 5000 0003", "Doha"));
        users.add(new User("O4", Role.OWNER, "Sneha Thomas", "sneha.thomas@example.test", "sneha", "demo123", "+44 7000 000004", "London"));
        users.add(new User("O5", Role.OWNER, "Kiran Pillai", "kiran.pillai@example.test", "kiran", "demo123", "+1 416 000 0005", "Toronto"));

        users.add(prov("P1", "BrightHome Cleaning Co.", "provider@vaastucare.test", "provider", "provider123", "Kottayam",
            "Cleaning", "Kottayam, Ernakulam", 6, "Deep cleaning and periodic upkeep for closed homes. Photo report after every visit.", "Mon–Sat, 8 AM – 6 PM", Provider.APPROVED));
        users.add(prov("P2", "SafeGuard CCTV Solutions", "safeguard@example.test", "safeguard", "demo123", "Kochi",
            "Security and CCTV", "Kochi, Thrissur", 9, "CCTV design, installation and remote-view setup for homes and plots.", "Mon–Sat, 9 AM – 7 PM", Provider.APPROVED));
        users.add(prov("P3", "Nightwatch Security Staffing", "nightwatch@example.test", "nightwatch", "demo123", "Kottayam",
            "Watchman Services", "Kottayam, Idukki", 12, "Trained watchmen and caretakers with daily check-in reports.", "All days, 24 hours", Provider.APPROVED));
        users.add(prov("P4", "AquaFix Plumbing", "aquafix@example.test", "aquafix", "demo123", "Ernakulam",
            "Plumbing", "Ernakulam, Kochi", 8, "Leak detection, tank cleaning, bathroom and kitchen plumbing.", "Mon–Sat, 8 AM – 5 PM", Provider.APPROVED));
        users.add(prov("P5", "VoltCare Electricals", "voltcare@example.test", "voltcare", "demo123", "Thrissur",
            "Electrical Work", "Thrissur, Palakkad", 10, "Licensed electricians for safety audits, rewiring and inverter work.", "Mon–Sat, 9 AM – 6 PM", Provider.APPROVED));
        users.add(prov("P6", "Heritage Repairs & Renovation", "heritage@example.test", "heritage", "demo123", "Kozhikode",
            "Repairs and Renovation", "Kozhikode, Kannur, Thrissur", 15, "Roof, masonry, painting and traditional-house restoration.", "Mon–Fri, 9 AM – 5 PM", Provider.APPROVED));
        users.add(prov("P7", "SparklePro Home Cleaners", "sparklepro@example.test", "sparklepro", "demo123", "Thiruvananthapuram",
            "Cleaning", "Thiruvananthapuram, Kollam", 4, "Move-in cleaning, apartment upkeep and garden clearing.", "All days, 8 AM – 6 PM", Provider.APPROVED));
        users.add(prov("P8", "PipeMasters", "pipemasters@example.test", "pipemasters", "demo123", "Alappuzha",
            "Plumbing", "Alappuzha, Kottayam", 7, "Waterlogging, borewell pipes and sanitary fittings.", "Mon–Sat, 8 AM – 6 PM", Provider.APPROVED));
        Provider p9 = prov("P9", "EyeSpy Security Systems", "eyespy@example.test", "eyespy", "demo123", "Palakkad",
            "Security and CCTV", "Palakkad", 3, "New CCTV and alarm installer awaiting verification.", "Mon–Sat, 10 AM – 6 PM", Provider.PENDING);
        Provider p10 = prov("P10", "TrustyWatch Services", "trustywatch@example.test", "trustywatch", "demo123", "Kollam",
            "Watchman Services", "Kollam", 2, "Caretaker service for plots and holiday homes, awaiting verification.", "All days", Provider.PENDING);
        p9.appliedOn = LocalDate.of(2026, 10, 14);
        p10.appliedOn = LocalDate.of(2026, 10, 17);
        users.add(p9);
        users.add(p10);
        counters.put("O", 5);
        counters.put("P", 10);
        for (User u : users) u.password = PasswordHasher.hash(u.password);   // sample accounts are stored hashed too
    }

    private Provider prov(String id, String name, String email, String username, String pw, String city,
                          String cat, String area, int exp, String desc, String avail, String approval) {
        return new Provider(id, name, email, username, pw, "+91 90000 " + String.format("%05d", Integer.parseInt(id.substring(1))),
            city, cat, area, exp, desc, avail, approval);
    }

    private void seedProperties() {
        Object[][] p = {
            {"PR1", "O1", "Varghese Family Home", "12 Temple Road, Nattakom", "Kottayam", "Independent House", 2400, 4, "Caretaker visits weekly. Water tank on terrace."},
            {"PR2", "O1", "Lakeview Apartment 4B", "Marine Drive Residency, 4th Floor", "Kochi", "Apartment", 1350, 3, "Key with the apartment association office."},
            {"PR3", "O2", "Nair Tharavad", "Puzhakkal Road, Ollur", "Thrissur", "Villa", 3200, 5, "Ancestral home with an old tiled roof."},
            {"PR4", "O2", "Coconut Grove Plot", "Kainakary North", "Alappuzha", "Agricultural Land", 26136, 0, "Coconut plantation with a small pump shed."},
            {"PR5", "O3", "Menon Residency", "Kalpathy Agraharam Street", "Palakkad", "Villa", 2800, 4, "Gated plot, solar water heater installed."},
            {"PR6", "O4", "Hillside Cottage", "Vagamon Hills Road", "Idukki", "Farmhouse", 1600, 2, "Used seasonally; damp-prone in monsoon."},
            {"PR7", "O5", "Pillai Heights Flat 12", "Kowdiar Gardens", "Thiruvananthapuram", "Apartment", 1100, 2, "Second-floor flat, lift access."},
            {"PR8", "O3", "Backwater Retreat", "Kumarakom Lake Side", "Kottayam", "Villa", 2000, 3, "Holiday home near the backwaters."}};
        int seed = 0;
        for (Object[] r : p)
            properties.add(new Property((String) r[0], (String) r[1], (String) r[2], (String) r[3], (String) r[4],
                (String) r[5], (Integer) r[6], (Integer) r[7], (String) r[8], seed++));
        counters.put("PR", 8);
    }

    /** status: 0=awaiting provider, 1=scheduled (accepted), 2=in progress, 3=completed, 4=rejected */
    private void req(int n, String prop, String owner, String prov, String cat, String desc, LocalDate pref,
                     String notes, int state, boolean reviewed, LocalDate created, int photoCount, String rejectReason) {
        ServiceRequest r = new ServiceRequest();
        r.id = "SR-" + (1000 + n); r.propertyId = prop; r.ownerId = owner; r.providerId = prov;
        r.category = cat; r.description = desc; r.preferredDate = pref; r.notes = notes;
        r.createdOn = created; r.reviewed = reviewed;
        String ownerName = getUser(owner).name, provName = getUser(prov).name;
        LocalDateTime t0 = created.atTime(10, 15);
        r.history.add(new StatusEntry(Status.SCHEDULED, "Request submitted", ownerName, t0));
        switch (state) {
            case 0 -> r.status = Status.SCHEDULED;
            case 1 -> { r.status = Status.SCHEDULED; r.accepted = true;
                r.history.add(new StatusEntry(Status.SCHEDULED, "Request accepted by provider", provName, t0.plusDays(1))); }
            case 2, 3 -> { r.accepted = true;
                r.history.add(new StatusEntry(Status.SCHEDULED, "Request accepted by provider", provName, t0.plusDays(1)));
                r.history.add(new StatusEntry(Status.IN_PROGRESS, "Work started on site", provName, pref.atTime(9, 30)));
                r.status = Status.IN_PROGRESS;
                if (state == 3) { r.status = Status.COMPLETED;
                    r.history.add(new StatusEntry(Status.COMPLETED, "Work completed; photo proof added", provName, pref.atTime(16, 45))); } }
            case 4 -> { r.status = Status.REJECTED;
                r.history.add(new StatusEntry(Status.REJECTED, "Declined: " + rejectReason, provName, t0.plusDays(1))); }
            default -> throw new IllegalArgumentException();
        }
        for (int i = 0; i < photoCount; i++) r.photos.add("DEMO:" + (n + i));
        requests.add(r);
    }

    private void seedRequests() {
        LocalDate d = LocalDate.of(2026, 10, 1);
        req(1, "PR1", "O1", "P1", "Cleaning", "Deep clean of all rooms before the family visit.", d.minusDays(14), "Please air the rooms.", 3, true, d.minusDays(22), 2, null);
        req(2, "PR1", "O1", "P2", "Security and CCTV", "Install 4 CCTV cameras with remote viewing.", d.plusDays(19), "Cameras on porch and gate.", 2, false, d.plusDays(5), 0, null);
        req(3, "PR2", "O1", "P4", "Plumbing", "Fix leaking kitchen tap and check the bathroom drain.", d.plusDays(27), "Ask the association office for the key.", 1, false, d.plusDays(12), 0, null);
        req(4, "PR2", "O1", "P1", "Cleaning", "Regular monthly cleaning of the apartment.", d.plusDays(30), "", 0, false, d.plusDays(18), 0, null);
        req(5, "PR3", "O2", "P5", "Electrical Work", "Full wiring safety inspection of the old house.", d.minusDays(10), "Check the main fuse board.", 3, true, d.minusDays(18), 3, null);
        req(6, "PR3", "O2", "P6", "Repairs and Renovation", "Repair tiled roof before monsoon damage spreads.", d.plusDays(12), "Match existing tile colour.", 2, false, d.minusDays(2), 1, null);
        req(7, "PR4", "O2", "P3", "Watchman Services", "Monthly night watch for the plantation shed.", d.minusDays(8), "", 3, true, d.minusDays(16), 2, null);
        req(8, "PR5", "O3", "P5", "Electrical Work", "Replace faulty inverter wiring.", d.minusDays(5), "", 3, true, d.minusDays(14), 2, null);
        req(9, "PR6", "O4", "P3", "Watchman Services", "Caretaker for the cottage during the off season.", d.plusDays(7), "", 4, false, d.minusDays(4), 0, "Outside our current service area");
        req(10, "PR7", "O5", "P7", "Cleaning", "Move-in cleaning for the flat.", d.minusDays(3), "", 3, true, d.minusDays(11), 2, null);
        req(11, "PR8", "O3", "P8", "Plumbing", "Inspect waterlogging near the lake-side wall.", d.plusDays(22), "", 0, false, d.plusDays(16), 0, null);
        req(12, "PR2", "O1", "P4", "Plumbing", "Replace bathroom pipe fittings.", d.minusDays(2), "Completed last week.", 3, false, d.minusDays(9), 2, null);
        counters.put("SR", 12);
    }

    private void seedReviews() {
        LocalDate d = LocalDate.of(2026, 10, 1);
        Object[][] r = {
            {"R1", "SR-1001", "O1", "P1", 5, "Spotless work and a clear photo report. Perfect for an owner living abroad.", d.minusDays(12)},
            {"R2", "SR-1005", "O2", "P5", 5, "Thorough inspection and honest advice about the old wiring.", d.minusDays(8)},
            {"R3", "SR-1007", "O2", "P3", 4, "Reliable watchman and regular updates.", d.minusDays(6)},
            {"R4", "SR-1008", "O3", "P5", 4, "Good quality work, slightly delayed start.", d.minusDays(3)},
            {"R5", "SR-1010", "O5", "P7", 5, "Flat was ready for move-in. Very professional team.", d.minusDays(1)},
            {"R6", null, "O3", "P1", 4, "Good cleaning service, would hire again.", d.minusDays(60)},
            {"R7", null, "O4", "P2", 5, "Neat CCTV installation and clear instructions for remote viewing.", d.minusDays(45)},
            {"R8", null, "O5", "P4", 4, "Fixed the leak quickly and tidied up afterwards.", d.minusDays(30)}};
        for (Object[] x : r)
            reviews.add(new Review((String) x[0], (String) x[1], (String) x[2], (String) x[3], (Integer) x[4], (String) x[5], (LocalDate) x[6]));
        counters.put("R", 8);
    }

    private void seedNotifications() {
        LocalDateTime t = LocalDate.of(2026, 10, 18).atTime(9, 0);
        notifications.add(new Notification("N1", "O1", "Your request SR-1003 was accepted by AquaFix Plumbing.", "SR-1003", t, false));
        notifications.add(new Notification("N2", "O1", "SR-1012 is completed. Please rate AquaFix Plumbing.", "SR-1012", t.plusHours(3), false));
        notifications.add(new Notification("N3", "O1", "SR-1002: work has started on site.", "SR-1002", t.minusDays(2), true));
        notifications.add(new Notification("N4", "P1", "New service request SR-1004 from Anil Varghese.", "SR-1004", t.plusHours(1), false));
        notifications.add(new Notification("N5", "P1", "Anil Varghese rated you 5 stars.", "SR-1001", t.minusDays(5), true));
        notifications.add(new Notification("N6", "U0", "2 provider applications are waiting for review.", null, t, false));
        notifications.add(new Notification("N7", "O2", "SR-1006: work is in progress.", "SR-1006", t.minusDays(1), false));
        notifications.add(new Notification("N8", "O3", "SR-1009 update available.", "SR-1009", t.minusDays(3), true));
        counters.put("N", 8);
    }

    // ------------------------------------------------------------------ users
    @Override public User findUserByLogin(String login) {
        return users.stream().filter(u -> u.email.equalsIgnoreCase(login) || u.username.equalsIgnoreCase(login)).findFirst().orElse(null);
    }
    @Override public boolean isIdentifierTaken(String id) {
        return users.stream().anyMatch(u -> u.email.equalsIgnoreCase(id) || u.username.equalsIgnoreCase(id));
    }
    @Override public void addUser(User u) {
        if (u.id == null) u.id = next(u.role == Role.OWNER ? "O" : "P", 1);
        users.add(u);
    }
    @Override public User getUser(String id) {
        return users.stream().filter(u -> u.id.equals(id)).findFirst().orElse(null);
    }
    @Override public List<User> getAllUsers() { return new ArrayList<>(users); }
    @Override public List<User> getOwners() { return users.stream().filter(u -> u.role == Role.OWNER).collect(Collectors.toList()); }
    @Override public List<Provider> getProviders() {
        return users.stream().filter(u -> u instanceof Provider).map(u -> (Provider) u).collect(Collectors.toList());
    }
    @Override public Provider getProvider(String id) { User u = getUser(id); return u instanceof Provider p ? p : null; }
    @Override public void saveProvider(Provider p) { /* objects are shared in memory; JDBC would UPDATE here */ }

    // ------------------------------------------------------------------ properties
    @Override public List<Property> getPropertiesByOwner(String ownerId) {
        return properties.stream().filter(p -> p.ownerId.equals(ownerId)).collect(Collectors.toList());
    }
    @Override public Property getProperty(String id) {
        return properties.stream().filter(p -> p.id.equals(id)).findFirst().orElse(null);
    }
    @Override public void saveProperty(Property p) {
        if (p.id == null) { p.id = next("PR", 1); properties.add(p); }
    }

    // ------------------------------------------------------------------ requests
    @Override public List<ServiceRequest> getAllRequests() { return new ArrayList<>(requests); }
    @Override public List<ServiceRequest> getRequestsByOwner(String o) {
        return requests.stream().filter(r -> r.ownerId.equals(o)).collect(Collectors.toList());
    }
    @Override public List<ServiceRequest> getRequestsByProvider(String p) {
        return requests.stream().filter(r -> r.providerId.equals(p)).collect(Collectors.toList());
    }
    @Override public ServiceRequest getRequest(String id) {
        return requests.stream().filter(r -> r.id.equals(id)).findFirst().orElse(null);
    }
    @Override public void saveRequest(ServiceRequest r) {
        if (r.id == null) { r.id = "SR-" + (1000 + counters.merge("SR", 1, Integer::sum)); requests.add(r); }
    }

    // ------------------------------------------------------------------ reviews
    @Override public List<Review> getAllReviews() { return new ArrayList<>(reviews); }
    @Override public List<Review> getReviewsByProvider(String pid) {
        return reviews.stream().filter(r -> r.providerId.equals(pid))
            .sorted(Comparator.comparing((Review r) -> r.date).reversed()).collect(Collectors.toList());
    }
    @Override public void saveReview(Review r) {
        if (r.id == null) { r.id = "R" + counters.merge("R", 1, Integer::sum); reviews.add(r); }
        if (r.requestId != null) { ServiceRequest sr = getRequest(r.requestId); if (sr != null) sr.reviewed = true; }
    }

    // ------------------------------------------------------------------ categories
    @Override public List<Category> getCategories() { return new ArrayList<>(categories); }
    @Override public void saveCategory(Category c) {
        if (c.id == null) { c.id = "C" + counters.merge("C", 1, Integer::sum); categories.add(c); }
    }
    @Override public void deleteCategory(String id) { categories.removeIf(c -> c.id.equals(id)); }

    // ------------------------------------------------------------------ notifications
    @Override public List<Notification> getNotifications(String userId) {
        return notifications.stream().filter(n -> n.userId.equals(userId))
            .sorted(Comparator.comparing((Notification n) -> n.time).reversed()).collect(Collectors.toList());
    }
    @Override public void saveNotification(Notification n) {
        if (n.id == null) { n.id = "N" + counters.merge("N", 1, Integer::sum); notifications.add(n); }
    }

    @Override public void markNotificationRead(String id) {
        notifications.stream().filter(n -> n.id.equals(id)).forEach(n -> n.read = true);
    }
    @Override public void markAllNotificationsRead(String userId) {
        notifications.stream().filter(n -> n.userId.equals(userId)).forEach(n -> n.read = true);
    }
    @Override public void inTransaction(Runnable work) { work.run(); }
}
