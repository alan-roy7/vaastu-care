package com.vaastucare.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

/** Five clickable stars (editable) or a read-only star display. */
public class StarRating extends JComponent {
    private int value;
    private final boolean editable;
    private final int size;

    public StarRating(int value, boolean editable, int size) {
        this.value = value; this.editable = editable; this.size = size;
        setPreferredSize(new Dimension(size * 5 + 8, size + 4));
        if (editable) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    StarRating.this.value = Math.max(1, Math.min(5, e.getX() / StarRating.this.size + 1));
                    repaint();
                }
            });
        }
    }

    public int getValue() { return value; }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < 5; i++) {
            double cx = i * size + size / 2.0, cy = size / 2.0 + 2, ro = size * 0.46, ri = ro * 0.45;
            Path2D p = new Path2D.Double();
            for (int k = 0; k < 10; k++) {
                double r = k % 2 == 0 ? ro : ri, a = Math.PI / 5 * k - Math.PI / 2;
                if (k == 0) p.moveTo(cx + r * Math.cos(a), cy + r * Math.sin(a));
                else p.lineTo(cx + r * Math.cos(a), cy + r * Math.sin(a));
            }
            p.closePath();
            g.setColor(i < value ? Ui.GOLD : new Color(0xDDD7C7));
            g.fill(p);
        }
        g.dispose();
    }
}
