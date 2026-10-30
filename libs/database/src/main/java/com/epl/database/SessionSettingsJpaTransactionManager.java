package com.epl.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * JPA transaction manager that applies {@link DatabaseSessionSettings} right after BEGIN,
 * so every statement of the transaction runs with the caller's tenant context.
 */
public class SessionSettingsJpaTransactionManager extends JpaTransactionManager {

    private final DatabaseSessionSettings settings;

    public SessionSettingsJpaTransactionManager(EntityManagerFactory entityManagerFactory, DatabaseSessionSettings settings) {
        super(entityManagerFactory);
        this.settings = settings;
    }

    @Override
    protected void doBegin(@NonNull Object transaction, @NonNull TransactionDefinition definition) {
        super.doBegin(transaction, definition);

        var values = settings.current();
        if (values.isEmpty()) {
            return;
        }
        var holder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(obtainEntityManagerFactory());
        var names = new ArrayList<>(values.keySet());
        // SELECT set_config(?1, ?2, true), set_config(?3, ?4, true) …
        var sql = IntStream.range(0, names.size())
                .mapToObj(i -> "set_config(?" + (i * 2 + 1) + ", ?" + (i * 2 + 2) + ", true)")
                .collect(Collectors.joining(", ", "SELECT ", ""));
        var query = holder.getEntityManager().createNativeQuery(sql);
        for (int i = 0; i < names.size(); i++) {
            var value = values.get(names.get(i));
            query.setParameter(i * 2 + 1, names.get(i));
            query.setParameter(i * 2 + 2, value == null ? "" : value);
        }
        query.getResultList();
    }
}
