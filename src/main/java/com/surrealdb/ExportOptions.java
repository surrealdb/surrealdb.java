package com.surrealdb;

import java.util.Arrays;

/**
 * Options controlling what a SQL export contains. Used with
 * {@link Surreal#exportSql(String, ExportOptions)} and
 * {@link Surreal#exportSql(ExportOptions)}.
 *
 * <p>
 * Each option toggles a section of the export. The defaults mirror the
 * SurrealDB defaults: everything is exported except historical record versions
 * ({@link #versions(boolean)} defaults to {@code false}).
 *
 * <p>
 * The builder-style setters return {@code this} so calls can be chained:
 *
 * <pre>
 * new ExportOptions().records(false).tables("person", "city");
 * </pre>
 */
public class ExportOptions {

	// Flag bits passed to the native layer. Keep in sync with the FLAG_*
	// constants in src/main/rust/surreal.rs.
	static final int FLAG_USERS = 1;
	static final int FLAG_ACCESSES = 1 << 1;
	static final int FLAG_PARAMS = 1 << 2;
	static final int FLAG_FUNCTIONS = 1 << 3;
	static final int FLAG_ANALYZERS = 1 << 4;
	static final int FLAG_APIS = 1 << 5;
	static final int FLAG_BUCKETS = 1 << 6;
	static final int FLAG_MODULES = 1 << 7;
	static final int FLAG_CONFIGS = 1 << 8;
	static final int FLAG_VERSIONS = 1 << 9;
	static final int FLAG_RECORDS = 1 << 10;
	static final int FLAG_TABLES = 1 << 11;

	private boolean users = true;
	private boolean accesses = true;
	private boolean params = true;
	private boolean functions = true;
	private boolean analyzers = true;
	private boolean apis = true;
	private boolean buckets = true;
	private boolean modules = true;
	private boolean configs = true;
	private boolean versions = false;
	private boolean records = true;
	private boolean allTables = true;
	private String[] tables = null;

	/**
	 * Constructs export options with the default settings.
	 */
	public ExportOptions() {
	}

	/**
	 * Whether to export users. Defaults to {@code true}.
	 *
	 * @param users
	 *            true to include user definitions
	 * @return this instance for chaining
	 */
	public ExportOptions users(boolean users) {
		this.users = users;
		return this;
	}

	/**
	 * Whether to export accesses. Defaults to {@code true}.
	 *
	 * @param accesses
	 *            true to include access definitions
	 * @return this instance for chaining
	 */
	public ExportOptions accesses(boolean accesses) {
		this.accesses = accesses;
		return this;
	}

	/**
	 * Whether to export params. Defaults to {@code true}.
	 *
	 * @param params
	 *            true to include param definitions
	 * @return this instance for chaining
	 */
	public ExportOptions params(boolean params) {
		this.params = params;
		return this;
	}

	/**
	 * Whether to export functions. Defaults to {@code true}.
	 *
	 * @param functions
	 *            true to include function definitions
	 * @return this instance for chaining
	 */
	public ExportOptions functions(boolean functions) {
		this.functions = functions;
		return this;
	}

	/**
	 * Whether to export analyzers. Defaults to {@code true}.
	 *
	 * @param analyzers
	 *            true to include analyzer definitions
	 * @return this instance for chaining
	 */
	public ExportOptions analyzers(boolean analyzers) {
		this.analyzers = analyzers;
		return this;
	}

	/**
	 * Whether to export apis. Defaults to {@code true}.
	 *
	 * @param apis
	 *            true to include api definitions
	 * @return this instance for chaining
	 */
	public ExportOptions apis(boolean apis) {
		this.apis = apis;
		return this;
	}

	/**
	 * Whether to export buckets. Defaults to {@code true}.
	 *
	 * @param buckets
	 *            true to include bucket definitions
	 * @return this instance for chaining
	 */
	public ExportOptions buckets(boolean buckets) {
		this.buckets = buckets;
		return this;
	}

	/**
	 * Whether to export modules. Defaults to {@code true}.
	 *
	 * @param modules
	 *            true to include module definitions
	 * @return this instance for chaining
	 */
	public ExportOptions modules(boolean modules) {
		this.modules = modules;
		return this;
	}

	/**
	 * Whether to export configs. Defaults to {@code true}.
	 *
	 * @param configs
	 *            true to include config definitions
	 * @return this instance for chaining
	 */
	public ExportOptions configs(boolean configs) {
		this.configs = configs;
		return this;
	}

	/**
	 * Whether to export all historical versions of data. Defaults to {@code false}.
	 *
	 * @param versions
	 *            true to include historical record versions
	 * @return this instance for chaining
	 */
	public ExportOptions versions(boolean versions) {
		this.versions = versions;
		return this;
	}

	/**
	 * Whether to export records (table data). Table definitions are still exported
	 * when tables are included. Defaults to {@code true}.
	 *
	 * @param records
	 *            true to include table records
	 * @return this instance for chaining
	 */
	public ExportOptions records(boolean records) {
		this.records = records;
		return this;
	}

	/**
	 * Whether to export all tables or none at all. Defaults to {@code true} (all
	 * tables). Replaces any table list previously set via
	 * {@link #tables(String...)}.
	 *
	 * @param tables
	 *            true to export all tables, false to export none
	 * @return this instance for chaining
	 */
	public ExportOptions tables(boolean tables) {
		this.allTables = tables;
		this.tables = null;
		return this;
	}

	/**
	 * Restricts the export to the given tables. Calling this with no arguments is
	 * equivalent to {@code tables(true)}: all tables are exported. Replaces any
	 * setting previously made via {@link #tables(boolean)} or this method.
	 *
	 * @param tables
	 *            the names of the tables to export; empty means all tables
	 * @return this instance for chaining
	 */
	public ExportOptions tables(String... tables) {
		if (tables.length == 0) {
			return tables(true);
		}
		// Clear the all-tables flag so FLAG_TABLES is never set alongside an
		// explicit list (the native layer gives the list precedence anyway).
		this.allTables = false;
		this.tables = Arrays.copyOf(tables, tables.length);
		return this;
	}

	/**
	 * Encodes the boolean options as flag bits for the native layer.
	 *
	 * @return the flags
	 */
	int flags() {
		int flags = 0;
		if (users)
			flags |= FLAG_USERS;
		if (accesses)
			flags |= FLAG_ACCESSES;
		if (params)
			flags |= FLAG_PARAMS;
		if (functions)
			flags |= FLAG_FUNCTIONS;
		if (analyzers)
			flags |= FLAG_ANALYZERS;
		if (apis)
			flags |= FLAG_APIS;
		if (buckets)
			flags |= FLAG_BUCKETS;
		if (modules)
			flags |= FLAG_MODULES;
		if (configs)
			flags |= FLAG_CONFIGS;
		if (versions)
			flags |= FLAG_VERSIONS;
		if (records)
			flags |= FLAG_RECORDS;
		if (allTables)
			flags |= FLAG_TABLES;
		return flags;
	}

	/**
	 * The explicit table list, or null when {@link #tables(boolean)} applies.
	 *
	 * @return the table names to export, or null
	 */
	String[] tableList() {
		return tables;
	}
}
