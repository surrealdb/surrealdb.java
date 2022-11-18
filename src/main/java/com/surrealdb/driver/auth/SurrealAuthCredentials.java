package com.surrealdb.driver.auth;


public sealed interface SurrealAuthCredentials permits SurrealDatabaseCredentials, SurrealNamespaceCredentials, SurrealRootCredentials, SurrealScopeCredentials {

}
