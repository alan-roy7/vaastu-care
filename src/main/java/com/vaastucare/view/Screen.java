package com.vaastucare.view;

/** Every page shown inside the Shell implements this. */
public interface Screen {
    /** Rebuild the page. arg may carry an id (request id, provider id) or be null to reuse the last one. */
    void refresh(Object arg);
}
