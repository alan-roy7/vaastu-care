package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.regex.Pattern;

/** Registration for property owners and service providers (provider accounts start as Pending). */
public class RegisterView extends JPanel {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private final AppController ctl;
    private final MainFrame frame;
    private final JComboBox<String> roleBox = Ui.combo("Property Owner", "Service Provider");
    private final JTextField name = Ui.field(18), email = Ui.field(18), username = Ui.field(18), phone = Ui.field(18),
        city = Ui.field(18), area = Ui.field(18);
    private final JPasswordField pass = Ui.passwordField(18), confirm = Ui.passwordField(18);
    private final JComboBox<String> category = Ui.combo();
    private final JSpinner experience = new JSpinner(new SpinnerNumberModel(1, 0, 50, 1));
    private final JTextArea description = Ui.area(3, 18);
    private final JPanel providerFields = Ui.box(true);
    private final JLabel error = Ui.label(" ", 12, Font.BOLD, Ui.DANGER);

    public RegisterView(AppController ctl, MainFrame frame) {
        super(new GridBagLayout());
        this.ctl = ctl; this.frame = frame;
        setBackground(Ui.BG);
        Ui.RoundedPanel card = Ui.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 34, 22, 34));
        card.add(Ui.title("Create your account"));
        card.add(Ui.gap(4));
        card.add(Ui.muted("Owner accounts are active immediately. Provider accounts need admin approval (simulated)."));
        card.add(Ui.gap(8));

        Ui.Form f = new Ui.Form();
        f.add("I am registering as", roleBox);
        f.addPair("Full name / business name", name, "Phone", phone);
        f.addPair("Email", email, "Username", username);
        f.addPair("Password (min 6 characters)", pass, "Confirm password", confirm);
        f.add("City", city);
        card.add(f);

        Ui.Form pf = new Ui.Form();
        pf.addPair("Service category", category, "Years of experience", experience);
        pf.add("Service area (towns or districts)", area);
        pf.add("Short description of your services", Ui.areaScroll(description));
        providerFields.add(pf);
        providerFields.setVisible(false);
        card.add(providerFields);
        card.add(Ui.gap(8));
        card.add(error);
        card.add(Ui.gap(6));

        JPanel buttons = Ui.flow(FlowLayout.LEFT, 8, 0);
        JButton create = Ui.button("Create account", Ui.PRIMARY), back = Ui.button("Back to login", Ui.OUTLINE);
        create.addActionListener(e -> submit());
        back.addActionListener(e -> frame.showLogin());
        buttons.add(create);
        buttons.add(back);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(buttons);
        roleBox.addActionListener(e -> {
            providerFields.setVisible(roleBox.getSelectedIndex() == 1);
            card.revalidate();
            card.repaint();
        });
        add(card);
    }

    public void selectRole(int index) { roleBox.setSelectedIndex(index); }

    public void reset() {
        for (JTextField t : new JTextField[]{name, email, username, phone, city, area, pass, confirm}) t.setText("");
        description.setText("");
        error.setText(" ");
        roleBox.setSelectedIndex(0);
        category.removeAllItems();
        ctl.data().getCategories().stream().filter(c -> c.active).forEach(c -> category.addItem(c.name));
    }

    private void submit() {
        boolean provider = roleBox.getSelectedIndex() == 1;
        String err = null;
        if (name.getText().trim().length() < 3) err = "Enter a name with at least 3 characters.";
        else if (!EMAIL.matcher(email.getText().trim()).matches()) err = "Enter a valid email address.";
        else if (username.getText().trim().length() < 3 || username.getText().contains(" ")) err = "Username needs 3+ characters and no spaces.";
        else if (!phone.getText().trim().matches("[+0-9 ()-]{7,20}")) err = "Enter a valid phone number (digits, +, spaces).";
        else if (new String(pass.getPassword()).length() < 6) err = "Password must be at least 6 characters.";
        else if (!new String(pass.getPassword()).equals(new String(confirm.getPassword()))) err = "Passwords do not match.";
        else if (city.getText().trim().isEmpty()) err = "Enter your city.";
        else if (provider && (area.getText().trim().isEmpty() || description.getText().trim().length() < 10))
            err = "Providers must enter a service area and a description (10+ characters).";
        if (err != null) { error.setText(err); return; }
        error.setText(" ");

        User u;
        if (provider) {
            u = new Provider(null, name.getText().trim(), email.getText().trim(), username.getText().trim(),
                new String(pass.getPassword()), phone.getText().trim(), city.getText().trim(),
                String.valueOf(category.getSelectedItem()), area.getText().trim(), (Integer) experience.getValue(),
                description.getText().trim(), "To be confirmed", Provider.PENDING);
            ((Provider) u).appliedOn = java.time.LocalDate.now();
        } else {
            u = new User(null, Role.OWNER, name.getText().trim(), email.getText().trim(), username.getText().trim(),
                new String(pass.getPassword()), phone.getText().trim(), city.getText().trim());
        }
        frame.loading("Creating account...", () -> {
            String problem = ctl.register(u);
            if (problem != null) { error.setText(problem); frame.toast("Registration failed", true); return; }
            frame.showLogin();
            frame.toast(provider ? "Application submitted. An admin must approve it (simulated)." : "Account created. You can log in now.", false);
        });
    }
}
