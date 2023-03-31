package com.surrealdb.connection;

import com.surrealdb.connection.exception.SurrealAuthenticationException;
import com.surrealdb.connection.exception.SurrealException;
import com.surrealdb.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.connection.exception.SurrealRecordAlreadyExitsException;
import com.surrealdb.connection.exception.UniqueIndexViolationException;
import com.surrealdb.connection.model.RpcResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Khalid Alharisi
 */
public class ErrorToExceptionMapper {
    private static final Pattern RECORD_ALREADY_EXITS_PATTERN =
            Pattern.compile(
                    "There was a problem with the database: Database record `(.+):(.+)` already exists");
    private static final Pattern UNIQE_INDEX_VIOLATION_PATTERN =
            Pattern.compile(
                    "There was a problem with the database: Database index `(.+)` already contains \\[.+\\], with record `(.+):(.+)`");

    public static SurrealException map(RpcResponse.Error error) {
        if (error.message().contains("There was a problem with authentication")) {
            return new SurrealAuthenticationException();
        }

        if (error.message()
                .contains("There was a problem with the database: Specify a namespace to use")) {
            return new SurrealNoDatabaseSelectedException();
        }

        Matcher recordAlreadyExitsMatcher = RECORD_ALREADY_EXITS_PATTERN.matcher(error.message());
        if (recordAlreadyExitsMatcher.matches()) {
            return new SurrealRecordAlreadyExitsException(
                    recordAlreadyExitsMatcher.group(1), recordAlreadyExitsMatcher.group(2));
        }

        Matcher uniqueIndexViolationPattern =
                UNIQE_INDEX_VIOLATION_PATTERN.matcher(error.message());
        if (uniqueIndexViolationPattern.matches()) {
            return new UniqueIndexViolationException(
                    uniqueIndexViolationPattern.group(2),
                    uniqueIndexViolationPattern.group(1),
                    uniqueIndexViolationPattern.group(3));
        }

        // return the generic Exception
        return new SurrealException(error.message());
    }
}
