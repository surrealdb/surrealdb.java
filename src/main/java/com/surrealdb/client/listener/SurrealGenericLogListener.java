package com.surrealdb.client.listener;

import org.jetbrains.annotations.NotNull;

public interface SurrealGenericLogListener {

    void onLog(@NotNull Type type, @NotNull String message);

    enum Type {

        CONNECTING_TO_SERVER(Severity.INFORMATION),
        DISCONNECTING_FROM_SERVER(Severity.INFORMATION),

        ATTEMPTING_TO_CONNECT_WHILE_ALREADY_CONNECTED(Severity.WARNING),

        REQUEST_ID_NOT_FOUND_IN_PENDING_REQUESTS(Severity.WARNING);

        @NotNull Severity severity;

        Type (@NotNull Severity severity) {
            this.severity = severity;
        }

        public @NotNull Severity getSeverity() {
            return severity;
        }
    }

    enum Severity {
        INFORMATION,
        WARNING,
    }
}
