package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Used twice: "Provider Approvals" (default filter Pending) and "Provider Listings" (default Approved). */
public class AdminProvidersScreen extends JPanel implements Screen {
    private final AppController ctl;
    private final boolean approvals;
    private final DataTable<Provider> table;

    public AdminProvidersScreen(AppController ctl, boolean approvals) {
        super(new BorderLayout());
        this.ctl = ctl; this.approvals = approvals;
        setOpaque(false);
        table = new DataTable<>(new String[]{"Provider", "Category", "Service area", "Experience", "Applied", "Rating", "Status"}, p -> new Object[]{
            p.name, p.category, p.area, p.experienceYears + " yrs", p.appliedOn.format(Widgets.DATE), ctl.ratingText(p.id).replace("★ ", ""), p.statusLabel()});
        table.setFilter("Status", new String[]{"All statuses", "Pending", "Approved", "Rejected", "Suspended"}, 6);
        table.setFilterSelection(approvals ? "Pending" : "Approved");
        table.setEmptyText("No providers here", "Change the status filter to see other providers.");
        table.setOnOpen(p -> Dialogs.providerDetails(ctl, this, p));
        table.addAction("View details", Ui.OUTLINE, null, p -> Dialogs.providerDetails(ctl, this, p));
        table.addAction("Approve", Ui.PRIMARY, p -> !Provider.APPROVED.equals(p.approval), p -> {
            if (Ui.confirm(this, "Approve provider", "Approve " + p.name + "? They will appear in the verified directory.")) {
                ctl.approveProvider(p); ctl.toast(p.name + " approved."); refresh(null); }
        });
        table.addAction("Reject", Ui.DANGER_BTN, p -> Provider.PENDING.equals(p.approval), p -> {
            String why = Dialogs.promptReason(this, "Reject application", "Reason for rejecting " + p.name + ":");
            if (why != null) { ctl.rejectProvider(p, why); ctl.toast(p.name + " rejected."); refresh(null); }
        });
        table.addAction("Suspend listing", Ui.GOLD_BTN, Provider::isListed, p -> {
            if (Ui.confirm(this, "Suspend listing", "Hide " + p.name + " from the directory?")) {
                ctl.setSuspended(p, true); ctl.toast("Listing suspended."); refresh(null); }
        });
        table.addAction("Reinstate", Ui.OUTLINE, p -> p.suspended, p -> { ctl.setSuspended(p, false); ctl.toast("Listing reinstated."); refresh(null); });
        add(Ui.pageHeader(approvals ? "Provider Approvals" : "Provider Listings",
            approvals ? "Review registration details, then approve or reject applications."
                : "Moderate providers shown in the verified directory."), BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    @Override public void refresh(Object arg) {
        List<Provider> list = ctl.data().getProviders();
        table.setItems(list);
    }
}
