package com.github.mostroverkhov.firebase_rsocket_data.common.model.write;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Maksym Ostroverkhov on 03.03.17.
 */
public class WriteResponse {
    private final String[] pathChildren;
    private String writeKey;

    public WriteResponse(String writeKey, String... pathChildren) {
        this.writeKey = writeKey;
        assertArgs(writeKey, pathChildren);

        this.pathChildren = pathChildren;
    }

    public List<String> getPathChildren() {
        return Arrays.asList(pathChildren);
    }

    public String getWriteKey() {
        return writeKey;
    }

    private static void assertArgs(String writeKey, String[] pathChildren) {
        if (pathChildren == null) {
            throw new IllegalArgumentException("Path should not be null");
        }
        if (writeKey == null || writeKey.isEmpty()) {
            throw new IllegalArgumentException("Write key should not be empty");
        }
    }
}
