package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import java.awt.*;

public class AdminUsersScreen extends JPanel implements Screen {
    private final AppController ctl;
    private final DataTable<User> table;

    public AdminUsersScreen(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
        table = new DataTable<>(new String[]{"ID", "Name", "Role", "Email", "City", "Phone"}, u -> new Object[]{
            u.id, u.name, u.roleLabel(), u.email, u.city, u.phone});
        table.setFilter("Role", new String[]{"All roles", "Property Owner", "Service Provider", "Administrator"}, 2);
        table.setEmptyText("No users found", "Try another search or role.");
        table.addAction("View details", Ui.PRIMARY, null, this::show);
        table.setOnOpen(this::show);
        add(Ui.pageHeader("Users", "All registered owners, providers and administrators (mock accounts)."), BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    private void show(User u) {
        if (u instanceof Provider p) { Dialogs.providerDetails(ctl, this, p); return; }
        long props = ctl.data().getAllRequests().stream().filter(r -> r.ownerId.equals(u.id)).count();
        Ui.info(this, "User details", u.name + "\n" + u.roleLabel() + "\n" + u.email + "\n" + u.phone + "\nLives in: " + u.city
            + (u.role == Role.OWNER ? "\nProperties: " + ctl.data().getPropertiesByOwner(u.id).size() + "\nService requests: " + props : ""));
    }

    @Override public void refresh(Object arg) { table.setItems(ctl.data().getAllUsers()); }
}
