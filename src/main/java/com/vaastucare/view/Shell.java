package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Sidebar + top bar + page area. The sidebar entries depend on the logged-in role. */
public class Shell extends JPanel {
    private final AppController ctl;
    private final MainFrame frame;
    private final User user;
    private final CardLayout pages = new CardLayout();
    private final JPanel content = new JPanel(pages);
    private final Map<String, Screen> screens = new LinkedHashMap<>();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final JLabel pageTitle = Ui.title("");
    private final BellButton bell = new BellButton();
    private String active;

    public Shell(AppController ctl, MainFrame frame, User user) {
        super(new BorderLayout());
        this.ctl = ctl; this.frame = frame; this.user = user;
        content.setBackground(Ui.BG);
        add(buildSidebar(), BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Ui.BG);
        right.add(buildTopBar(), BorderLayout.NORTH);
        right.add(content, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);
    }

    private void register(String id, Screen s) {
        screens.put(id, s);
        content.add(new ScreenHolder((JComponent) s), id);
    }

    /** Pads every screen the same way. */
    private static class ScreenHolder extends JPanel {
        ScreenHolder(JComponent c) {
            super(new BorderLayout());
            setBackground(Ui.BG);
            setBorder(new EmptyBorder(24, 28, 20, 28));
            add(c, BorderLayout.CENTER);
        }
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Ui.GREEN_DARK);
        side.setPreferredSize(new Dimension(232, 100));
        JPanel brand = Ui.box(true);
        brand.setBorder(new EmptyBorder(26, 24, 20, 20));
        JLabel logo = new JLabel("Vaastu Care");
        logo.setFont(Ui.serif(Font.BOLD, 26));
        logo.setForeground(Ui.GOLD_SOFT);
        brand.add(logo);
        brand.add(Ui.label(user.roleLabel().toUpperCase(), 11, Font.BOLD, Ui.GOLD));
        side.add(brand, BorderLayout.NORTH);

        JPanel nav = Ui.box(true);
        nav.setBorder(new EmptyBorder(8, 12, 8, 12));
        String[][] items;
        switch (user.role) {
            case OWNER -> {
                register("dashboard", new OwnerDashboard(ctl));
                register("properties", new PropertiesScreen(ctl));
                register("providers", new ProviderDirectoryScreen(ctl));
                register("requests", new OwnerRequestsScreen(ctl));
                items = new String[][]{{"dashboard", "Dashboard"}, {"properties", "My Properties"},
                    {"providers", "Find Providers"}, {"requests", "Service Requests"}};
            }
            case PROVIDER -> {
                register("dashboard", new ProviderDashboard(ctl));
                register("incoming", new ProviderRequestsScreen(ctl, true));
                register("jobs", new ProviderRequestsScreen(ctl, false));
                register("profile", new ProviderProfileEditScreen(ctl));
                register("reviews", new ProviderReviewsScreen(ctl));
                items = new String[][]{{"dashboard", "Dashboard"}, {"incoming", "Incoming Requests"},
                    {"jobs", "My Jobs"}, {"profile", "My Profile"}, {"reviews", "Ratings & Reviews"}};
            }
            default -> {
                register("dashboard", new AdminDashboard(ctl));
                register("approvals", new AdminProvidersScreen(ctl, true));
                register("listings", new AdminProvidersScreen(ctl, false));
                register("users", new AdminUsersScreen(ctl));
                register("allrequests", new AdminRequestsScreen(ctl));
                register("categories", new AdminCategoriesScreen(ctl));
                items = new String[][]{{"dashboard", "Dashboard"}, {"approvals", "Provider Approvals"},
                    {"listings", "Provider Listings"}, {"users", "Users"}, {"allrequests", "Service Requests"},
                    {"categories", "Service Categories"}};
            }
        }
        register("tracking", new TrackingScreen(ctl));
        if (user.role == Role.OWNER) register("providerProfile", new ProviderProfileScreen(ctl));
        for (String[] it : items) {
            JButton b = navButton(it[1]);
            b.addActionListener(e -> show(it[0], null));
            navButtons.put(it[0], b);
            nav.add(b);
            nav.add(Ui.gap(4));
        }
        side.add(nav, BorderLayout.CENTER);

        JPanel foot = Ui.box(true);
        foot.setBorder(new EmptyBorder(10, 16, 18, 16));
        foot.add(Ui.label("Demo mode: all data is simulated", 11, Font.PLAIN, new Color(0xA9BDB0)));
        foot.add(Ui.gap(8));
        JButton out = Ui.button("Log out", Ui.GOLD_BTN);
        out.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        out.addActionListener(e -> { if (Ui.confirm(this, "Log out", "Log out of Vaastu Care?")) ctl.logout(); });
        foot.add(out);
        side.add(foot, BorderLayout.SOUTH);
        return side;
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                boolean on = Boolean.TRUE.equals(getClientProperty("active"));
                if (on) { g.setColor(new Color(255, 255, 255, 30)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g.setColor(Ui.GOLD); g.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4); }
                g.setFont(Ui.font(on ? Font.BOLD : Font.PLAIN, 14));
                g.setColor(on ? Color.WHITE : new Color(0xCBD9CF));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(getText(), 18, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g.dispose();
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setPreferredSize(new Dimension(200, 42));
        return b;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(10, 28, 10, 24));
        JLabel hello = Ui.label("Signed in as " + user.name, 13, Font.PLAIN, Ui.MUTED);
        bar.add(hello, BorderLayout.WEST);
        JPanel right = Ui.flow(FlowLayout.RIGHT, 14, 0);
        bell.addActionListener(e -> showNotifications());
        right.add(bell);
        JButton profile = new JButton(user.name, Images.avatarIcon(user.name, 32));
        profile.setFont(Ui.font(Font.BOLD, 13));
        profile.setBorderPainted(false);
        profile.setContentAreaFilled(false);
        profile.setFocusPainted(false);
        profile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profile.addActionListener(e -> {
            JPopupMenu m = new JPopupMenu();
            JMenuItem acct = new JMenuItem("Account details");
            acct.addActionListener(a -> Ui.info(this, "Account details", user.name + "\n" + user.roleLabel() + "\n"
                + user.email + "\n" + user.phone + "\n" + user.city + "\n\n(Mock account - no real data)"));
            JMenuItem out = new JMenuItem("Log out");
            out.addActionListener(a -> { if (Ui.confirm(this, "Log out", "Log out of Vaastu Care?")) ctl.logout(); });
            m.add(acct); m.addSeparator(); m.add(out);
            m.show(profile, 0, profile.getHeight());
        });
        right.add(profile);
        bar.add(right, BorderLayout.EAST);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(bar, BorderLayout.CENTER);
        JSeparator sep = new JSeparator();
        sep.setForeground(Ui.BORDER);
        wrap.add(sep, BorderLayout.SOUTH);
        return wrap;
    }

    // ---------------------------------------------------------------- navigation
    public void show(String id, Object arg) {
        Screen s = screens.get(id);
        if (s == null) return;
        s.refresh(arg);
        pages.show(content, id);
        active = id;
        String highlight = switch (id) {
            case "tracking" -> user.role == Role.OWNER ? "requests" : user.role == Role.PROVIDER ? "jobs" : "allrequests";
            case "providerProfile" -> "providers";
            default -> id;
        };
        navButtons.forEach((k, b) -> { b.putClientProperty("active", k.equals(highlight)); b.repaint(); });
        refreshBell();
    }

    public void refreshBell() { bell.setCount(ctl.unreadCount()); }

    private void showNotifications() {
        JPopupMenu pop = new JPopupMenu();
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(380, 380));
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Color.WHITE);
        head.setBorder(new EmptyBorder(12, 14, 8, 10));
        head.add(Ui.h2("Notifications"), BorderLayout.WEST);
        JButton all = Ui.button("Mark all read", Ui.GHOST);
        all.addActionListener(e -> { ctl.markAllRead(); pop.setVisible(false); ctl.toast("All notifications marked as read."); });
        head.add(all, BorderLayout.EAST);
        p.add(head, BorderLayout.NORTH);
        JPanel list = Ui.box(true);
        list.setBackground(Color.WHITE);
        list.setOpaque(true);
        java.util.List<Notification> ns = ctl.data().getNotifications(user.id);
        if (ns.isEmpty()) list.add(Ui.emptyState("You're all caught up", "New updates will appear here."));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM, h:mm a");
        for (Notification n : ns) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(n.read ? Color.WHITE : Ui.GOLD_SOFT);
            row.setBorder(new EmptyBorder(10, 14, 10, 14));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            row.add(Ui.wrapped(n.text, 270, Ui.TEXT), BorderLayout.CENTER);
            row.add(Ui.muted(n.time.format(fmt)), BorderLayout.SOUTH);
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    ctl.markRead(n);
                    pop.setVisible(false);
                    if (n.requestId != null) ctl.openTracking(n.requestId);
                }
            });
            list.add(row);
        }
        p.add(Ui.scroll(list), BorderLayout.CENTER);
        pop.add(p);
        pop.show(bell, bell.getWidth() - 380, bell.getHeight());
    }

    /** Bell icon with an unread-count badge. */
    private static class BellButton extends JButton {
        private int count;
        BellButton() {
            setPreferredSize(new Dimension(40, 40));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Notifications");
        }
        void setCount(int c) { count = c; repaint(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Ui.GREEN_DARK);
            Path2D b = new Path2D.Double();
            b.moveTo(11, 26); b.curveTo(13, 24, 13, 20, 13, 17); b.curveTo(13, 11, 17, 9, 20, 9);
            b.curveTo(23, 9, 27, 11, 27, 17); b.curveTo(27, 20, 27, 24, 29, 26); b.closePath();
            g.fill(b);
            g.fillOval(17, 27, 6, 5);
            g.fillRect(19, 6, 2, 4);
            if (count > 0) {
                g.setColor(Ui.DANGER);
                g.fillOval(22, 3, 17, 17);
                g.setColor(Color.WHITE);
                g.setFont(Ui.font(Font.BOLD, 10));
                String s = String.valueOf(Math.min(count, 9));
                g.drawString(s, 30 - g.getFontMetrics().stringWidth(s) / 2, 15);
            }
            g.dispose();
        }
    }
}
