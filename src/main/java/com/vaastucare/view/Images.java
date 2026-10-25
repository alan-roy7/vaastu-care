package com.vaastucare.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates all demo pictures in code so the project needs no image files.
 * Every generated picture is stamped as a demonstration image.
 */
public final class Images {
    private Images() {}
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final Color[][] SKIES = {
        {new Color(0xBFD9E8), new Color(0xEAF3F5)}, {new Color(0xF6D9B8), new Color(0xFBF1E1)},
        {new Color(0xC9DDC8), new Color(0xEEF5E9)}, {new Color(0xD4D6EC), new Color(0xF1F1FA)}};
    private static final Color[] WALLS = {new Color(0xF3EBD8), new Color(0xE7D3B3), new Color(0xF7F4EC), new Color(0xDCE5D5)};
    private static final Color[] ROOFS = {new Color(0x8A4B38), new Color(0x2F5D45), new Color(0x6B5B4B), new Color(0x7A3B2E)};

    private static Graphics2D gfx(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    private static void stamp(Graphics2D g, int w, int h, String text, Color bg) {
        g.setFont(Ui.font(Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text) + 20;
        g.setColor(bg);
        g.fill(new RoundRectangle2D.Float(12, h - 38, tw, 26, 26, 26));
        g.setColor(Color.WHITE);
        g.drawString(text, 22, h - 20);
    }

    public static BufferedImage property(String type, int seed) {
        String key = "prop:" + type + seed;
        BufferedImage cached = CACHE.get(key);
        if (cached != null) return cached;
        int w = 600, h = 360;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = gfx(img);
        Color[] sky = SKIES[seed % SKIES.length];
        g.setPaint(new GradientPaint(0, 0, sky[0], 0, h, sky[1]));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(0x7FA37C).darker());
        g.fillRect(0, 270, w, 90);
        g.setColor(new Color(0x8FB58A));
        g.fillOval(-100, 240, 500, 140);
        g.fillOval(300, 250, 400, 120);
        Color wall = WALLS[seed % WALLS.length], roof = ROOFS[seed % ROOFS.length];
        boolean land = type.contains("Land") || type.contains("Plot");
        if (land) {
            for (int i = 0; i < 5; i++) {
                int x = 80 + i * 110, y = 120 + (i % 2) * 25;
                g.setColor(new Color(0x7A5C3E));
                g.setStroke(new BasicStroke(7));
                g.drawLine(x, 285, x + 8, y + 40);
                g.setColor(new Color(0x2F5D45));
                for (int a = -2; a <= 2; a++) g.drawLine(x + 8, y + 40, x + 8 + a * 28, y + 25 + Math.abs(a) * 12);
            }
        } else if (type.contains("Apartment")) {
            g.setColor(wall);
            g.fillRect(190, 50, 220, 225);
            g.setColor(roof);
            g.fillRect(180, 40, 240, 16);
            g.setColor(new Color(0x9BB8C9));
            for (int r = 0; r < 5; r++) for (int c = 0; c < 4; c++) g.fillRect(208 + c * 50, 72 + r * 38, 30, 24);
            g.setColor(roof.darker());
            g.fillRect(285, 235, 30, 40);
        } else {
            g.setColor(wall);
            g.fillRect(150, 150, 300, 125);
            g.setColor(roof);
            Path2D p = new Path2D.Float();
            p.moveTo(130, 155); p.lineTo(300, 70); p.lineTo(470, 155); p.closePath();
            g.fill(p);
            g.setColor(new Color(0x9BB8C9));
            g.fillRect(180, 185, 55, 45);
            g.fillRect(365, 185, 55, 45);
            g.setColor(roof.darker());
            g.fillRect(278, 205, 44, 70);
            g.setColor(new Color(0x2F5D45));
            g.fillOval(460, 130, 90, 90);
            g.setColor(new Color(0x6B4B34));
            g.fillRect(500, 210, 10, 70);
        }
        stamp(g, w, h, "DEMO IMAGE", new Color(0, 0, 0, 120));
        g.dispose();
        CACHE.put(key, img);
        return img;
    }

    /** ref is "DEMO:n" (generated sample) or "FILE:path" (picture chosen with the file dialog). */
    public static BufferedImage proof(String ref) {
        BufferedImage cached = CACHE.get(ref);
        if (cached != null) return cached;
        int w = 640, h = 400;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = gfx(img);
        if (ref.startsWith("FILE:")) {
            try {
                BufferedImage src = ImageIO.read(new File(ref.substring(5)));
                double s = Math.max(w / (double) src.getWidth(), h / (double) src.getHeight());
                int dw = (int) (src.getWidth() * s), dh = (int) (src.getHeight() * s);
                g.drawImage(src, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            } catch (Exception e) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, 0, w, h);
                g.setColor(Color.DARK_GRAY);
                g.drawString("Could not load selected file", 200, 200);
            }
            stamp(g, w, h, "UPLOADED PHOTO", new Color(0x2E7D4F));
        } else {
            int n = Integer.parseInt(ref.substring(5));
            Color[] sky = SKIES[n % SKIES.length];
            g.setPaint(new GradientPaint(0, 0, sky[0].darker(), w, h, sky[1]));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 90));
            for (int i = 0; i < 6; i++) g.fillRoundRect(40 + ((i * 97 + n * 53) % 460), 40 + ((i * 61 + n * 29) % 240), 110, 70, 16, 16);
            g.setColor(new Color(0, 0, 0, 70));
            g.fillRoundRect(w / 2 - 55, h / 2 - 50, 110, 80, 18, 18);
            g.setColor(Color.WHITE);
            g.fillOval(w / 2 - 25, h / 2 - 35, 50, 50);
            g.setColor(new Color(0x1F3D2B));
            g.fillOval(w / 2 - 15, h / 2 - 25, 30, 30);
            g.setFont(Ui.font(Font.BOLD, 16));
            g.setColor(Color.WHITE);
            String t = "Sample completion photo #" + (n + 1);
            g.drawString(t, (w - g.getFontMetrics().stringWidth(t)) / 2, h / 2 + 70);
            stamp(g, w, h, "DEMONSTRATION IMAGE - not real evidence", new Color(0xA83A32));
        }
        g.dispose();
        CACHE.put(ref, img);
        return img;
    }

    public static BufferedImage avatar(String name, int size) {
        String key = "av:" + name + size;
        BufferedImage cached = CACHE.get(key);
        if (cached != null) return cached;
        Color[] pal = {new Color(0x2F5D45), new Color(0xB8975A), new Color(0x4F7A8A), new Color(0x8A5A44), new Color(0x5B6B9A), new Color(0x6B8E4E)};
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = gfx(img);
        g.setColor(pal[Math.abs(name.hashCode()) % pal.length]);
        g.fillOval(0, 0, size - 1, size - 1);
        StringBuilder ini = new StringBuilder();
        for (String part : name.split("[ .&]+")) if (!part.isEmpty() && ini.length() < 2 && Character.isLetter(part.charAt(0))) ini.append(Character.toUpperCase(part.charAt(0)));
        g.setColor(Color.WHITE);
        g.setFont(Ui.font(Font.BOLD, size / 3));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(ini.toString(), (size - fm.stringWidth(ini.toString())) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        CACHE.put(key, img);
        return img;
    }

    public static Icon avatarIcon(String name, int size) { return new ImageIcon(avatar(name, size)); }

    /** Paints an image "cover" style with rounded corners. */
    public static class ImagePanel extends JComponent {
        private final BufferedImage img;
        private final int radius;
        public ImagePanel(BufferedImage img, int radius, int height) {
            this.img = img; this.radius = radius;
            setPreferredSize(new Dimension(100, height));
            setMinimumSize(new Dimension(50, height));
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            double s = Math.max(getWidth() / (double) img.getWidth(), getHeight() / (double) img.getHeight());
            int dw = (int) (img.getWidth() * s), dh = (int) (img.getHeight() * s);
            g.drawImage(img, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
            g.dispose();
        }
    }
}
