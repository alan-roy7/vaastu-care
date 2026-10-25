package com.vaastucare.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Reusable table: search box, optional filter combo, empty state and a row-action button bar.
 * Buttons enable/disable themselves according to the selected row.
 */
public class DataTable<T> extends JPanel {
    private final List<String> cols;
    private final Function<T, Object[]> mapper;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final List<T> items = new ArrayList<>();
    private final JTextField search = Ui.field(18);
    private JComboBox<String> filter;
    private int filterCol = -1;
    private final JPanel topRight = Ui.flow(FlowLayout.RIGHT, 8, 0);
    private final JPanel top = new JPanel(new BorderLayout());
    private final JPanel actions = Ui.flow(FlowLayout.LEFT, 8, 0);
    private final List<Runnable> updaters = new ArrayList<>();
    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards);
    private final JPanel emptyHolder = new JPanel(new BorderLayout());
    private Consumer<T> onOpen;

    public DataTable(String[] columns, Function<T, Object[]> mapper) {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        this.cols = List.of(columns);
        this.mapper = mapper;
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(100, 38));
        header.setDefaultRenderer((t, v, sel, foc, r, c) -> {
            JLabel l = Ui.label(String.valueOf(v), 12, Font.BOLD, Ui.MUTED);
            l.setOpaque(true);
            l.setBackground(new Color(0xF1EDE1));
            l.setBorder(new EmptyBorder(0, 12, 0, 12));
            return l;
        });

        search.putClientProperty("JTextField.placeholderText", "Search...");
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        JPanel left = Ui.flow(FlowLayout.LEFT, 8, 0);
        left.add(Ui.label("Search", 12, Font.BOLD, Ui.MUTED));
        left.add(search);
        top.setOpaque(false);
        top.add(left, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(Ui.BORDER, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        center.setOpaque(false);
        emptyHolder.setBackground(Color.WHITE);
        emptyHolder.setBorder(new LineBorder(Ui.BORDER, 1, true));
        center.add(sp, "table");
        center.add(emptyHolder, "empty");
        setEmptyText("Nothing to show", "Try changing the search or filter.");

        table.getSelectionModel().addListSelectionListener(e -> updateActions());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onOpen != null && selected() != null) onOpen.accept(selected());
            }
        });

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    public void setColumnWidth(int col, int width) { table.getColumnModel().getColumn(col).setPreferredWidth(width); }
    public void setOnOpen(Consumer<T> c) { onOpen = c; }
    public void addTopComponent(JComponent c) { topRight.add(c); }

    public void setFilter(String label, String[] options, int column) {
        filterCol = column;
        filter = Ui.combo(options);
        filter.addActionListener(e -> applyFilter());
        JPanel p = Ui.flow(FlowLayout.LEFT, 6, 0);
        p.add(Ui.label(label, 12, Font.BOLD, Ui.MUTED));
        p.add(filter);
        topRight.add(p, 0);
    }
    public void setFilterSelection(String s) { if (filter != null) filter.setSelectedItem(s); }

    public void setEmptyText(String headline, String hint) {
        emptyHolder.removeAll();
        JPanel p = Ui.emptyState(headline, hint);
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(p);
        emptyHolder.add(wrap, BorderLayout.CENTER);
        emptyHolder.revalidate();
    }

    public void addAction(String label, int kind, Predicate<T> enabled, Consumer<T> run) {
        JButton b = Ui.button(label, kind);
        b.addActionListener(e -> { T t = selected(); if (t != null) run.accept(t); });
        actions.add(b);
        updaters.add(() -> { T t = selected(); b.setEnabled(t != null && (enabled == null || enabled.test(t))); });
        updateActions();
    }

    public T selected() {
        int r = table.getSelectedRow();
        return r < 0 ? null : items.get(table.convertRowIndexToModel(r));
    }

    public void setItems(List<T> list) {
        T keep = selected();
        items.clear();
        items.addAll(list);
        model.setRowCount(0);
        for (T t : items) model.addRow(mapper.apply(t));
        applyFilter();
        if (keep != null) {
            int idx = items.indexOf(keep);
            if (idx >= 0) {
                int v = table.convertRowIndexToView(idx);
                if (v >= 0) table.setRowSelectionInterval(v, v);
            }
        }
        updateActions();
    }

    private void applyFilter() {
        final String q = search.getText().trim().toLowerCase();
        final String f = filter == null ? "All" : String.valueOf(filter.getSelectedItem());
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                if (filterCol >= 0 && !f.startsWith("All") && !f.equals(e.getStringValue(filterCol))) return false;
                if (q.isEmpty()) return true;
                for (int i = 0; i < e.getValueCount(); i++) if (e.getStringValue(i).toLowerCase().contains(q)) return true;
                return false;
            }
        });
        cards.show(center, table.getRowCount() == 0 ? "empty" : "table");
        updateActions();
    }

    private void updateActions() { for (Runnable r : updaters) r.run(); }

    private class CellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
            l.setBorder(new EmptyBorder(0, 12, 0, 12));
            l.setOpaque(true);
            l.setFont(Ui.font(Font.PLAIN, 13));
            l.setForeground(Ui.TEXT);
            l.setBackground(sel ? Ui.GREEN_SOFT : r % 2 == 0 ? Color.WHITE : new Color(0xFBF9F4));
            String col = cols.get(c);
            if (col.equals("Status") || col.equals("Response")) {
                l.setForeground(Ui.statusColor(String.valueOf(v)));
                l.setFont(Ui.font(Font.BOLD, 13));
            }
            return l;
        }
    }
}
