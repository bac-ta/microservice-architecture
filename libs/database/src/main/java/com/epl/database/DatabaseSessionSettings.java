package com.epl.database;

import java.util.Map;

/**
 * PostgreSQL settings applied with {@code set_config(name, value, true)} at the start of every
 * transaction, e.g. {@code app.tenant_id} for Row-Level Security (database/README.md).
 * Local to the transaction, so a pooled connection never carries them into the next request.
 */
@FunctionalInterface
public interface DatabaseSessionSettings {

    /** Settings for the transaction starting now; empty values are sent as '' (= NULL for app.ctx). */
    Map<String, String> current();
}
