package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminRequestsScreen extends JPanel implements Screen {
    private final AppController ctl;
    private final DataTable<ServiceRequest> table;

    public AdminRequestsScreen(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
        table = new DataTable<>(new String[]{"Request ID", "Owner", "Provider", "Property", "Category", "Date", "Status"}, r -> new Object[]{
            r.id, ctl.data().getUser(r.ownerId).name, ctl.data().getUser(r.providerId).name,
            ctl.data().getProperty(r.propertyId).name, r.category, r.preferredDate.format(Widgets.DATE), r.status});
        table.setFilter("Status", new String[]{"All statuses", Status.SCHEDULED, Status.IN_PROGRESS, Status.COMPLETED, Status.REJECTED}, 6);
        table.setEmptyText("No requests found", "Try another search or status.");
        table.setOnOpen(r -> ctl.openTracking(r.id));
        table.addAction("View details", Ui.PRIMARY, null, r -> ctl.openTracking(r.id));
        add(Ui.pageHeader("Service Requests", "Every request across the platform (read-only for admins)."), BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    @Override public void refresh(Object arg) {
        List<ServiceRequest> list = ctl.data().getAllRequests();
        list.sort((a, b) -> b.id.compareTo(a.id));
        table.setItems(list);
    }
}
