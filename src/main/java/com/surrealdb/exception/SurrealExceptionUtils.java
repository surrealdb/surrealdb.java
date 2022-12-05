package com.surrealdb.exception;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for SurrealDB exception messages.
 */
@UtilityClass
public class SurrealExceptionUtils {

    // precomputed private variables
    private static final @NotNull Pattern AUTH_PROBLEM_PATTERN = Pattern.compile("(There was a problem with authentication|You don't have permission to perform this query type)");
    private static final @NotNull Pattern NO_DATABASE_SELECTED_PATTERN = Pattern.compile("Specify a (namespace|database) to use");
    private static final @NotNull Pattern RECORD_ALREADY_EXITS_PATTERN = Pattern.compile("Database record `(.+):(.+)` already exists");

    /**
     * Parses an exception message and returns a {@link SurrealException} instance.
     *
     * @param message The exception message to parse
     * @return The parsed exception
     */
    public static @NotNull SurrealException createExceptionFromMessage(@NotNull String message) {
        if (AUTH_PROBLEM_PATTERN.matcher(message).matches()) {
            return new SurrealAuthenticationException(message);
        }

        if (NO_DATABASE_SELECTED_PATTERN.matcher(message).matches()) {
            return new SurrealNoDatabaseSelectedException(message);
        }

        Matcher recordAlreadyExitsMatcher = RECORD_ALREADY_EXITS_PATTERN.matcher(message);
        if (recordAlreadyExitsMatcher.matches()) {
            return new SurrealRecordAlreadyExistsException(recordAlreadyExitsMatcher.group(1), recordAlreadyExitsMatcher.group(2));
        }

        // If we don't know what the error is, just return a generic exception
        return new SurrealException(message);
    }

    public static @NotNull RuntimeException wrapException(@NotNull String message, @NotNull Exception exception) {
        System.out.println(exception.getClass());

        if (exception instanceof SurrealException surrealException) {
            return surrealException;
        }

        return new SurrealException(message, exception);
    }
}
