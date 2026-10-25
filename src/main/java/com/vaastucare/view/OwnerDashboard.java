package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OwnerDashboard extends JPanel implements Screen {
    private final AppController ctl;

    public OwnerDashboard(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
    }

    @Override public void refresh(Object arg) {
        removeAll();
        User me = ctl.user();
        List<ServiceRequest> reqs = ctl.data().getRequestsByOwner(me.id);
        long active = reqs.stream().filter(r -> (Status.SCHEDULED.equals(r.status) && r.accepted) || Status.IN_PROGRESS.equals(r.status)).count();
        long done = reqs.stream().filter(r -> Status.COMPLETED.equals(r.status)).count();
        long pending = reqs.stream().filter(ServiceRequest::awaitingProvider).count();

        JButton addProp = Ui.button("+ Add property", Ui.GOLD_BTN), reqSvc = Ui.button("Request a service", Ui.PRIMARY);
        addProp.addActionListener(e -> Dialogs.propertyForm(ctl, this, null, () -> { refresh(null); ctl.navigate("properties"); }));
        reqSvc.addActionListener(e -> Dialogs.requestForm(ctl, this, null, null, () -> ctl.navigate("requests")));
        add(Ui.pageHeader("Welcome, " + me.name.split(" ")[0], "Here is how your properties are being cared for.", addProp, reqSvc), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(Ui.statCard("REGISTERED PROPERTIES", String.valueOf(ctl.data().getPropertiesByOwner(me.id).size()), Ui.GREEN_DARK));
        stats.add(Ui.statCard("ACTIVE SERVICE REQUESTS", String.valueOf(active), Ui.statusColor(Status.SCHEDULED)));
        stats.add(Ui.statCard("COMPLETED SERVICES", String.valueOf(done), Ui.OK));
        stats.add(Ui.statCard("PENDING REQUESTS", String.valueOf(pending), Ui.WARN));
        body.add(stats, BorderLayout.NORTH);
        JPanel cols = new JPanel(new GridLayout(1, 2, 16, 0));
        cols.setOpaque(false);
        cols.add(Widgets.activityCard(ctl, reqs, "Recent activity"));
        cols.add(Widgets.upcomingCard(ctl, reqs, "Upcoming and scheduled services", true));
        body.add(cols, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
