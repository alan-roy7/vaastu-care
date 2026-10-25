package com.vaastucare.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;

/** Glass pane that draws toast messages and a loading spinner above everything else. */
class GlassLayer extends JComponent {
    private String toast, loadingText;
    private boolean toastError, loading;
    private double angle;
    private final Timer spin = new Timer(40, e -> { angle += 18; repaint(); });
    private final Timer hide = new Timer(2800, e -> { toast = null; repaint(); });

    GlassLayer() {
        hide.setRepeats(false);
        addMouseListener(new MouseAdapter() { });   // swallow clicks while loading
        setOpaque(false);
    }

    /** Only intercepts the mouse while the loading overlay is visible. */
    @Override public boolean contains(int x, int y) { return loading; }

    void showToast(String msg, boolean error) { toast = msg; toastError = error; hide.restart(); repaint(); }
    void showLoading(String msg) { loading = true; loadingText = msg; spin.start(); repaint(); }
    void hideLoading() { loading = false; spin.stop(); repaint(); }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (loading) {
            g.setColor(new Color(247, 244, 236, 190));
            g.fillRect(0, 0, getWidth(), getHeight());
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Ui.BORDER);
            g.drawOval(cx - 22, cy - 22, 44, 44);
            g.setColor(Ui.GREEN_DARK);
            g.draw(new Arc2D.Double(cx - 22, cy - 22, 44, 44, -angle, 100, Arc2D.OPEN));
            g.setFont(Ui.font(Font.BOLD, 13));
            g.setColor(Ui.GREEN_DARK);
            String t = loadingText == null ? "Loading..." : loadingText;
            g.drawString(t, cx - g.getFontMetrics().stringWidth(t) / 2, cy + 52);
        }
        if (toast != null) {
            g.setFont(Ui.font(Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(toast) + 56, h = 46;
            int x = (getWidth() - w) / 2, y = getHeight() - 90;
            g.setColor(new Color(0, 0, 0, 40));
            g.fill(new RoundRectangle2D.Float(x + 2, y + 3, w, h, 24, 24));
            g.setColor(toastError ? Ui.DANGER : Ui.GREEN_DARK);
            g.fill(new RoundRectangle2D.Float(x, y, w, h, 24, 24));
            g.setColor(Ui.GOLD);
            g.fillOval(x + 16, y + h / 2 - 5, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString(toast, x + 36, y + (h - fm.getHeight()) / 2 + fm.getAscent());
        }
        g.dispose();
    }
}
