package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import java.awt.*;

/** Add, edit, enable/disable and delete service categories. */
public class AdminCategoriesScreen extends JPanel implements Screen {
    private final AppController ctl;
    private final DataTable<Category> table;

    public AdminCategoriesScreen(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
        table = new DataTable<>(new String[]{"ID", "Category", "Description", "Providers", "Status"}, c -> new Object[]{
            c.id, c.name, c.description,
            ctl.data().getProviders().stream().filter(p -> c.name.equals(p.category)).count(), c.active ? "Active" : "Disabled"});
        table.setEmptyText("No categories", "Add a category to get started.");
        JButton add = Ui.button("+ Add category", Ui.GOLD_BTN);
        add.addActionListener(e -> form(null));
        table.addTopComponent(add);
        table.setOnOpen(this::form);
        table.addAction("Edit", Ui.OUTLINE, null, this::form);
        table.addAction("Enable / disable", Ui.PRIMARY, null, c -> {
            c.active = !c.active;
            ctl.saveCategory(c);
            ctl.toast(c.name + (c.active ? " enabled." : " disabled. It no longer appears in new requests."));
            refresh(null);
        });
        table.addAction("Delete", Ui.DANGER_BTN, null, c -> {
            if (!Ui.confirm(this, "Delete category", "Delete \"" + c.name + "\"?")) return;
            String err = ctl.deleteCategory(c);
            if (err != null) Ui.error(this, err); else { ctl.toast("Category deleted."); refresh(null); }
        });
        add(Ui.pageHeader("Service Categories", "Manage the categories owners can request and providers can offer."), BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    private void form(Category existing) {
        JTextField name = Ui.field(22);
        JTextArea desc = Ui.area(3, 22);
        JLabel err = Ui.label(" ", 12, Font.BOLD, Ui.DANGER);
        if (existing != null) { name.setText(existing.name); name.setEditable(false); desc.setText(existing.description); }
        Ui.Form f = new Ui.Form();
        f.add(existing == null ? "Category name" : "Category name (fixed once created)", name);
        f.add("Description", Ui.areaScroll(desc));
        JPanel c = new JPanel(new BorderLayout());
        c.setOpaque(false);
        c.add(f, BorderLayout.CENTER);
        c.add(err, BorderLayout.SOUTH);
        c.setPreferredSize(new Dimension(420, 230));
        JButton save = Ui.button("Save", Ui.PRIMARY), cancel = Ui.button("Cancel", Ui.OUTLINE);
        JDialog d = Ui.dialog(this, existing == null ? "Add category" : "Edit category", c, cancel, save);
        cancel.addActionListener(e -> d.dispose());
        save.addActionListener(e -> {
            String n = name.getText().trim();
            if (n.length() < 3) { err.setText("Enter a category name (3+ characters)."); return; }
            if (existing == null && ctl.data().getCategories().stream().anyMatch(x -> x.name.equalsIgnoreCase(n))) { err.setText("That category already exists."); return; }
            if (desc.getText().trim().length() < 5) { err.setText("Add a short description."); return; }
            if (existing == null) ctl.saveCategory(new Category(null, n, desc.getText().trim()));
            else { existing.description = desc.getText().trim(); ctl.saveCategory(existing); }
            d.dispose();
            ctl.toast(existing == null ? "Category added." : "Category updated.");
            refresh(null);
        });
        d.setVisible(true);
    }

    @Override public void refresh(Object arg) { table.setItems(ctl.data().getCategories()); }
}
