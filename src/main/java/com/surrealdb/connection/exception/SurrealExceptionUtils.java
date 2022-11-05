package com.surrealdb.connection.exception;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class SurrealExceptionUtils {

    // precomputed private variables
    private static final Pattern RECORD_ALREADY_EXITS_PATTERN = Pattern.compile("Database record `(.+):(.+)` already exists");

    public static @NotNull SurrealException createExceptionFromMessage(@NotNull String message) {
        if (message.contains("There was a problem with authentication")) {
            return new SurrealAuthenticationException();
        }

        if (message.contains("Specify a namespace to use")) {
            return new SurrealNoDatabaseSelectedException();
        }

        Matcher recordAlreadyExitsMatcher = RECORD_ALREADY_EXITS_PATTERN.matcher(message);
        if (recordAlreadyExitsMatcher.matches()) {
            return new SurrealRecordAlreadyExistsException(recordAlreadyExitsMatcher.group(1), recordAlreadyExitsMatcher.group(2));
        }

        return new SurrealException(message);
    }
}
