package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import java.awt.*;

/** The provider's own profile: contact details, service category, area and availability. */
public class ProviderProfileEditScreen extends JPanel implements Screen {
    private final AppController ctl;
    private final JTextField name = Ui.field(20), email = Ui.field(20), phone = Ui.field(14), city = Ui.field(14),
        area = Ui.field(20), availability = Ui.field(20);
    private final JComboBox<String> category = Ui.combo();
    private final JSpinner exp = new JSpinner(new SpinnerNumberModel(1, 0, 60, 1));
    private final JTextArea desc = Ui.area(4, 30);
    private final JLabel err = Ui.label(" ", 12, Font.BOLD, Ui.DANGER);
    private final JPanel statusRow = Ui.flow(FlowLayout.LEFT, 8, 0);

    public ProviderProfileEditScreen(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
        email.setEditable(false);
        add(Ui.pageHeader("My Profile", "Keep your details and service categories up to date."), BorderLayout.NORTH);
        Ui.RoundedPanel card = Ui.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusRow);
        Ui.Form f = new Ui.Form();
        f.addPair("Business / full name", name, "Email (cannot be changed)", email);
        f.addPair("Phone", phone, "City", city);
        f.addPair("Service category", category, "Years of experience", exp);
        f.addPair("Service area", area, "Availability", availability);
        f.add("About your services", Ui.areaScroll(desc));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(f);
        card.add(Ui.gap(8));
        card.add(err);
        JButton save = Ui.button("Save profile", Ui.PRIMARY);
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.addActionListener(e -> save());
        card.add(save);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH);
        JPanel hold = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hold.setOpaque(false);
        card.setPreferredSize(new Dimension(760, 520));
        hold.add(card);
        add(Ui.scroll(hold), BorderLayout.CENTER);
    }

    @Override public void refresh(Object arg) {
        Provider p = ctl.data().getProvider(ctl.user().id);
        name.setText(p.name); email.setText(p.email); phone.setText(p.phone); city.setText(p.city);
        area.setText(p.area); availability.setText(p.availability); desc.setText(p.description); exp.setValue(p.experienceYears);
        category.removeAllItems();
        ctl.data().getCategories().stream().filter(c -> c.active || c.name.equals(p.category)).forEach(c -> category.addItem(c.name));
        category.setSelectedItem(p.category);
        err.setText(" ");
        statusRow.removeAll();
        statusRow.add(Ui.label("Listing status:", 12, Font.BOLD, Ui.MUTED));
        statusRow.add(Ui.statusBadge(p.statusLabel()));
        statusRow.revalidate();
        statusRow.repaint();
    }

    private void save() {
        if (name.getText().trim().length() < 3) { err.setText("Enter a name (3+ characters)."); return; }
        if (!phone.getText().trim().matches("[+0-9 ()-]{7,20}")) { err.setText("Enter a valid phone number."); return; }
        if (area.getText().trim().isEmpty() || availability.getText().trim().isEmpty()) { err.setText("Service area and availability are required."); return; }
        if (desc.getText().trim().length() < 10) { err.setText("Write a description of at least 10 characters."); return; }
        Provider p = ctl.data().getProvider(ctl.user().id);
        p.name = name.getText().trim(); p.phone = phone.getText().trim(); p.city = city.getText().trim();
        p.category = String.valueOf(category.getSelectedItem()); p.area = area.getText().trim();
        p.availability = availability.getText().trim(); p.description = desc.getText().trim(); p.experienceYears = (Integer) exp.getValue();
        err.setText(" ");
        ctl.updateProviderProfile(p);
    }
}
