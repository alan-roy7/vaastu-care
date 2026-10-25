package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class PropertiesScreen extends JPanel implements Screen {
    private final AppController ctl;
    private final JTextField search = Ui.field(18);
    private final JPanel holder = new JPanel(new BorderLayout());

    public PropertiesScreen(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { cards(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { cards(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { cards(); }
        });
        holder.setOpaque(false);
        JButton add = Ui.button("+ Add property", Ui.GOLD_BTN);
        add.addActionListener(e -> Dialogs.propertyForm(ctl, this, null, () -> refresh(null)));
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(Ui.pageHeader("My Properties", "Homes and land you have registered with Vaastu Care.", add), BorderLayout.NORTH);
        JPanel s = Ui.flow(FlowLayout.LEFT, 8, 0);
        s.add(Ui.label("Search", 12, Font.BOLD, Ui.MUTED));
        s.add(search);
        s.setBorder(new EmptyBorder(0, 0, 12, 0));
        north.add(s, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(Ui.scroll(wrap()), BorderLayout.CENTER);
    }

    private JComponent wrap() {
        Ui.VPanel v = new Ui.VPanel(new BorderLayout());
        v.add(holder, BorderLayout.NORTH);
        return v;
    }

    @Override public void refresh(Object arg) { cards(); }

    private void cards() {
        holder.removeAll();
        String q = search.getText().trim().toLowerCase();
        List<Property> list = ctl.data().getPropertiesByOwner(ctl.user().id).stream()
            .filter(p -> q.isEmpty() || (p.name + " " + p.city + " " + p.type + " " + p.address).toLowerCase().contains(q))
            .collect(Collectors.toList());
        if (list.isEmpty()) {
            JPanel e = Ui.emptyState(q.isEmpty() ? "No properties yet" : "No matching properties",
                q.isEmpty() ? "Add your first property to start requesting services." : "Try a different search.");
            holder.add(e, BorderLayout.CENTER);
        } else {
            JPanel grid = new JPanel(new GridLayout(0, 3, 18, 18));
            grid.setOpaque(false);
            for (Property p : list) grid.add(card(p));
            for (int i = list.size() % 3; i != 0 && i < 3; i++) grid.add(new JPanel() {{ setOpaque(false); }});
            holder.add(grid, BorderLayout.NORTH);
        }
        holder.revalidate();
        holder.repaint();
    }

    private JComponent card(Property p) {
        Ui.RoundedPanel c = Ui.card();
        c.setLayout(new BorderLayout(0, 10));
        c.setBorder(new EmptyBorder(12, 12, 14, 12));
        c.add(new Images.ImagePanel(Images.property(p.type, p.imageSeed), 14, 150), BorderLayout.NORTH);
        JPanel info = Ui.box(true);
        JLabel name = Ui.label(p.name, 17, Font.BOLD, Ui.GREEN_DARK);
        name.setFont(Ui.serif(Font.BOLD, 17));
        info.add(name);
        info.add(Ui.gap(2));
        info.add(Ui.muted(p.address + ", " + p.city));
        info.add(Ui.gap(8));
        JPanel row = Ui.flow(FlowLayout.LEFT, 8, 0);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(Ui.badge(p.type, Ui.GREEN, Ui.GREEN_SOFT));
        row.add(Ui.muted(p.summary()));
        info.add(row);
        c.add(info, BorderLayout.CENTER);
        JPanel btns = new JPanel(new GridLayout(1, 3, 8, 0));
        btns.setOpaque(false);
        JButton view = Ui.button("View", Ui.OUTLINE), edit = Ui.button("Edit", Ui.PRIMARY), req = Ui.button("Request", Ui.GOLD_BTN);
        view.addActionListener(e -> Dialogs.propertyView(ctl, this, p));
        edit.addActionListener(e -> Dialogs.propertyForm(ctl, this, p, () -> refresh(null)));
        req.addActionListener(e -> Dialogs.requestForm(ctl, this, null, p, () -> ctl.navigate("requests")));
        btns.add(view); btns.add(edit); btns.add(req);
        c.add(btns, BorderLayout.SOUTH);
        return c;
    }
}
