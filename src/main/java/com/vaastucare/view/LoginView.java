package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.prefs.Preferences;

public class LoginView extends JPanel {
    private final AppController ctl;
    private final MainFrame frame;
    private final JTextField userField = Ui.field(22);
    private final JPasswordField passField = Ui.passwordField(22);
    private final JCheckBox show = new JCheckBox("Show password");
    private final JCheckBox remember = new JCheckBox("Remember me");
    private final JLabel error = Ui.label(" ", 12, Font.BOLD, Ui.DANGER);
    private final Preferences prefs = Preferences.userNodeForPackage(LoginView.class);

    public LoginView(AppController ctl, MainFrame frame) {
        super(new GridLayout(1, 2));
        this.ctl = ctl; this.frame = frame;
        add(brandPanel());
        add(formPanel());
    }

    private JPanel brandPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Ui.GREEN_DARK);
        JPanel in = Ui.box(true);
        in.add(Ui.label("VAASTU CARE", 40, Font.BOLD, Ui.GOLD_SOFT));
        in.getComponent(0).setFont(Ui.serif(Font.BOLD, 42));
        in.add(Ui.gap(6));
        in.add(Ui.label("Property Care Management System", 16, Font.PLAIN, Ui.GOLD));
        in.add(Ui.gap(26));
        JLabel tag = Ui.wrapped("Look after your home and land from anywhere. Hire verified cleaners, watchmen, electricians and more, and see photo proof when the work is done.", 380, new Color(0xD9E4DC));
        in.add(tag);
        in.add(Ui.gap(34));
        Ui.RoundedPanel demo = new Ui.RoundedPanel(16, new Color(255, 255, 255, 22), new Color(255, 255, 255, 60));
        demo.setLayout(new BoxLayout(demo, BoxLayout.Y_AXIS));
        demo.setBorder(new EmptyBorder(16, 18, 16, 18));
        demo.add(Ui.label("DEMO ACCOUNTS (mock login, no real data)", 11, Font.BOLD, Ui.GOLD));
        demo.add(Ui.gap(8));
        demo.add(demoRow("Owner", "owner", "owner123"));
        demo.add(demoRow("Service Provider", "provider", "provider123"));
        demo.add(demoRow("Admin", "admin", "admin123"));
        demo.add(Ui.gap(6));
        demo.add(Ui.label("Click a row to fill the form.", 11, Font.PLAIN, new Color(0xBFCFC5)));
        in.add(demo);
        p.add(in);
        return p;
    }

    private JComponent demoRow(String role, String user, String pass) {
        JButton b = new JButton("<html><b>" + role + "</b> &nbsp; " + user + " / " + pass + "</html>");
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> { userField.setText(user); passField.setText(pass); error.setText(" "); });
        return b;
    }

    private JPanel formPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Ui.BG);
        Ui.RoundedPanel card = Ui.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 34, 28, 34));
        card.add(Ui.title("Welcome back"));
        card.add(Ui.gap(4));
        card.add(Ui.muted("Sign in to manage your properties and services."));
        card.add(Ui.gap(18));
        Ui.Form f = new Ui.Form();
        f.add("Email or username", userField);
        f.add("Password", passField);
        card.add(f);
        show.setOpaque(false);
        remember.setOpaque(false);
        show.addActionListener(e -> passField.setEchoChar(show.isSelected() ? (char) 0 : '\u2022'));
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 0, 0, 0));
        row.add(remember, BorderLayout.WEST);
        row.add(show, BorderLayout.EAST);
        card.add(row);
        card.add(Ui.gap(6));
        card.add(error);
        card.add(Ui.gap(8));
        JButton login = Ui.button("Log in", Ui.PRIMARY);
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        login.addActionListener(e -> doLogin());
        passField.addActionListener(e -> doLogin());
        userField.addActionListener(e -> passField.requestFocus());
        card.add(login);
        card.add(Ui.gap(16));
        JPanel reg = Ui.flow(FlowLayout.LEFT, 6, 0);
        reg.add(Ui.muted("New to Vaastu Care?"));
        JButton b1 = Ui.button("Register as Owner", Ui.GHOST), b2 = Ui.button("Register as Provider", Ui.GHOST);
        b1.addActionListener(e -> frame.showRegister(0));
        b2.addActionListener(e -> frame.showRegister(1));
        reg.add(b1);
        reg.add(b2);
        reg.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(reg);
        card.setPreferredSize(new Dimension(460, 470));
        outer.add(card);
        return outer;
    }

    public void reset() {
        passField.setText("");
        error.setText(" ");
        String saved = prefs.get("remembered", "");
        userField.setText(saved);
        remember.setSelected(!saved.isEmpty());
    }

    public void prefill(String username) { userField.setText(username); passField.setText(""); }

    private void doLogin() {
        String u = userField.getText().trim(), p = new String(passField.getPassword());
        if (u.isEmpty() || p.isEmpty()) { error.setText("Enter your username/email and password."); return; }
        error.setText(" ");
        frame.loading("Signing in...", () -> {
            User user = ctl.login(u, p);
            if (user == null) { error.setText("Incorrect username/email or password."); frame.toast("Login failed", true); return; }
            if (remember.isSelected()) prefs.put("remembered", u); else prefs.remove("remembered");
            frame.showApp(user);
            frame.toast("Welcome, " + user.name + " (" + user.roleLabel() + ")", false);
        });
    }
}
