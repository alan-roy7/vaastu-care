package com.vaastucare.data.jdbc;

/** Converts between database integer ids and the text ids used by the UI (U7, PR3, SR-1004, C2, R5, N9). */
final class Ids {
    private Ids() {}
    static String user(int n) { return "U" + n; }
    static String property(int n) { return "PR" + n; }
    static String request(int n) { return "SR-" + n; }
    static String category(int n) { return "C" + n; }
    static String review(int n) { return "R" + n; }
    static String notification(int n) { return "N" + n; }

    /** "SR-1004" -> 1004, "U7" -> 7. */
    static int num(String id) {
        try {
            return Integer.parseInt(id.replaceAll("\\D", ""));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
    }
}
