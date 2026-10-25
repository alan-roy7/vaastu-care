package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Small shared pieces used by several dashboards. */
public final class Widgets {
    private Widgets() {}
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a");

    /** A clickable row with a coloured dot, a main line and a small second line. */
    public static JComponent row(String primary, String secondary, Color dot, Runnable onClick) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(9, 0, 9, 0));
        JComponent d = new JComponent() {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(dot);
                g.fillOval(0, 4, 10, 10);
                g.dispose();
            }
        };
        d.setPreferredSize(new Dimension(12, 20));
        p.add(d, BorderLayout.WEST);
        JPanel t = Ui.box(true);
        t.add(Ui.label(primary, 13, Font.BOLD, Ui.TEXT));
        t.add(Ui.muted(secondary));
        p.add(t, BorderLayout.CENTER);
        if (onClick != null) {
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            p.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { onClick.run(); } });
        }
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return p;
    }

    private static JPanel listCard(String title) {
        Ui.RoundedPanel c = Ui.card();
        c.add(Ui.h2(title), BorderLayout.NORTH);
        return c;
    }

    /** Latest status-history events across the given requests. */
    public static JComponent activityCard(AppController ctl, List<ServiceRequest> reqs, String title) {
        JPanel card = listCard(title);
        record Ev(ServiceRequest r, StatusEntry e) {}
        List<Ev> evs = new ArrayList<>();
        for (ServiceRequest r : reqs) for (StatusEntry e : r.history) evs.add(new Ev(r, e));
        evs.sort(Comparator.comparing((Ev x) -> x.e().time).reversed());
        JPanel list = Ui.box(true);
        list.setBorder(new EmptyBorder(10, 0, 0, 0));
        if (evs.isEmpty()) list.add(Ui.emptyState("No activity yet", "Updates on your requests will show up here."));
        for (int i = 0; i < Math.min(6, evs.size()); i++) {
            Ev x = evs.get(i);
            list.add(row(x.r().id + " · " + x.e().note, x.e().by + " · " + x.e().time.format(DATETIME),
                Ui.statusColor(x.e().status), () -> ctl.openTracking(x.r().id)));
        }
        card.add(Ui.scroll(list), BorderLayout.CENTER);
        return card;
    }

    /** Scheduled requests ordered by date. */
    public static JComponent upcomingCard(AppController ctl, List<ServiceRequest> reqs, String title, boolean showProvider) {
        JPanel card = listCard(title);
        JPanel list = Ui.box(true);
        list.setBorder(new EmptyBorder(10, 0, 0, 0));
        List<ServiceRequest> up = new ArrayList<>();
        for (ServiceRequest r : reqs) if (Status.SCHEDULED.equals(r.status)) up.add(r);
        up.sort(Comparator.comparing(r -> r.preferredDate));
        if (up.isEmpty()) list.add(Ui.emptyState("Nothing scheduled", "Scheduled services will appear here."));
        for (ServiceRequest r : up) {
            Property p = ctl.data().getProperty(r.propertyId);
            String who = showProvider ? ctl.data().getUser(r.providerId).name : ctl.data().getUser(r.ownerId).name;
            list.add(row(r.preferredDate.format(DATE) + " · " + r.category,
                (p == null ? "" : p.name + " · ") + who + (r.accepted ? "" : " · awaiting response"),
                r.accepted ? Ui.statusColor(Status.SCHEDULED) : Ui.WARN, () -> ctl.openTracking(r.id)));
        }
        card.add(Ui.scroll(list), BorderLayout.CENTER);
        return card;
    }

    /** Label/value pairs in two columns. */
    public static JPanel keyValue(String[][] rows, int valueWidth) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(4, 0, 4, 14);
        int y = 0;
        for (String[] r : rows) {
            g.gridy = y++;
            g.gridx = 0;
            p.add(Ui.label(r[0], 12, Font.BOLD, Ui.MUTED), g);
            g.gridx = 1;
            g.insets = new Insets(4, 0, 4, 0);
            p.add(Ui.wrapped(r[1] == null || r[1].isEmpty() ? "-" : r[1], valueWidth, Ui.TEXT), g);
            g.insets = new Insets(4, 0, 4, 14);
        }
        return p;
    }

    public static JComponent verifiedBadge() {
        JLabel l = Ui.badge("\u2713 Verified", new Color(0x7A5E22), Ui.GOLD_SOFT);
        l.setToolTipText("Approved by an admin (simulated verification in this demo)");
        return l;
    }

    public static String shortName(String full) {
        String[] p = full.split(" ");
        return p.length > 1 ? p[0] + " " + p[p.length - 1].charAt(0) + "." : full;
    }

    /** Simple bar chart of request counts per status. */
    public static JComponent statusBars(List<ServiceRequest> reqs) {
        int[] counts = new int[Status.ALL.length];
        for (ServiceRequest r : reqs) for (int i = 0; i < counts.length; i++) if (Status.ALL[i].equals(r.status)) counts[i]++;
        int max = Math.max(1, java.util.Arrays.stream(counts).max().orElse(1));
        return new JComponent() {
            { setPreferredSize(new Dimension(300, 190)); }
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int n = counts.length, slot = getWidth() / n, base = getHeight() - 30;
                for (int i = 0; i < n; i++) {
                    int h = (int) ((base - 30) * counts[i] / (double) max);
                    int x = i * slot + slot / 2 - 24;
                    g.setColor(Ui.statusColor(Status.ALL[i]));
                    g.fillRoundRect(x, base - h, 48, Math.max(h, 4), 10, 10);
                    g.setFont(Ui.font(Font.BOLD, 14));
                    g.setColor(Ui.TEXT);
                    String c = String.valueOf(counts[i]);
                    g.drawString(c, x + 24 - g.getFontMetrics().stringWidth(c) / 2, base - h - 6);
                    g.setFont(Ui.font(Font.PLAIN, 12));
                    g.setColor(Ui.MUTED);
                    g.drawString(Status.ALL[i], x + 24 - g.getFontMetrics().stringWidth(Status.ALL[i]) / 2, getHeight() - 8);
                }
                g.dispose();
            }
        };
    }
}
