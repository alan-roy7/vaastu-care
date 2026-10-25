package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Directory of admin-approved providers with search, category, rating filters and sorting. */
public class ProviderDirectoryScreen extends JPanel implements Screen {
    private final AppController ctl;
    private final JTextField search = Ui.field(16);
    private final JComboBox<String> category = Ui.combo("All categories");
    private final JComboBox<String> rating = Ui.combo("Any rating", "3.0 and above", "4.0 and above", "4.5 and above");
    private final JComboBox<String> sort = Ui.combo("Top rated", "Most reviewed", "Name A-Z");
    private final JPanel holder = new JPanel(new BorderLayout());
    private boolean loading;

    public ProviderDirectoryScreen(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
        holder.setOpaque(false);
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(Ui.pageHeader("Verified Service Providers", "Only admin-approved providers are listed here.", new JComponent[0]), BorderLayout.NORTH);
        JPanel f = Ui.flow(FlowLayout.LEFT, 8, 0);
        f.setBorder(new EmptyBorder(0, 0, 12, 0));
        f.add(Ui.label("Search", 12, Font.BOLD, Ui.MUTED)); f.add(search);
        f.add(Ui.label("Category", 12, Font.BOLD, Ui.MUTED)); f.add(category);
        f.add(Ui.label("Rating", 12, Font.BOLD, Ui.MUTED)); f.add(rating);
        f.add(Ui.label("Sort", 12, Font.BOLD, Ui.MUTED)); f.add(sort);
        JButton clear = Ui.button("Clear filters", Ui.GHOST);
        clear.addActionListener(e -> clearFilters());
        f.add(clear);
        north.add(f, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        Ui.VPanel v = new Ui.VPanel(new BorderLayout());
        v.add(holder, BorderLayout.NORTH);
        add(Ui.scroll(v), BorderLayout.CENTER);
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { cards(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { cards(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { cards(); }
        });
        category.addActionListener(e -> cards());
        rating.addActionListener(e -> cards());
        sort.addActionListener(e -> cards());
    }

    private void clearFilters() {
        loading = true;
        search.setText(""); category.setSelectedIndex(0); rating.setSelectedIndex(0); sort.setSelectedIndex(0);
        loading = false;
        cards();
    }

    @Override public void refresh(Object arg) {
        loading = true;
        Object keep = category.getSelectedItem();
        category.removeAllItems();
        category.addItem("All categories");
        ctl.data().getCategories().stream().filter(c -> c.active).forEach(c -> category.addItem(c.name));
        category.setSelectedItem(keep == null ? "All categories" : keep);
        loading = false;
        cards();
    }

    private void cards() {
        if (loading) return;
        holder.removeAll();
        String q = search.getText().trim().toLowerCase(), cat = String.valueOf(category.getSelectedItem());
        double min = new double[]{0, 3, 4, 4.5}[rating.getSelectedIndex()];
        List<Provider> list = new ArrayList<>();
        for (Provider p : ctl.data().getProviders()) {
            if (!p.isListed()) continue;
            if (!cat.equals("All categories") && !p.category.equals(cat)) continue;
            if (ctl.avgRating(p.id) < min) continue;
            if (!q.isEmpty() && !(p.name + " " + p.category + " " + p.area + " " + p.description).toLowerCase().contains(q)) continue;
            list.add(p);
        }
        switch (sort.getSelectedIndex()) {
            case 0 -> list.sort(Comparator.comparingDouble((Provider p) -> ctl.avgRating(p.id)).reversed().thenComparing(p -> -ctl.reviewCount(p.id)));
            case 1 -> list.sort(Comparator.comparingInt((Provider p) -> ctl.reviewCount(p.id)).reversed());
            default -> list.sort(Comparator.comparing(p -> p.name));
        }
        if (list.isEmpty()) {
            JPanel e = Ui.emptyState("No providers match your filters", "Try another category or lower the rating filter.");
            JButton clear = Ui.button("Clear filters", Ui.OUTLINE);
            clear.addActionListener(ev -> clearFilters());
            clear.setAlignmentX(Component.CENTER_ALIGNMENT);
            e.add(Ui.gap(12));
            e.add(clear);
            holder.add(e, BorderLayout.CENTER);
        } else {
            JPanel grid = new JPanel(new GridLayout(0, 3, 18, 18));
            grid.setOpaque(false);
            for (Provider p : list) grid.add(card(p));
            for (int i = list.size() % 3; i != 0 && i < 3; i++) grid.add(new JPanel() {{ setOpaque(false); }});
            holder.add(grid, BorderLayout.NORTH);
        }
        holder.revalidate();
        holder.repaint();
    }

    private JComponent card(Provider p) {
        Ui.RoundedPanel c = Ui.card();
        c.setLayout(new BorderLayout(0, 10));
        JPanel head = new JPanel(new BorderLayout(12, 0));
        head.setOpaque(false);
        head.add(new JLabel(Images.avatarIcon(p.name, 60)), BorderLayout.WEST);
        JPanel t = Ui.box(true);
        t.add(Ui.label(p.name, 15, Font.BOLD, Ui.GREEN_DARK));
        t.add(Ui.gap(4));
        JPanel b = Ui.flow(FlowLayout.LEFT, 6, 0);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.add(Ui.badge(p.category, Ui.GREEN, Ui.GREEN_SOFT));
        b.add(Widgets.verifiedBadge());
        t.add(b);
        head.add(t, BorderLayout.CENTER);
        c.add(head, BorderLayout.NORTH);
        JPanel mid = Ui.box(true);
        mid.add(Ui.label(ctl.ratingText(p.id), 13, Font.BOLD, Ui.GOLD.darker()));
        mid.add(Ui.gap(4));
        mid.add(Ui.muted("Service area: " + p.area));
        mid.add(Ui.gap(6));
        String d = p.description.length() > 95 ? p.description.substring(0, 92) + "..." : p.description;
        mid.add(Ui.wrapped(d, 240, Ui.TEXT));
        c.add(mid, BorderLayout.CENTER);
        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 0));
        btns.setOpaque(false);
        JButton view = Ui.button("View profile", Ui.OUTLINE), req = Ui.button("Request service", Ui.PRIMARY);
        view.addActionListener(e -> ctl.openProvider(p.id));
        req.addActionListener(e -> Dialogs.requestForm(ctl, this, p, null, () -> ctl.navigate("requests")));
        btns.add(view); btns.add(req);
        c.add(btns, BorderLayout.SOUTH);
        return c;
    }
}
