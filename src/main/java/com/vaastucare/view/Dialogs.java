package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Modal forms and detail pop-ups shared by several screens. */
public final class Dialogs {
    private Dialogs() {}
    public static final String[] PROPERTY_TYPES = {"Apartment", "Independent House", "Villa", "Farmhouse", "Agricultural Land", "Residential Plot", "Commercial Property"};

    // ------------------------------------------------------------------ property add/edit
    public static void propertyForm(AppController ctl, Component parent, Property existing, Runnable onSaved) {
        JTextField name = Ui.field(20), address = Ui.field(20), city = Ui.field(12), area = Ui.field(8);
        JComboBox<String> type = Ui.combo(PROPERTY_TYPES);
        JSpinner beds = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));
        JTextArea notes = Ui.area(3, 20);
        JLabel err = Ui.label(" ", 12, Font.BOLD, Ui.DANGER);
        if (existing != null) {
            name.setText(existing.name); address.setText(existing.address); city.setText(existing.city);
            type.setSelectedItem(existing.type); area.setText(String.valueOf(existing.areaSqft));
            beds.setValue(existing.bedrooms); notes.setText(existing.notes);
        }
        Ui.Form f = new Ui.Form();
        f.add("Property name", name);
        f.add("Address", address);
        f.addPair("City / location", city, "Property type", type);
        f.addPair("Area (sq ft)", area, "Bedrooms (0 for land)", beds);
        f.add("Additional details", Ui.areaScroll(notes));
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(f, BorderLayout.CENTER);
        content.add(err, BorderLayout.SOUTH);
        content.setPreferredSize(new Dimension(480, 400));
        JButton save = Ui.button(existing == null ? "Add property" : "Save changes", Ui.PRIMARY), cancel = Ui.button("Cancel", Ui.OUTLINE);
        JDialog d = Ui.dialog(parent, existing == null ? "Add a property" : "Edit property", content, cancel, save);
        cancel.addActionListener(e -> d.dispose());
        save.addActionListener(e -> {
            int sq;
            try { sq = Integer.parseInt(area.getText().trim().replace(",", "")); } catch (NumberFormatException ex) { sq = -1; }
            if (name.getText().trim().length() < 3) err.setText("Enter a property name (3+ characters).");
            else if (address.getText().trim().length() < 5) err.setText("Enter the address.");
            else if (city.getText().trim().isEmpty()) err.setText("Enter the city or location.");
            else if (sq <= 0) err.setText("Area must be a positive whole number.");
            else {
                Property p = existing != null ? existing : new Property(null, ctl.user().id, "", "", "", "", 0, 0, "", ctl.data().getPropertiesByOwner(ctl.user().id).size() + 3);
                p.name = name.getText().trim(); p.address = address.getText().trim(); p.city = city.getText().trim();
                p.type = String.valueOf(type.getSelectedItem()); p.areaSqft = sq; p.bedrooms = (Integer) beds.getValue();
                p.notes = notes.getText().trim();
                ctl.saveProperty(p);
                d.dispose();
                onSaved.run();
            }
        });
        d.setVisible(true);
    }

    public static void propertyView(AppController ctl, Component parent, Property p) {
        JPanel c = Ui.box(true);
        Images.ImagePanel img = new Images.ImagePanel(Images.property(p.type, p.imageSeed), 16, 190);
        img.setAlignmentX(Component.LEFT_ALIGNMENT);
        img.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        c.add(img);
        c.add(Ui.gap(12));
        JLabel n = Ui.label(p.name, 22, Font.BOLD, Ui.GREEN_DARK);
        n.setFont(Ui.serif(Font.BOLD, 22));
        c.add(n);
        c.add(Ui.gap(8));
        long reqs = ctl.data().getRequestsByOwner(p.ownerId).stream().filter(r -> r.propertyId.equals(p.id)).count();
        c.add(Widgets.keyValue(new String[][]{{"Type", p.type}, {"Address", p.address + ", " + p.city},
            {"Size", p.summary()}, {"Notes", p.notes}, {"Service requests", String.valueOf(reqs)}}, 300));
        c.setPreferredSize(new Dimension(440, 380));
        JButton close = Ui.button("Close", Ui.OUTLINE);
        JDialog d = Ui.dialog(parent, "Property details", c, close);
        close.addActionListener(e -> d.dispose());
        d.setVisible(true);
    }

    // ------------------------------------------------------------------ service request form
    public static void requestForm(AppController ctl, Component parent, Provider preselected, Property preProp, Runnable onSaved) {
        User me = ctl.user();
        List<Property> props = ctl.data().getPropertiesByOwner(me.id);
        if (props.isEmpty()) { Ui.error(parent, "Please add a property first, then request a service for it."); return; }
        JComboBox<String> propBox = Ui.combo();
        for (Property p : props) propBox.addItem(p.name + " - " + p.city);
        if (preProp != null) propBox.setSelectedIndex(Math.max(0, props.indexOf(preProp)));
        JComboBox<String> catBox = Ui.combo();
        ctl.data().getCategories().stream().filter(c -> c.active).forEach(c -> catBox.addItem(c.name));
        JComboBox<String> provBox = Ui.combo();
        List<Provider> options = new ArrayList<>();
        Runnable fill = () -> {
            options.clear();
            provBox.removeAllItems();
            String cat = String.valueOf(catBox.getSelectedItem());
            for (Provider p : ctl.data().getProviders())
                if (p.isListed() && p.category.equals(cat)) { options.add(p); provBox.addItem(p.name + "  " + ctl.ratingText(p.id)); }
        };
        catBox.addActionListener(e -> fill.run());
        if (preselected != null) catBox.setSelectedItem(preselected.category);
        fill.run();
        if (preselected != null) provBox.setSelectedIndex(Math.max(0, options.indexOf(preselected)));
        JTextArea desc = Ui.area(3, 20), notes = Ui.area(2, 20);
        Date tomorrow = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner date = new JSpinner(new SpinnerDateModel(tomorrow, null, null, java.util.Calendar.DAY_OF_MONTH));
        date.setEditor(new JSpinner.DateEditor(date, "dd MMM yyyy"));
        JLabel err = Ui.label(" ", 12, Font.BOLD, Ui.DANGER);

        Ui.Form f = new Ui.Form();
        f.add("Property", propBox);
        f.addPair("Service category", catBox, "Preferred date", date);
        f.add("Verified provider", provBox);
        f.add("Description of the work required", Ui.areaScroll(desc));
        f.add("Additional notes (optional)", Ui.areaScroll(notes));
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(f, BorderLayout.CENTER);
        content.add(err, BorderLayout.SOUTH);
        content.setPreferredSize(new Dimension(520, 500));
        JButton send = Ui.button("Send request", Ui.PRIMARY), cancel = Ui.button("Cancel", Ui.OUTLINE);
        JDialog d = Ui.dialog(parent, "Request a service", content, cancel, send);
        cancel.addActionListener(e -> d.dispose());
        send.addActionListener(e -> {
            LocalDate when = ((Date) date.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (options.isEmpty() || provBox.getSelectedIndex() < 0) err.setText("No verified provider is available in this category yet.");
            else if (desc.getText().trim().length() < 10) err.setText("Describe the work in at least 10 characters.");
            else if (when.isBefore(LocalDate.now())) err.setText("The preferred date cannot be in the past.");
            else {
                Provider prov = options.get(provBox.getSelectedIndex());
                ServiceRequest r = ctl.createRequest(props.get(propBox.getSelectedIndex()), prov.category, prov, desc.getText().trim(), when, notes.getText().trim());
                d.dispose();
                ctl.toast(r.id + " sent to " + prov.name + ". Waiting for the provider to respond.");
                onSaved.run();
            }
        });
        d.setVisible(true);
    }

    // ------------------------------------------------------------------ review form
    public static void reviewForm(AppController ctl, Component parent, ServiceRequest r, Runnable onSaved) {
        Provider p = ctl.data().getProvider(r.providerId);
        StarRating stars = new StarRating(0, true, 38);
        JTextArea text = Ui.area(4, 24);
        JLabel err = Ui.label(" ", 12, Font.BOLD, Ui.DANGER);
        JPanel c = Ui.box(true);
        c.add(Ui.label("How was the service by " + p.name + "?", 15, Font.BOLD, Ui.GREEN_DARK));
        c.add(Ui.gap(4));
        c.add(Ui.muted(r.id + " · " + r.category));
        c.add(Ui.gap(12));
        stars.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(stars);
        c.add(Ui.gap(10));
        JScrollPane sp = Ui.areaScroll(text);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(sp);
        c.add(Ui.gap(6));
        c.add(err);
        c.setPreferredSize(new Dimension(420, 270));
        JButton submit = Ui.button("Submit review", Ui.GOLD_BTN), cancel = Ui.button("Cancel", Ui.OUTLINE);
        JDialog d = Ui.dialog(parent, "Rate this service", c, cancel, submit);
        cancel.addActionListener(e -> d.dispose());
        submit.addActionListener(e -> {
            if (stars.getValue() < 1) { err.setText("Choose a star rating from 1 to 5."); return; }
            if (text.getText().trim().length() < 10) { err.setText("Write a review of at least 10 characters."); return; }
            String problem = ctl.submitReview(r, stars.getValue(), text.getText().trim());
            if (problem != null) { err.setText(problem); return; }
            d.dispose();
            ctl.toast("Review submitted. " + p.name + " now shows " + ctl.ratingText(p.id).replace("★ ", "") + ".");
            onSaved.run();
        });
        d.setVisible(true);
    }

    // ------------------------------------------------------------------ misc
    public static void photoViewer(Component parent, String ref) {
        JPanel c = new JPanel(new BorderLayout(0, 8));
        c.setOpaque(false);
        c.add(new Images.ImagePanel(Images.proof(ref), 12, 380), BorderLayout.CENTER);
        c.add(Ui.muted(ref.startsWith("DEMO:") ? "Demonstration image generated by the app - not real evidence."
            : "Photo uploaded by the provider and stored in the uploads folder."), BorderLayout.SOUTH);
        c.setPreferredSize(new Dimension(620, 430));
        JButton close = Ui.button("Close", Ui.OUTLINE);
        JDialog d = Ui.dialog(parent, "Photo proof", c, close);
        close.addActionListener(e -> d.dispose());
        d.setVisible(true);
    }

    public static String promptReason(Component parent, String title, String message) {
        String s = JOptionPane.showInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE);
        if (s == null) return null;
        if (s.trim().length() < 3) { Ui.error(parent, "Please enter a short reason (3+ characters)."); return null; }
        return s.trim();
    }

    public static void providerDetails(AppController ctl, Component parent, Provider p) {
        JPanel c = Ui.box(true);
        JPanel head = Ui.flow(FlowLayout.LEFT, 12, 0);
        head.add(new JLabel(Images.avatarIcon(p.name, 56)));
        JPanel t = Ui.box(true);
        t.add(Ui.label(p.name, 18, Font.BOLD, Ui.GREEN_DARK));
        t.add(Ui.statusBadge(p.statusLabel()));
        head.add(t);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(head);
        c.add(Ui.gap(10));
        c.add(Widgets.keyValue(new String[][]{{"Category", p.category}, {"Service area", p.area},
            {"Experience", p.experienceYears + " years"}, {"Email", p.email}, {"Phone", p.phone}, {"City", p.city},
            {"Availability", p.availability}, {"Description", p.description},
            {"Applied on", p.appliedOn.format(Widgets.DATE)},
            {"Rating", ctl.ratingText(p.id)},
            {"Review notes", p.rejectionReason}}, 320));
        c.add(Ui.gap(8));
        c.add(Ui.muted("Documents and identity checks are not part of this demo; verification is simulated."));
        c.setPreferredSize(new Dimension(480, 460));
        JButton close = Ui.button("Close", Ui.OUTLINE);
        JDialog d = Ui.dialog(parent, "Provider registration details", c, close);
        close.addActionListener(e -> d.dispose());
        d.setVisible(true);
    }

    public static void emptyBorder(JComponent c, int n) { c.setBorder(new EmptyBorder(n, n, n, n)); }
}
