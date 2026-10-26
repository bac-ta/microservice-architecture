package com.epl.database;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("epl.database.migration")
public class DatabaseMigrationProperties {

    public enum Mode {
        /** Running service: check that every migration is applied, change nothing. */
        VALIDATE,
        /** Migrate job (profile "migrate"): apply pending migrations as the schema owner. */
        MIGRATE
    }

    private Mode mode = Mode.VALIDATE;
}
