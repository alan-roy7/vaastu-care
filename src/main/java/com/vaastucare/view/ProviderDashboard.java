package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProviderDashboard extends JPanel implements Screen {
    private final AppController ctl;

    public ProviderDashboard(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
    }

    @Override public void refresh(Object arg) {
        removeAll();
        Provider me = ctl.data().getProvider(ctl.user().id);
        List<ServiceRequest> reqs = ctl.data().getRequestsByProvider(me.id);
        long fresh = reqs.stream().filter(ServiceRequest::awaitingProvider).count();
        long sched = reqs.stream().filter(r -> Status.SCHEDULED.equals(r.status) && r.accepted).count();
        long prog = reqs.stream().filter(r -> Status.IN_PROGRESS.equals(r.status)).count();
        long done = reqs.stream().filter(r -> Status.COMPLETED.equals(r.status)).count();

        JButton inbox = Ui.button("View incoming requests", Ui.PRIMARY);
        inbox.addActionListener(e -> ctl.navigate("incoming"));
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(Ui.pageHeader("Welcome, " + me.name, "Your rating: " + ctl.ratingText(me.id), inbox), BorderLayout.NORTH);
        if (!me.isListed()) {
            String msg = me.suspended ? "Your listing is suspended by an administrator."
                : Provider.PENDING.equals(me.approval) ? "Your profile is waiting for admin approval. You will not appear in the verified directory until approved."
                : "Your application was rejected" + (me.rejectionReason.isEmpty() ? "." : ": " + me.rejectionReason);
            Ui.RoundedPanel b = new Ui.RoundedPanel(12, Ui.GOLD_SOFT, Ui.GOLD);
            b.setLayout(new BorderLayout());
            b.setBorder(new EmptyBorder(10, 14, 10, 14));
            b.add(Ui.label(msg, 13, Font.BOLD, new Color(0x6B5520)));
            JPanel w = new JPanel(new BorderLayout());
            w.setOpaque(false);
            w.setBorder(new EmptyBorder(0, 0, 14, 0));
            w.add(b);
            north.add(w, BorderLayout.SOUTH);
        }
        add(north, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(Ui.statCard("NEW SERVICE REQUESTS", String.valueOf(fresh), Ui.WARN));
        stats.add(Ui.statCard("ACCEPTED / SCHEDULED JOBS", String.valueOf(sched), Ui.statusColor(Status.SCHEDULED)));
        stats.add(Ui.statCard("WORK IN PROGRESS", String.valueOf(prog), Ui.GOLD.darker()));
        stats.add(Ui.statCard("COMPLETED JOBS", String.valueOf(done), Ui.OK));
        body.add(stats, BorderLayout.NORTH);
        JPanel cols = new JPanel(new GridLayout(1, 2, 16, 0));
        cols.setOpaque(false);
        cols.add(Widgets.activityCard(ctl, reqs, "Recent activity"));
        cols.add(Widgets.upcomingCard(ctl, reqs, "Upcoming jobs and new requests", false));
        body.add(cols, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
