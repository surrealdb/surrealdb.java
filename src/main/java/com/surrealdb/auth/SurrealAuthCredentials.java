package com.surrealdb.auth;


public sealed interface SurrealAuthCredentials permits SurrealDatabaseCredentials, SurrealNamespaceCredentials, SurrealRootCredentials, SurrealScopeCredentials {

}
