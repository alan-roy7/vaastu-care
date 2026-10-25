package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/** Incoming requests (accept/reject) or all jobs (start, add proof, complete). */
public class ProviderRequestsScreen extends JPanel implements Screen {
    private final AppController ctl;
    private final boolean incoming;
    private final DataTable<ServiceRequest> table;

    public ProviderRequestsScreen(AppController ctl, boolean incoming) {
        super(new BorderLayout());
        this.ctl = ctl; this.incoming = incoming;
        setOpaque(false);
        table = new DataTable<>(new String[]{"Request ID", "Property", "Owner", "Category", "Date", "Status"}, r -> new Object[]{
            r.id, ctl.data().getProperty(r.propertyId).name + ", " + ctl.data().getProperty(r.propertyId).city,
            ctl.data().getUser(r.ownerId).name, r.category, r.preferredDate.format(Widgets.DATE), r.status});
        table.setOnOpen(r -> ctl.openTracking(r.id));
        table.addAction("View details", Ui.OUTLINE, null, r -> ctl.openTracking(r.id));
        if (incoming) {
            table.setEmptyText("No new requests", "New requests from owners will appear here.");
            table.addAction("Accept", Ui.PRIMARY, ServiceRequest::awaitingProvider, r -> {
                if (Ui.confirm(this, "Accept request", "Accept " + r.id + " (" + r.category + ")?")) {
                    ctl.acceptRequest(r); ctl.toast(r.id + " accepted. It is now in My Jobs."); refresh(null); }
            });
            table.addAction("Reject", Ui.DANGER_BTN, ServiceRequest::awaitingProvider, r -> {
                String why = Dialogs.promptReason(this, "Reject request", "Reason for rejecting " + r.id + ":");
                if (why != null) { ctl.rejectRequest(r, why); ctl.toast(r.id + " rejected."); refresh(null); }
            });
        } else {
            table.setFilter("Status", new String[]{"All statuses", Status.SCHEDULED, Status.IN_PROGRESS, Status.COMPLETED, Status.REJECTED}, 5);
            table.setEmptyText("No jobs yet", "Accepted requests will appear here.");
            table.addAction("Start work", Ui.PRIMARY, r -> Status.SCHEDULED.equals(r.status) && r.accepted, r -> {
                if (Ui.confirm(this, "Start work", "Mark " + r.id + " as In Progress?")) {
                    ctl.startWork(r); ctl.toast("Status updated to In Progress."); refresh(null); }
            });
            table.addAction("Add proof / complete", Ui.GOLD_BTN, r -> Status.IN_PROGRESS.equals(r.status), r -> ctl.openTracking(r.id));
        }
        add(Ui.pageHeader(incoming ? "Incoming Requests" : "My Jobs",
            incoming ? "Accept or reject new requests from property owners." : "Accepted work and its progress. Double-click a row for details."), BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    @Override public void refresh(Object arg) {
        List<ServiceRequest> list = ctl.data().getRequestsByProvider(ctl.user().id).stream()
            .filter(r -> incoming == r.awaitingProvider()).sorted((a, b) -> b.id.compareTo(a.id)).collect(Collectors.toList());
        table.setItems(list);
    }
}
