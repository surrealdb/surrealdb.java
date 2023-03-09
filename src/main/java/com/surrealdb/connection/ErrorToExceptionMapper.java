package com.surrealdb.connection;

import com.surrealdb.connection.exception.*;
import com.surrealdb.connection.model.RpcResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Khalid Alharisi
 */
public class ErrorToExceptionMapper {
    private final static Pattern RECORD_ALREADY_EXITS_PATTERN = Pattern.compile("There was a problem with the database: Database record `(.+):(.+)` already exists");
    private final static Pattern UNIQE_INDEX_VIOLATION_PATTERN = Pattern.compile("There was a problem with the database: Database index `(.+)` already contains \\[.+\\], with record `(.+):(.+)`");

    public static SurrealException map(RpcResponse.Error error){
        if (error.getMessage().contains("There was a problem with authentication")) {
            return new SurrealAuthenticationException();
        }

        if (error.getMessage().contains("There was a problem with the database: Specify a namespace to use")) {
            return new SurrealNoDatabaseSelectedException();
        }

        Matcher recordAlreadyExitsMatcher = RECORD_ALREADY_EXITS_PATTERN.matcher(error.getMessage());
        if (recordAlreadyExitsMatcher.matches()) {
            return new SurrealRecordAlreadyExitsException(recordAlreadyExitsMatcher.group(1), recordAlreadyExitsMatcher.group(2));
        }

        Matcher uniqueIndexViolationPattern = UNIQE_INDEX_VIOLATION_PATTERN.matcher(error.getMessage());
        if (uniqueIndexViolationPattern.matches()) {
            return new UniqueIndexViolationException(uniqueIndexViolationPattern.group(2), uniqueIndexViolationPattern.group(1), uniqueIndexViolationPattern.group(3));
        }

        // return the generic Exception
        return new SurrealException(error.getMessage());
    }

}
