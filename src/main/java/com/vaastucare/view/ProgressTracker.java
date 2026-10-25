package com.vaastucare.view;

import com.vaastucare.model.ServiceRequest;
import com.vaastucare.model.Status;

import javax.swing.*;
import java.awt.*;

/** Horizontal progress indicator: Requested -> Accepted -> In Progress -> Completed (or Rejected). */
public class ProgressTracker extends JComponent {
    private int step;               // number of finished steps (0..4)
    private boolean rejected;

    public ProgressTracker() { setPreferredSize(new Dimension(300, 78)); }

    public void setRequest(ServiceRequest r) {
        rejected = Status.REJECTED.equals(r.status);
        step = switch (r.status) {
            case Status.COMPLETED -> 4;
            case Status.IN_PROGRESS -> 3;
            default -> r.accepted ? 2 : 1;
        };
        repaint();
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        String[] labels = {"Requested", "Accepted", "In Progress", rejected ? "Rejected" : "Completed"};
        int n = labels.length, margin = 60, w = getWidth() - margin * 2, y = 22;
        for (int i = 0; i < n; i++) {
            int x = margin + (int) (w * i / (double) (n - 1));
            boolean done = rejected ? i == 0 || i == n - 1 : i < step;
            if (i < n - 1) {
                int x2 = margin + (int) (w * (i + 1) / (double) (n - 1));
                g.setStroke(new BasicStroke(4));
                g.setColor(!rejected && i + 1 < step ? Ui.GREEN : new Color(0xDDD7C7));
                g.drawLine(x, y, x2, y);
            }
            Color c = rejected && i == n - 1 ? Ui.DANGER : done ? Ui.GREEN : Color.WHITE;
            g.setColor(c);
            g.fillOval(x - 13, y - 13, 26, 26);
            g.setStroke(new BasicStroke(2));
            g.setColor(done ? c : new Color(0xC9C2AE));
            g.drawOval(x - 13, y - 13, 26, 26);
            g.setColor(done ? Color.WHITE : Ui.MUTED);
            g.setFont(Ui.font(Font.BOLD, 12));
            String s = rejected && i == n - 1 ? "x" : done ? "\u2713" : String.valueOf(i + 1);
            g.drawString(s, x - g.getFontMetrics().stringWidth(s) / 2, y + 4);
            boolean current = !rejected && i == step - 1;
            g.setFont(Ui.font(current ? Font.BOLD : Font.PLAIN, 12));
            g.setColor(current ? Ui.GREEN_DARK : Ui.MUTED);
            g.drawString(labels[i], x - g.getFontMetrics().stringWidth(labels[i]) / 2, y + 36);
        }
        g.dispose();
    }
}
