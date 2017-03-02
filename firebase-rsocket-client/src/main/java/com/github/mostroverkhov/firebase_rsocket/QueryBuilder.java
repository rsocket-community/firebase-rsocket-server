package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket_data.common.model.Path;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Query;

import static com.github.mostroverkhov.firebase_rsocket_data.common.model.Query.OrderBy.*;

/**
 * Created by Maksym Ostroverkhov on 28.02.17.
 */
public class QueryBuilder {

    private String operation = "";
    private int windowSize = 25;
    private Query.OrderDir orderDir = Query.OrderDir.ASC;
    private Query.OrderBy orderBy = KEY;
    private String key;
    private final Path path;

    public QueryBuilder(String... childPaths) {
        this.path = new Path(childPaths);

    }

    public QueryBuilder windowWithSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size should be positive");
        }
        this.windowSize = size;
        return this;
    }

    public QueryBuilder op(String operation) {
        this.operation = operation;
        return this;
    }

    public QueryBuilder orderByChild(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key should not be null");
        }
        this.key = key;
        orderBy = CHILD;
        return this;
    }

    public QueryBuilder orderByKey() {
        orderBy = KEY;
        this.key = null;
        return this;
    }

    public QueryBuilder orderByValue() {
        orderBy = VALUE;
        this.key = null;
        return this;
    }

    public QueryBuilder asc() {
        orderDir = Query.OrderDir.ASC;
        return this;
    }

    public QueryBuilder desc() {
        orderDir = Query.OrderDir.DESC;
        return this;
    }

    public Query build() {

        return new Query(operation,
                path,
                windowSize,
                orderDir,
                orderBy,
                key, null);
    }

    static void assertNotEmpty(String dataPath) {
        if (dataPath == null || dataPath.isEmpty()) {
            throw new IllegalArgumentException("Data path should not be empty");
        }
    }

}