package com.epl.gateway.graphql;

import graphql.ExecutionInput;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.persisted.PersistedQueryCache;
import graphql.execution.preparsed.persisted.PersistedQueryCacheMiss;
import graphql.execution.preparsed.persisted.PersistedQueryNotFound;
import graphql.execution.preparsed.persisted.PersistedQuerySupport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Parsed and validated documents of Automatic Persisted Queries, by SHA-256 of the query.
 * Any client can register a query, so the cache keeps only the {@code maxSize} most recently used.
 */
public final class BoundedPersistedQueryCache implements PersistedQueryCache {

    private final Map<Object, PreparsedDocumentEntry> documents;

    public BoundedPersistedQueryCache(int maxSize) {
        this.documents = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, PreparsedDocumentEntry> eldest) {
                return size() > maxSize;
            }
        };
    }

    @Override
    public CompletableFuture<PreparsedDocumentEntry> getPersistedQueryDocumentAsync(Object persistedQueryId, ExecutionInput executionInput,
                                                                                   PersistedQueryCacheMiss onCacheMiss) throws PersistedQueryNotFound {
        PreparsedDocumentEntry document;
        synchronized (documents) {
            document = documents.get(persistedQueryId);
        }
        if (document != null) {
            return CompletableFuture.completedFuture(document);
        }

        // hash only: the client sends the full query once it gets PersistedQueryNotFound
        var query = executionInput.getQuery();
        if (query == null || query.isBlank() || query.equals(PersistedQuerySupport.PERSISTED_QUERY_MARKER)) {
            throw new PersistedQueryNotFound(persistedQueryId);
        }
        document = onCacheMiss.apply(query);
        if (!document.hasErrors()) {
            synchronized (documents) {
                documents.put(persistedQueryId, document);
            }
        }
        return CompletableFuture.completedFuture(document);
    }
}
