package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JPanel implements Screen {
    private final AppController ctl;

    public AdminDashboard(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
    }

    @Override public void refresh(Object arg) {
        removeAll();
        List<Provider> provs = ctl.data().getProviders();
        long pending = provs.stream().filter(p -> Provider.PENDING.equals(p.approval)).count();
        long approved = provs.stream().filter(p -> Provider.APPROVED.equals(p.approval)).count();
        List<ServiceRequest> reqs = ctl.data().getAllRequests();
        add(Ui.pageHeader("Admin Dashboard", "Platform overview and pending work."), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        JPanel stats = new JPanel(new GridLayout(2, 3, 16, 16));
        stats.setOpaque(false);
        stats.add(Ui.statCard("TOTAL REGISTERED OWNERS", String.valueOf(ctl.data().getOwners().size()), Ui.GREEN_DARK));
        stats.add(Ui.statCard("TOTAL SERVICE PROVIDERS", String.valueOf(provs.size()), Ui.GREEN));
        stats.add(Ui.statCard("PENDING PROVIDER APPROVALS", String.valueOf(pending), Ui.WARN));
        stats.add(Ui.statCard("APPROVED PROVIDERS", String.valueOf(approved), Ui.OK));
        stats.add(Ui.statCard("TOTAL SERVICE REQUESTS", String.valueOf(reqs.size()), Ui.statusColor(Status.SCHEDULED)));
        stats.add(Ui.statCard("SERVICE CATEGORIES", String.valueOf(ctl.data().getCategories().size()), Ui.GOLD.darker()));
        body.add(stats, BorderLayout.NORTH);

        JPanel cols = new JPanel(new GridLayout(1, 2, 16, 0));
        cols.setOpaque(false);
        Ui.RoundedPanel pend = Ui.card();
        pend.add(Ui.h2("Provider applications waiting for review"), BorderLayout.NORTH);
        JPanel list = Ui.box(true);
        list.setBorder(new EmptyBorder(10, 0, 0, 0));
        boolean any = false;
        for (Provider p : provs) {
            if (!Provider.PENDING.equals(p.approval)) continue;
            any = true;
            list.add(Widgets.row(p.name, p.category + " · " + p.area + " · applied " + p.appliedOn.format(Widgets.DATE), Ui.WARN, () -> ctl.navigate("approvals")));
        }
        if (!any) list.add(Ui.emptyState("No pending applications", "You're all caught up."));
        pend.add(list, BorderLayout.CENTER);
        JButton go = Ui.button("Review applications", Ui.PRIMARY);
        go.addActionListener(e -> ctl.navigate("approvals"));
        JPanel south = Ui.flow(FlowLayout.LEFT, 0, 0);
        south.add(go);
        pend.add(south, BorderLayout.SOUTH);
        cols.add(pend);

        Ui.RoundedPanel chart = Ui.card();
        chart.add(Ui.h2("Service requests by status"), BorderLayout.NORTH);
        chart.add(Widgets.statusBars(reqs), BorderLayout.CENTER);
        cols.add(chart);
        body.add(cols, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
