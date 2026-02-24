package com.surrealdb;

/**
 * Detail kind constants for configuration errors.
 *
 * @see ConfigurationException
 */
public final class ConfigurationDetailKind {

	public static final String LIVE_QUERY_NOT_SUPPORTED = "LiveQueryNotSupported";
	public static final String BAD_LIVE_QUERY_CONFIG = "BadLiveQueryConfig";
	public static final String BAD_GRAPHQL_CONFIG = "BadGraphqlConfig";

	private ConfigurationDetailKind() {
	}
}
