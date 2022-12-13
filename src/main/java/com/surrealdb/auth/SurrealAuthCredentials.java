package com.surrealdb.auth;


/**
 * An interface that represents the credentials used to authenticate with the SurrealDB server.
 */
public sealed interface SurrealAuthCredentials permits SurrealDatabaseCredentials, SurrealNamespaceCredentials, SurrealRootCredentials, SurrealScopeCredentials {

}
