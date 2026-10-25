package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.User;

import javax.swing.*;
import java.awt.*;

/** Top-level window: switches between login, registration and the role-based shell. */
public class MainFrame extends JFrame {
    private final AppController ctl;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final GlassLayer glass = new GlassLayer();
    private final LoginView login;
    private final RegisterView register;
    private Shell shell;

    public MainFrame(AppController ctl) {
        super("Vaastu Care - Property Care Management (frontend demo, mock data)");
        this.ctl = ctl;
        ctl.attach(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setSize(1366, 820);
        setLocationRelativeTo(null);
        setGlassPane(glass);
        glass.setVisible(true);
        login = new LoginView(ctl, this);
        register = new RegisterView(ctl, this);
        root.add(login, "login");
        root.add(register, "register");
        setContentPane(root);
        showLogin();
    }

    public Shell shell() { return shell; }

    public void showLogin() {
        if (shell != null) { root.remove(shell); shell = null; }
        login.reset();
        cards.show(root, "login");
    }
    public void showRegister(int roleIndex) { register.reset(); register.selectRole(roleIndex); cards.show(root, "register"); }

    public void showApp(User u) {
        if (shell != null) root.remove(shell);
        shell = new Shell(ctl, this, u);
        root.add(shell, "app");
        cards.show(root, "app");
        shell.show("dashboard", null);
    }

    public void toast(String msg, boolean error) { glass.showToast(msg, error); }

    /** Shows a short spinner (simulating a server call), then runs the action. */
    public void loading(String msg, Runnable done) {
        glass.showLoading(msg);
        Timer t = new Timer(650, e -> { glass.hideLoading(); done.run(); });
        t.setRepeats(false);
        t.start();
    }
}
