package com.vaastucare.view;

import com.vaastucare.controller.AppController;
import com.vaastucare.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProviderReviewsScreen extends JPanel implements Screen {
    private final AppController ctl;

    public ProviderReviewsScreen(AppController ctl) {
        super(new BorderLayout());
        this.ctl = ctl;
        setOpaque(false);
    }

    @Override public void refresh(Object arg) {
        removeAll();
        String id = ctl.user().id;
        add(Ui.pageHeader("Ratings and Reviews", "What property owners say about your work."), BorderLayout.NORTH);
        JPanel col = Ui.box(true);
        Ui.RoundedPanel sum = Ui.card();
        sum.setAlignmentX(Component.LEFT_ALIGNMENT);
        int n = ctl.reviewCount(id);
        JLabel big = new JLabel(n == 0 ? "-" : String.format("%.1f", ctl.avgRating(id)));
        big.setFont(Ui.serif(Font.BOLD, 44));
        big.setForeground(Ui.GREEN_DARK);
        sum.add(big, BorderLayout.WEST);
        JPanel r = Ui.box(true);
        r.setBorder(new EmptyBorder(6, 18, 0, 0));
        r.add(new StarRating((int) Math.round(ctl.avgRating(id)), false, 22));
        r.add(Ui.muted(n + " review" + (n == 1 ? "" : "s")));
        sum.add(r, BorderLayout.CENTER);
        col.add(sum);
        col.add(Ui.gap(14));
        List<Review> list = ctl.data().getReviewsByProvider(id);
        if (list.isEmpty()) {
            Ui.RoundedPanel e = Ui.card();
            e.add(Ui.emptyState("No reviews yet", "Owners can review you once a job is completed."));
            e.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(e);
        }
        for (Review rv : list) {
            Ui.RoundedPanel c = Ui.card();
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            JPanel h = Ui.flow(FlowLayout.LEFT, 10, 0);
            h.add(Ui.label(Widgets.shortName(ctl.data().getUser(rv.ownerId).name), 13, Font.BOLD, Ui.TEXT));
            h.add(new StarRating(rv.rating, false, 16));
            h.add(Ui.muted(rv.date.format(Widgets.DATE) + (rv.requestId == null ? "" : " · " + rv.requestId)));
            c.add(h, BorderLayout.NORTH);
            c.add(Ui.wrapped(rv.text, 700, Ui.TEXT), BorderLayout.CENTER);
            col.add(c);
            col.add(Ui.gap(10));
        }
        Ui.VPanel wrap = new Ui.VPanel(new BorderLayout());
        wrap.add(col, BorderLayout.NORTH);
        add(Ui.scroll(wrap), BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
