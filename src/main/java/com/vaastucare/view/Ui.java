package com.vaastucare.view;

import com.vaastucare.model.Status;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/** Theme colours, fonts and small factory methods so every screen looks the same. */
public final class Ui {
    private Ui() {}

    public static final Color GREEN_DARK = new Color(0x1F3D2B), GREEN = new Color(0x2F5D45),
        GREEN_SOFT = new Color(0xE4EEE7), GOLD = new Color(0xB8975A), GOLD_SOFT = new Color(0xF3EBD8),
        BG = new Color(0xF7F4EC), TEXT = new Color(0x1E2A22), MUTED = new Color(0x6B7A70),
        BORDER = new Color(0xE2DDD0), DANGER = new Color(0xA83A32), OK = new Color(0x2E7D4F), WARN = new Color(0xC98A1B);
    public static final int PRIMARY = 0, GOLD_BTN = 1, OUTLINE = 2, DANGER_BTN = 3, GHOST = 4;

    public static void installLookAndFeel() {
        UIManager.put("nimbusBase", new Color(0xC9D4CB));
        UIManager.put("nimbusBlueGrey", new Color(0xB5C4B9));
        UIManager.put("nimbusLightBackground", Color.WHITE);
        UIManager.put("control", BG);
        UIManager.put("nimbusSelectionBackground", GREEN);
        UIManager.put("nimbusFocus", GOLD);
        UIManager.put("text", TEXT);
        UIManager.put("defaultFont", font(Font.PLAIN, 13));
        try {
            for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; }
        } catch (Exception ignored) { /* falls back to the default look and feel */ }
    }

    public static Font font(int style, int size) { return new Font(Font.SANS_SERIF, style, size); }
    public static Font serif(int style, int size) { return new Font(Font.SERIF, style, size); }

    public static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }
    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(serif(Font.BOLD, 26));
        l.setForeground(GREEN_DARK);
        return l;
    }
    public static JLabel h2(String text) { return label(text, 16, Font.BOLD, GREEN_DARK); }
    public static JLabel muted(String text) { return label(text, 12, Font.PLAIN, MUTED); }

    /** Wraps text in HTML so long text wraps inside a label of the given pixel width. */
    public static JLabel wrapped(String text, int width, Color color) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace("\n", "<br>");
        int css = (int) Math.round(width * 0.75);   // Swing HTML treats px as points (x1.33)
        JLabel l = new JLabel("<html><div style='width:" + css + "px'>" + safe + "</div></html>");
        l.setFont(font(Font.PLAIN, 13));
        l.setForeground(color);
        return l;
    }

    public static JLabel badge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g.dispose();
                super.paintComponent(g0);
            }
        };
        l.setOpaque(false);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(font(Font.BOLD, 11));
        l.setBorder(new EmptyBorder(3, 10, 3, 10));
        return l;
    }

    public static Color statusColor(String status) {
        if (status == null) return TEXT;
        return switch (status) {
            case Status.SCHEDULED -> new Color(0x2B6CB0);
            case Status.IN_PROGRESS -> WARN;
            case Status.COMPLETED, "Accepted" -> OK;
            case Status.REJECTED -> DANGER;
            case "Approved", "Active" -> OK;
            case "Pending", "Awaiting response" -> WARN;
            case "Suspended", "Disabled", "Declined" -> DANGER;
            default -> TEXT;
        };
    }
    public static JLabel statusBadge(String status) {
        Color c = statusColor(status);
        return badge(status, c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
    }

    public static JPanel box(boolean vertical) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, vertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
        p.setOpaque(false);
        return p;
    }
    public static JPanel flow(int align, int hgap, int vgap) {
        JPanel p = new JPanel(new FlowLayout(align, hgap, vgap));
        p.setOpaque(false);
        return p;
    }
    public static Component gap(int h) { return Box.createRigidArea(new Dimension(0, h)); }
    public static Component hgap(int w) { return Box.createRigidArea(new Dimension(w, 0)); }

    public static RoundedPanel card() {
        RoundedPanel p = new RoundedPanel(16, Color.WHITE, BORDER);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(18, 20, 18, 20));
        return p;
    }

    public static JButton button(String text, int kind) { return new RoundButton(text, kind); }

    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(font(Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 10, 7, 10)));
        return f;
    }
    public static JPasswordField passwordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(font(Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 10, 7, 10)));
        return f;
    }
    public static JTextArea area(int rows, int cols) {
        JTextArea a = new JTextArea(rows, cols);
        a.setFont(font(Font.PLAIN, 13));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(7, 10, 7, 10));
        return a;
    }
    public static JScrollPane areaScroll(JTextArea a) {
        JScrollPane s = new JScrollPane(a);
        s.setBorder(new LineBorder(BORDER, 1, true));
        return s;
    }
    public static JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(font(Font.PLAIN, 13));
        return c;
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.setViewportBorder(null);
        s.getViewport().setOpaque(false);
        s.setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(18);
        return s;
    }

    /** Page header: title and subtitle on the left, optional buttons on the right. */
    public static JPanel pageHeader(String title, String subtitle, JComponent... right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));
        JPanel left = box(true);
        left.add(title(title));
        if (subtitle != null) { left.add(gap(2)); left.add(muted(subtitle)); }
        p.add(left, BorderLayout.WEST);
        JPanel r = flow(FlowLayout.RIGHT, 8, 0);
        for (JComponent c : right) r.add(c);
        p.add(r, BorderLayout.EAST);
        return p;
    }

    public static RoundedPanel statCard(String label, String value, Color accent) {
        RoundedPanel c = card();
        c.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel v = new JLabel(value);
        v.setFont(serif(Font.BOLD, 34));
        v.setForeground(accent);
        c.add(v, BorderLayout.CENTER);
        c.add(label(label, 12, Font.BOLD, MUTED), BorderLayout.SOUTH);
        return c;
    }

    public static JPanel emptyState(String headline, String hint) {
        JPanel p = box(true);
        p.setBorder(new EmptyBorder(40, 20, 40, 20));
        JLabel a = label(headline, 16, Font.BOLD, GREEN_DARK);
        JLabel b = muted(hint);
        a.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(a); p.add(gap(4)); p.add(b);
        return p;
    }

    public static boolean confirm(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
    public static void info(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Please check", JOptionPane.WARNING_MESSAGE);
    }

    /** Standard modal dialog wrapper with a content panel and a button row. */
    public static JDialog dialog(Component parent, String title, JComponent content, JButton... buttons) {
        Window w = SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(w, title, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(20, 24, 16, 24));
        root.add(content, BorderLayout.CENTER);
        JPanel bar = flow(FlowLayout.RIGHT, 8, 0);
        for (JButton b : buttons) bar.add(b);
        root.add(bar, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.pack();
        d.setLocationRelativeTo(w);
        return d;
    }

    // ------------------------------------------------------------------ custom components
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private Color fill, line;
        public RoundedPanel(int radius, Color fill, Color line) {
            this.radius = radius; this.fill = fill; this.line = line;
            setOpaque(false);
        }
        public void setFill(Color c) { fill = c; repaint(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            if (line != null) {
                g.setColor(line);
                g.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    public static class RoundButton extends JButton {
        private final int kind;
        private boolean hover;
        RoundButton(String text, int kind) {
            super(text);
            this.kind = kind;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(font(Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + 36, 38);
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color bg, fg, edge = null;
            switch (kind) {
                case PRIMARY -> { bg = hover ? GREEN : GREEN_DARK; fg = Color.WHITE; }
                case GOLD_BTN -> { bg = hover ? GOLD.darker() : GOLD; fg = Color.WHITE; }
                case DANGER_BTN -> { bg = hover ? DANGER.darker() : DANGER; fg = Color.WHITE; }
                case OUTLINE -> { bg = hover ? GREEN_SOFT : Color.WHITE; fg = GREEN_DARK; edge = GREEN_DARK; }
                default -> { bg = hover ? GREEN_SOFT : new Color(0, 0, 0, 0); fg = GREEN_DARK; }
            }
            if (!isEnabled()) { bg = new Color(0xECE8DD); fg = new Color(0xA7ADA8); edge = null; }
            RoundRectangle2D.Float r = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g.setColor(bg);
            g.fill(r);
            if (edge != null) { g.setColor(edge); g.draw(r); }
            g.setFont(getFont());
            g.setColor(fg);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** A panel that always matches the width of its scroll pane (so grids reflow). */
    public static class VPanel extends JPanel implements Scrollable {
        public VPanel(LayoutManager lm) { super(lm); setOpaque(false); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 18; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    /** Simple label-above-field form layout, one or two fields per row. */
    public static class Form extends JPanel {
        private int row;
        public Form() { super(new GridBagLayout()); setOpaque(false); }
        public void add(String label, JComponent c) { addPair(label, c, null, null); }
        public void addPair(String l1, JComponent c1, String l2, JComponent c2) {
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.anchor = GridBagConstraints.WEST;
            g.weightx = 1;
            g.gridy = row++;
            g.gridx = 0;
            g.gridwidth = c2 == null ? 2 : 1;
            g.insets = new Insets(8, 0, 3, 12);
            super.add(label(l1, 12, Font.BOLD, MUTED), g);
            if (c2 != null) { g.gridx = 1; g.insets = new Insets(8, 0, 3, 0); super.add(label(l2, 12, Font.BOLD, MUTED), g); }
            g.gridy = row++;
            g.gridx = 0;
            g.insets = new Insets(0, 0, 0, c2 == null ? 0 : 12);
            super.add(c1, g);
            if (c2 != null) { g.gridx = 1; g.insets = new Insets(0, 0, 0, 0); super.add(c2, g); }
        }
    }
}
