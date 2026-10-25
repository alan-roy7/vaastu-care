package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Request details, progress, history and photo proof. Shows role-specific actions. */
public class TrackingScreen extends JPanel implements Screen {
    private final AppController ctl;
    private String requestId;

    public TrackingScreen(AppController ctl) {
        super(new BorderLayout(0, 10));
        this.ctl = ctl;
        setOpaque(false);
    }

    @Override public void refresh(Object arg) {
        if (arg != null) requestId = (String) arg;
        removeAll();
        ServiceRequest r = ctl.request(requestId);
        User me = ctl.user();
        User owner = ctl.data().getUser(r.ownerId);
        Provider prov = ctl.data().getProvider(r.providerId);
        Property prop = ctl.data().getProperty(r.propertyId);

        JButton back = Ui.button("\u2190 Back", Ui.GHOST);
        back.addActionListener(e -> ctl.navigate(me.role == Role.OWNER ? "requests" : me.role == Role.PROVIDER ? "jobs" : "allrequests"));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel hl = Ui.box(true);
        hl.add(back);
        JPanel t = Ui.flow(FlowLayout.LEFT, 12, 4);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.add(Ui.title("Request " + r.id));
        t.add(Ui.statusBadge(r.status));
        hl.add(t);
        hl.add(Ui.muted(r.category + " · created " + r.createdOn.format(Widgets.DATE)));
        head.add(hl, BorderLayout.WEST);
        add(head, BorderLayout.NORTH);

        // ---------------- left column
        JPanel left = Ui.box(true);
        Ui.RoundedPanel details = Ui.card();
        details.add(Ui.h2("Request details"), BorderLayout.NORTH);
        details.add(Widgets.keyValue(new String[][]{{"Category", r.category}, {"Work required", r.description},
            {"Preferred date", r.preferredDate.format(Widgets.DATE)}, {"Notes", r.notes},
            {"Provider response", r.responseLabel()}}, 290), BorderLayout.CENTER);
        stack(left, details);
        Ui.RoundedPanel parties = Ui.card();
        parties.add(Ui.h2("Owner and provider"), BorderLayout.NORTH);
        parties.add(Widgets.keyValue(new String[][]{{"Owner", owner.name}, {"Owner contact", owner.email + "\n" + owner.phone},
            {"Provider", prov.name}, {"Provider contact", prov.email + "\n" + prov.phone}}, 270), BorderLayout.CENTER);
        stack(left, parties);
        Ui.RoundedPanel pc = Ui.card();
        pc.add(Ui.h2("Property"), BorderLayout.NORTH);
        JPanel pb = Ui.box(true);
        pb.setBorder(new EmptyBorder(10, 0, 0, 0));
        Images.ImagePanel img = new Images.ImagePanel(Images.property(prop.type, prop.imageSeed), 12, 120);
        img.setAlignmentX(Component.LEFT_ALIGNMENT);
        pb.add(img);
        JPanel kv = Widgets.keyValue(new String[][]{{"Name", prop.name}, {"Address", prop.address + ", " + prop.city},
            {"Type", prop.type + " · " + prop.summary()}}, 270);
        kv.setAlignmentX(Component.LEFT_ALIGNMENT);
        pb.add(kv);
        pc.add(pb, BorderLayout.CENTER);
        stack(left, pc);

        // ---------------- right column
        JPanel right = Ui.box(true);
        Ui.RoundedPanel prog = Ui.card();
        prog.add(Ui.h2("Service progress"), BorderLayout.NORTH);
        ProgressTracker tracker = new ProgressTracker();
        tracker.setRequest(r);
        prog.add(tracker, BorderLayout.CENTER);
        JPanel actions = actions(r, me);
        if (actions != null) prog.add(actions, BorderLayout.SOUTH);
        stack(right, prog);

        Ui.RoundedPanel hist = Ui.card();
        hist.add(Ui.h2("Status update history"), BorderLayout.NORTH);
        JPanel hl2 = Ui.box(true);
        hl2.setBorder(new EmptyBorder(8, 0, 0, 0));
        for (int i = r.history.size() - 1; i >= 0; i--) {
            StatusEntry e = r.history.get(i);
            hl2.add(Widgets.row(e.status + " · " + e.note, e.by + " · " + e.time.format(Widgets.DATETIME), Ui.statusColor(e.status), null));
        }
        hist.add(hl2, BorderLayout.CENTER);
        stack(right, hist);

        Ui.RoundedPanel photos = Ui.card();
        photos.add(Ui.h2("Completion photo proof"), BorderLayout.NORTH);
        JPanel pbody = new JPanel(new BorderLayout(0, 10));
        pbody.setOpaque(false);
        pbody.setBorder(new EmptyBorder(8, 0, 0, 0));
        Ui.RoundedPanel banner = new Ui.RoundedPanel(10, Ui.GOLD_SOFT, null);
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(8, 12, 8, 12));
        banner.add(Ui.wrapped("Images stamped DEMO are generated samples, not real evidence. Photos uploaded by the provider are stored in the app\u2019s uploads folder.", 440, new Color(0x6B5520)));
        pbody.add(banner, BorderLayout.NORTH);
        if (r.photos.isEmpty()) {
            pbody.add(Ui.emptyState("No photos yet", Status.COMPLETED.equals(r.status) ? "No proof was attached." : "The provider adds photo proof before completing the job."), BorderLayout.CENTER);
        } else {
            JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
            grid.setOpaque(false);
            for (String ref : r.photos) {
                Images.ImagePanel ip = new Images.ImagePanel(Images.proof(ref), 10, 100);
                ip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                ip.setToolTipText("Click to enlarge");
                ip.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { Dialogs.photoViewer(TrackingScreen.this, ref); } });
                grid.add(ip);
            }
            for (int i = r.photos.size() % 3; i != 0 && i < 3; i++) grid.add(new JPanel() {{ setOpaque(false); }});
            pbody.add(grid, BorderLayout.CENTER);
        }
        photos.add(pbody, BorderLayout.CENTER);
        stack(right, photos);

        JPanel cols = new JPanel(new GridBagLayout());
        cols.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.NORTH;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1; g.weighty = 1; g.gridy = 0;
        g.gridx = 0; g.insets = new Insets(0, 0, 0, 9); cols.add(left, g);
        g.gridx = 1; g.insets = new Insets(0, 9, 0, 0); cols.add(right, g);
        Ui.VPanel wrap = new Ui.VPanel(new BorderLayout());
        wrap.add(cols, BorderLayout.NORTH);
        add(Ui.scroll(wrap), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void stack(JPanel col, JComponent card) {
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(card);
        col.add(Ui.gap(14));
    }

    /** Role-specific buttons; returns null when there is nothing the user can do. */
    private JPanel actions(ServiceRequest r, User me) {
        JPanel p = Ui.flow(FlowLayout.LEFT, 8, 0);
        p.setBorder(new EmptyBorder(8, 0, 0, 0));
        if (me.role == Role.PROVIDER && r.providerId.equals(me.id)) {
            if (r.awaitingProvider()) {
                JButton ok = Ui.button("Accept request", Ui.PRIMARY), no = Ui.button("Reject", Ui.DANGER_BTN);
                ok.addActionListener(e -> { if (Ui.confirm(this, "Accept request", "Accept " + r.id + "?")) { ctl.acceptRequest(r); ctl.toast("Request accepted. The owner was notified."); refresh(null); } });
                no.addActionListener(e -> { String why = Dialogs.promptReason(this, "Reject request", "Reason for rejecting " + r.id + ":"); if (why != null) { ctl.rejectRequest(r, why); ctl.toast("Request rejected."); refresh(null); } });
                p.add(ok); p.add(no);
            } else if (Status.SCHEDULED.equals(r.status)) {
                JButton go = Ui.button("Start work", Ui.PRIMARY);
                go.addActionListener(e -> { if (Ui.confirm(this, "Start work", "Mark " + r.id + " as In Progress?")) { ctl.startWork(r); ctl.toast("Status updated to In Progress."); refresh(null); } });
                p.add(go);
            } else if (Status.IN_PROGRESS.equals(r.status)) {
                JButton demo = Ui.button("Add demo photo", Ui.OUTLINE), file = Ui.button("Upload photo...", Ui.OUTLINE), done = Ui.button("Mark completed", Ui.GOLD_BTN);
                demo.addActionListener(e -> { ctl.addPhoto(r, "DEMO:" + (r.photos.size() + Integer.parseInt(r.id.substring(3)))); ctl.toast("Demo photo added."); refresh(null); });
                file.addActionListener(e -> choosePhoto(r));
                done.addActionListener(e -> {
                    if (r.photos.isEmpty()) { Ui.error(this, "Add at least one completion photo before marking the job as completed."); return; }
                    if (Ui.confirm(this, "Complete job", "Mark " + r.id + " as Completed? The owner will be asked to review.")) {
                        String err = ctl.completeRequest(r);
                        if (err != null) Ui.error(this, err); else { ctl.toast("Job completed with photo proof."); refresh(null); }
                    }
                });
                p.add(demo); p.add(file); p.add(done);
            } else return null;
            return p;
        }
        if (me.role == Role.OWNER && Status.COMPLETED.equals(r.status)) {
            if (r.reviewed) { p.add(Ui.label("You have already reviewed this service.", 13, Font.BOLD, Ui.OK)); return p; }
            JButton rate = Ui.button("Rate and review provider", Ui.GOLD_BTN);
            rate.addActionListener(e -> Dialogs.reviewForm(ctl, this, r, () -> refresh(null)));
            p.add(rate);
            return p;
        }
        return null;
    }

    private void choosePhoto(ServiceRequest r) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images (jpg, png, gif)", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            ctl.addPhotoFile(r, fc.getSelectedFile());
        } catch (java.io.IOException ex) {
            Ui.error(this, ex.getMessage());
            return;
        }
        ctl.toast("Photo uploaded and saved.");
        refresh(null);
    }
}
