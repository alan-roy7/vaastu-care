package com.vaastucare;

import com.vaastucare.controller.AppController;
import com.vaastucare.data.DataService;
import com.vaastucare.data.MockDataService;
import com.vaastucare.data.jdbc.JdbcDataService;
import com.vaastucare.db.AppConfig;
import com.vaastucare.db.DataAccessException;
import com.vaastucare.db.Db;
import com.vaastucare.view.MainFrame;
import com.vaastucare.view.Ui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * Entry point. Uses the MySQL database by default. Start with the argument --mock to run the
 * old in-memory demo data instead (nothing is saved).
 */
public class Main {
    private static MainFrame frame;

    public static void main(String[] args) {
        boolean mock = args.length > 0 && args[0].equals("--mock");
        SwingUtilities.invokeLater(() -> {
            Ui.installLookAndFeel();
            DataService data;
            try {
                data = mock ? new MockDataService() : connect();
            } catch (DataAccessException e) {
                startupError(e.getMessage());
                return;
            }
            Thread.setDefaultUncaughtExceptionHandler((t, ex) -> showError(ex));
            AppController controller = new AppController(data);
            frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }

    private static DataService connect() {
        Db.init(AppConfig.load());
        JdbcDataService data = new JdbcDataService();
        if (data.getAllUsers().isEmpty())
            throw new DataAccessException("The database is empty. Run the database setup with sample data first (see README, step \"Create the database\").");
        return data;
    }

    private static void startupError(String message) {
        JOptionPane.showMessageDialog(null, message + "\n\nThe application will close.", "Vaastu Care - cannot start", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    /** Shows a friendly message for any error that escapes a button handler, instead of crashing. */
    private static void showError(Throwable ex) {
        ex.printStackTrace();
        String msg;
        if (ex instanceof DataAccessException || ex instanceof SecurityException || ex instanceof IllegalStateException) msg = ex.getMessage();
        else msg = "Something went wrong: " + ex;
        Component parent = frame;
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent, msg, "Vaastu Care", JOptionPane.WARNING_MESSAGE));
    }
}
