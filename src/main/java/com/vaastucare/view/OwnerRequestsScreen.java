package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OwnerRequestsScreen extends JPanel implements Screen {
    private final AppController ctl;
    private final DataTable<ServiceRequest> table;

    public OwnerRequestsScreen(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
        table = new DataTable<>(new String[]{"Request ID", "Property", "Provider", "Category", "Date", "Status", "Response"}, r -> new Object[]{
            r.id, ctl.data().getProperty(r.propertyId).name, ctl.data().getUser(r.providerId).name, r.category,
            r.preferredDate.format(Widgets.DATE), r.status, r.responseLabel()});
        table.setFilter("Status", new String[]{"All statuses", Status.SCHEDULED, Status.IN_PROGRESS, Status.COMPLETED, Status.REJECTED}, 5);
        table.setEmptyText("No service requests yet", "Choose a provider and send your first request.");
        table.setOnOpen(r -> ctl.openTracking(r.id));
        table.addAction("View / track", Ui.PRIMARY, null, r -> ctl.openTracking(r.id));
        table.addAction("Rate provider", Ui.GOLD_BTN, r -> Status.COMPLETED.equals(r.status) && !r.reviewed,
            r -> Dialogs.reviewForm(ctl, this, r, () -> refresh(null)));
        JButton add = Ui.button("+ New request", Ui.PRIMARY);
        add.addActionListener(e -> Dialogs.requestForm(ctl, this, null, null, () -> refresh(null)));
        add(Ui.pageHeader("Service Requests", "Track every request you have made. Double-click a row for details.", add), BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    @Override public void refresh(Object arg) {
        List<ServiceRequest> list = ctl.data().getRequestsByOwner(ctl.user().id);
        list.sort((a, b) -> b.id.compareTo(a.id));
        table.setItems(list);
    }
}
