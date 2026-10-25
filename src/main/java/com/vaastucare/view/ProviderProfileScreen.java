package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/** Provider profile page seen by owners: details, availability, ratings and reviews. */
public class ProviderProfileScreen extends JPanel implements Screen {
    private final AppController ctl;
    private String providerId;

    public ProviderProfileScreen(AppController ctl) {
        super(new BorderLayout(0, 12));
        this.ctl = ctl;
        setOpaque(false);
    }

    @Override public void refresh(Object arg) {
        if (arg != null) providerId = (String) arg;
        removeAll();
        Provider p = ctl.data().getProvider(providerId);
        JButton back = Ui.button("\u2190 Back to providers", Ui.GHOST);
        back.addActionListener(e -> ctl.navigate("providers"));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(back, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);

        Ui.RoundedPanel left = Ui.card();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(330, 100));
        JLabel av = new JLabel(Images.avatarIcon(p.name, 96));
        av.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(av);
        left.add(Ui.gap(12));
        JLabel nm = Ui.label(p.name, 22, Font.BOLD, Ui.GREEN_DARK);
        nm.setFont(Ui.serif(Font.BOLD, 22));
        left.add(nm);
        left.add(Ui.gap(6));
        JPanel badges = Ui.flow(FlowLayout.LEFT, 6, 0);
        badges.setAlignmentX(Component.LEFT_ALIGNMENT);
        badges.add(Ui.badge(p.category, Ui.GREEN, Ui.GREEN_SOFT));
        badges.add(Widgets.verifiedBadge());
        left.add(badges);
        left.add(Ui.gap(10));
        left.add(Ui.label(ctl.ratingText(p.id), 14, Font.BOLD, Ui.GOLD.darker()));
        left.add(Ui.gap(10));
        JPanel kv = Widgets.keyValue(new String[][]{{"Service area", p.area}, {"Experience", p.experienceYears + " years"},
            {"Availability", p.availability}, {"Based in", p.city}}, 170);
        kv.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(kv);
        left.add(Ui.gap(14));
        JButton req = Ui.button("Request this service", Ui.PRIMARY);
        req.setAlignmentX(Component.LEFT_ALIGNMENT);
        req.addActionListener(e -> Dialogs.requestForm(ctl, this, p, null, () -> ctl.navigate("requests")));
        left.add(req);
        left.add(Ui.gap(10));
        JLabel note = Ui.wrapped("Verification is simulated in this demo.", 260, Ui.MUTED);
        left.add(note);
        body.add(left, BorderLayout.WEST);

        JPanel right = Ui.box(true);
        Ui.RoundedPanel about = Ui.card();
        about.add(Ui.h2("About this provider"), BorderLayout.NORTH);
        about.add(Ui.wrapped(p.description, 520, Ui.TEXT), BorderLayout.CENTER);
        about.setBorder(new EmptyBorder(16, 20, 16, 20));
        about.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(about);
        right.add(Ui.gap(14));

        Ui.RoundedPanel rev = Ui.card();
        rev.setAlignmentX(Component.LEFT_ALIGNMENT);
        rev.add(Ui.h2("Ratings and reviews"), BorderLayout.NORTH);
        JPanel list = Ui.box(true);
        list.setBorder(new EmptyBorder(10, 0, 0, 0));
        List<Review> rs = ctl.data().getReviewsByProvider(p.id);
        if (rs.isEmpty()) list.add(Ui.emptyState("No reviews yet", "Owners can review this provider after a completed service."));
        for (Review r : rs) {
            JPanel row = new JPanel(new BorderLayout(0, 4));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(8, 0, 10, 0));
            JPanel h = Ui.flow(FlowLayout.LEFT, 10, 0);
            h.add(Ui.label(Widgets.shortName(ctl.data().getUser(r.ownerId).name), 13, Font.BOLD, Ui.TEXT));
            h.add(new StarRating(r.rating, false, 16));
            h.add(Ui.muted(r.date.format(Widgets.DATE)));
            row.add(h, BorderLayout.NORTH);
            row.add(Ui.wrapped(r.text, 500, Ui.TEXT), BorderLayout.CENTER);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(row);
            list.add(new JSeparator());
        }
        rev.add(list, BorderLayout.CENTER);
        right.add(rev);
        JPanel wrap = new Ui.VPanel(new BorderLayout());
        wrap.add(right, BorderLayout.NORTH);
        body.add(Ui.scroll(wrap), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
