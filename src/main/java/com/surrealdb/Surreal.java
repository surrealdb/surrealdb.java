package com.surrealdb;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.surrealdb.signin.BearerCredential;
import com.surrealdb.signin.Credential;
import com.surrealdb.signin.DatabaseCredential;
import com.surrealdb.signin.NamespaceCredential;
import com.surrealdb.signin.RecordCredential;
import com.surrealdb.signin.RootCredential;
import com.surrealdb.signin.Token;

/**
 * The {@code Surreal} class provides methods to interact with a Surreal database.
 * It includes functionality to connect to the database, sign in with different scopes,
 * set the namespace and database, execute queries, and perform CRUD operations on records.
 */
public class Surreal extends Native implements AutoCloseable {

    static {
        Loader.loadNative();
    }

    // Current namespace and database set by useNs() / useDb() / useDefaults() (from server return value).
    private String namespace;
    private String database;

    /**
     * Constructs a new Surreal object.
     */
    public Surreal() {
        super(Surreal.newInstance());
    }

    /**
     * Constructor for a Surreal instance backed by an existing native pointer (e.g. from {@link #newSession()}).
     */
    private Surreal(long ptr) {
        super(ptr);
        this.namespace = null;
        this.database = null;
    }

    private static native long newInstance();

    private static native long cloneSession(long ptr);

    private static native boolean connect(long ptr, String connect);

    private static native Token signinRoot(long ptr, String username, String password);

    private static native Token signinNamespace(long ptr, String username, String password, String ns);

    private static native Token signinDatabase(long ptr, String username, String password, String ns, String db);

    private static native Token signup(long ptr, String namespace, String database, String access, long paramsValuePtr);

    private static native Token signinRecord(long ptr, String namespace, String database, String access, long paramsValuePtr);

    private static native boolean authenticate(long ptr, String token);

    private static native boolean invalidate(long ptr);

    private static native NsDb useNs(long ptr, String ns);

    private static native NsDb useDb(long ptr, String db);

    private static native NsDb useDefaults(long ptr);

    private static native long query(long ptr, String sql);

    private static native long queryBind(long ptr, String sql, String[] paramsKey, long[] valuePtrs);

    private static native long createRecordIdValue(long ptr, long recordIdPtr, long valuePtr);

    private static native long[] createTargetValues(long ptr, String target, long[] valuePtrs);

    private static native long[] insertTargetValues(long ptr, String target, long[] valuePtrs);

    private static native long insertRelationTargetValue(long ptr, String target, long valuePtr);

    private static native long[] insertRelationTargetValues(long ptr, String target, long[] valuePtrs);

    private static native long relate(long ptr, long from, String table, long to);

    private static native long relateContent(long ptr, long from, String table, long to, long valuePtr);

    private static native long updateRecordIdValue(long ptr, long recordIdPtr, int update, long valuePtr);

    private static native long updateTargetValue(long ptr, String target, int update, long valuePtr);

    private static native long updateTargetsValue(long ptr, String[] targets, int update, long valuePtr);

    private static native long updateTargetValueSync(long ptr, String target, int update, long valuePtr);

    private static native long updateTargetsValueSync(long ptr, String[] targets, int update, long valuePtr);

    private static native long upsertRecordIdValue(long ptr, long recordIdPtr, int update, long valuePtr);

    private static native long upsertTargetValue(long ptr, String target, int update, long valuePtr);

    private static native long upsertTargetsValue(long ptr, String[] targets, int update, long valuePtr);

    private static native long upsertTargetValueSync(long ptr, String target, int update, long valuePtr);

    private static native long upsertTargetsValueSync(long ptr, String[] targets, int update, long valuePtr);

    private static native long selectRecordId(long ptr, long recordId);

    private static native long[] selectRecordIds(long ptr, long[] recordIds);

    private static native long selectTargetsValues(long ptr, String... targets);

    private static native long selectTargetsValuesSync(long ptr, String... targets);

    private static native boolean deleteRecordId(long ptr, long recordId);

    private static native boolean deleteRecordIds(long ptr, long[] recordIds);

    private static native boolean deleteTarget(long ptr, String target);

    private static native String version(long ptr);

    private static native boolean health(long ptr);


    @Override
    final String toString(long ptr) {
        return getClass().getName() + "[ptr=" + ptr + "]";
    }

    @Override
    final int hashCode(long ptr) {
        return Objects.hashCode(ptr);
    }

    @Override
    final boolean equals(long ptr1, long ptr2) {
        return ptr1 == ptr2;
    }

    @Override
    final native void deleteInstance(long ptr);

    /**
     * Establishes a connection to the Surreal database using the provided connection string.
     *
     * @param connect the connection string used to establish the connection
     * @return the current instance of the {@code Surreal} class
     */
    public Surreal connect(String connect) {
        connect(getPtr(), connect);
        namespace = null;
        database = null;
        return this;
    }

    /**
     * Returns the server version (semantic version string, e.g. "1.0.0").
     *
     * @return the server version string
     * @throws SurrealException if the request fails
     */
    public String version() {
        return version(getPtr());
    }

    /**
     * Performs a health check against the server.
     *
     * @return true if the server is healthy
     * @throws SurrealException if the health check fails
     */
    public boolean health() {
        return health(getPtr());
    }

    /**
     * Signs in with the given credential. Supports RootCredential, NamespaceCredential, DatabaseCredential, RecordCredential, and BearerCredential.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealdb/security/authentication">authentication documentation</a>.
     *
     * @param credential the credentials (RootCredential, NamespaceCredential, DatabaseCredential, RecordCredential, or BearerCredential)
     * @return a Token representing the session after a successful sign-in
     * @throws SurrealException if the credential type is unsupported or RecordCredential ns/db cannot be resolved
     */
    public Token signin(Credential credential) {
        if (credential instanceof DatabaseCredential) {
            final DatabaseCredential db = (DatabaseCredential) credential;
            return signinDatabase(getPtr(), db.getUsername(), db.getPassword(), db.getNamespace(), db.getDatabase());
        } else if (credential instanceof NamespaceCredential) {
            final NamespaceCredential ns = (NamespaceCredential) credential;
            return signinNamespace(getPtr(), ns.getUsername(), ns.getPassword(), ns.getNamespace());
        } else if (credential instanceof RootCredential) {
            final RootCredential r = (RootCredential) credential;
            return signinRoot(getPtr(), r.getUsername(), r.getPassword());
        } else if (credential instanceof RecordCredential) {
            final RecordCredential rec = (RecordCredential) credential;
            String ns = rec.getNamespace() != null ? rec.getNamespace() : this.namespace;
            String db = rec.getDatabase() != null ? rec.getDatabase() : this.database;
            if (ns == null || db == null) {
                throw new SurrealException(
                    "RecordCredential signin requires namespace and database. Set them explicitly on RecordCredential or call useNs() and useDb() first.");
            }
            final ValueMut paramsValue = ValueBuilder.convert(rec.getParams());
            return signinRecord(getPtr(), ns, db, rec.getAccess(), paramsValue.getPtr());
        } else if (credential instanceof BearerCredential) {
            BearerCredential bearer = (BearerCredential) credential;
            authenticate(getPtr(), bearer.getToken());
            return new Token(bearer.getToken(), null);
        }
        throw new SurrealException("Unsupported credential type: " + (credential != null ? credential.getClass().getName() : "null"));
    }

    /**
     * Signs up a record user with the given record access credentials.
     * When namespace or database are null on the record, the current session values from useNs/useDb are used.
     *
     * @param record record signup credentials (namespace, database, access, params; ns/db may be null to use session)
     * @return tokens (access and optional refresh) returned by the server
     * @throws SurrealException if namespace or database cannot be resolved (call useNs/useDb first when omitting)
     */
    public Token signup(RecordCredential record) {
        String ns = record.getNamespace() != null ? record.getNamespace() : this.namespace;
        String db = record.getDatabase() != null ? record.getDatabase() : this.database;
        if (ns == null || db == null) {
            throw new SurrealException(
                "RecordCredential signup requires namespace and database. Set them explicitly on RecordCredential or call useNs() and useDb() first.");
        }
        final ValueMut paramsValue = ValueBuilder.convert(record.getParams());
        return signup(getPtr(), ns, db, record.getAccess(), paramsValue.getPtr());
    }

    /**
     * Authenticates the current connection with a JWT token (e.g. the access token from signin).
     *
     * @param token the access token string
     * @return the current instance
     */
    public Surreal authenticate(String token) {
        authenticate(getPtr(), token);
        return this;
    }

    /**
     * Invalidates the authentication for the current connection.
     *
     * @return the current instance
     */
    public Surreal invalidate() {
        invalidate(getPtr());
        return this;
    }

    /**
     * Creates a new session that shares the same connection but has its own namespace, database,
     * and authentication state. Use this for multi-session support.
     *
     * @return a new Surreal instance representing a separate session
     */
    public Surreal newSession() {
        return new Surreal(cloneSession(getPtr()));
    }

    private static native long beginTransaction(long ptr);

    /**
     * Starts a client-side transaction. Use {@link Transaction#query(String)} for operations
     * and {@link Transaction#commit()} or {@link Transaction#cancel()} to complete it.
     *
     * @return a new Transaction instance
     */
    public Transaction beginTransaction() {
        return new Transaction(beginTransaction(getPtr()));
    }

    /**
     * Sets the namespace for the Surreal instance. The current namespace and database from the server
     * are stored; use {@link #getNamespace()} and {@link #getDatabase()} to read them.
     *
     * @param ns the namespace to use
     * @return this instance for chaining
     */
    public Surreal useNs(String ns) {
        NsDb result = useNs(getPtr(), ns);
        this.namespace = result.getNamespace();
        this.database = result.getDatabase();
        return this;
    }

    /**
     * Sets the database for the current instance. The current namespace and database from the server
     * are stored; use {@link #getNamespace()} and {@link #getDatabase()} to read them.
     *
     * @param db the database name to use
     * @return this instance for chaining
     */
    public Surreal useDb(String db) {
        NsDb result = useDb(getPtr(), db);
        this.namespace = result.getNamespace();
        this.database = result.getDatabase();
        return this;
    }

    /**
     * Sets the default namespace and database. The actual defaults from the server are stored;
     * use {@link #getNamespace()} and {@link #getDatabase()} to read them.
     *
     * @return this instance for chaining
     */
    public Surreal useDefaults() {
        NsDb result = useDefaults(getPtr());
        this.namespace = result.getNamespace();
        this.database = result.getDatabase();
        return this;
    }

    /**
     * Returns the current namespace set by the last {@link #useNs(String)}, {@link #useDb(String)},
     * or {@link #useDefaults()} call (from the server response). Null if none of those have been
     * called since {@link #connect(String)} or for a new session.
     *
     * @return the current namespace, or null
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the current database set by the last {@link #useNs(String)}, {@link #useDb(String)},
     * or {@link #useDefaults()} call (from the server response). Null if none of those have been
     * called since {@link #connect(String)} or for a new session.
     *
     * @return the current database, or null
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Executes a SurrealQL query on the database.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql">SurrealQL documentation</a>.
     * <p>
     *
     * @param sql the SurrealQL query to be executed
     * @return a Response object containing the results of the query
     */
    public Response query(String sql) {
        return new Response(query(getPtr(), sql));
    }

    /**
     * Executes a parameterized SurrealQL query on the database.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql">SurrealQL documentation</a>.
     * <p>
     *
     * @param sql    the SurrealQL query to be executed
     * @param params a map containing parameter values to be bound to the SQL query
     * @return a Response object containing the results of the query
     */
    public Response queryBind(String sql, Map<String, ?> params) {
        Map<String, ValueMut> valueMutMap = params.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry ->ValueBuilder.convert(entry.getValue())
            ));
        String[] keys = valueMutMap.keySet().toArray(new String[0]);
        long[] values = new long[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = valueMutMap.get(keys[i]).getPtr();
        }
        return new Response(queryBind(getPtr(), sql,keys, values ));
    }

    /**
     * Creates a record in the database with the given `RecordID` as the key and the provided content as the value.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/create">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>     the type of the content
     * @param recordId the RecordId associated with the new record
     * @param content  the content of the created record
     * @return a new Value object initialized with the provided RecordId and content
     */
    public <T> Value create(RecordId recordId, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = createRecordIdValue(getPtr(), recordId.getPtr(), valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Creates a record in the database with the given `RecordID` as the key and the provided content as the value.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/create">SurrealQL documentation</a>.
     * <p>
     *
     * @param type      The class type of the object to create
     * @param recordId  The RecordId used with the new record
     * @param content   The content of the created record
     * @return An instance of the specified type
     */
    public <T> T create(Class<T> type, RecordId recordId, T content) {
        return create(recordId, content).get(type);
    }

    /**
     * Creates records in the database with the given table and the provided contents as the values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/create">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of the contents
     * @param target   the target for which the records are created
     * @param contents the contents of the created records
     * @return a list of Value objects created based on the target and contents
     */
    @SafeVarargs
    public final <T> List<Value> create(String target, T... contents) {
        final long[] valueMutPtrs = contents2longs(contents);
        final long[] valuePtrs = createTargetValues(getPtr(), target, valueMutPtrs);
        return Arrays.stream(valuePtrs).mapToObj(Value::new).collect(Collectors.toList());
    }

    /**
     * Creates records in the database with the given table and the provided contents as the values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/create">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of objects to be created
     * @param type     the class of the type to be created
     * @param target   the target string used in the creation process
     * @param contents the contents to be used to create the objects
     * @return a list of objects of the specified type
     */
    @SafeVarargs
    public final <T> List<T> create(Class<T> type, String target, T... contents) {
        try (final Stream<Value> s = create(target, contents).stream()) {
            return s.map(v -> v.get(type)).collect(Collectors.toList());
        }
    }

    /**
     * Insert records in the database with the given table and the provided contents as the values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of the contents
     * @param target   the target for which the records are inserted
     * @param contents the contents of the inserted records
     * @return a list of Value objects inserted based on the target and contents
     */
    @SafeVarargs
    public final <T> List<Value> insert(String target, T... contents) {
        final long[] valueMutPtrs = contents2longs(contents);
        final long[] valuePtrs = insertTargetValues(getPtr(), target, valueMutPtrs);
        return Arrays.stream(valuePtrs).mapToObj(Value::new).collect(Collectors.toList());
    }

    /**
     * Insert records in the database with the given table and the provided contents as the values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of objects to be inserted
     * @param type     the class of the type to be inserted
     * @param target   the target string used in the insertion process
     * @param contents the contents to be used to insert the objects
     * @return a list of objects of the specified type
     */
    @SafeVarargs
    public final <T> List<T> insert(Class<T> type, String target, T... contents) {
        try (final Stream<Value> s = insert(target, contents).stream()) {
            return s.map(v -> v.get(type)).collect(Collectors.toList());
        }
    }

    /**
     * Inserts a relation to the specified table using the provided content.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert#insert-relation-tables">SurrealQL documentation</a>.
     * <p>
     *
     * @param target  the table where the relation is to be inserted
     * @param content the content to insert as the relation
     * @param <T>     a type that extends InsertRelation
     * @return a Value object representing the inserted relation
     */
    public <T extends InsertRelation> Value insertRelation(String target, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = insertRelationTargetValue(getPtr(), target, valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Inserts a relation of the specified type and table with the provided content.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert#insert-relation-tables">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>     the type of the relation that extends InsertRelation
     * @param type    the class object of the type T
     * @param target  the target identifier for the relation
     * @param content the content to be inserted in the relation
     * @return the inserted relation of type T
     */
    public <T extends InsertRelation> T insertRelation(Class<T> type, String target, T content) {
        return insertRelation(target, content).get(type);
    }

    /**
     * Inserts relations into the specified table with the provided contents.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert#insert-relation-tables">SurrealQL documentation</a>.
     * <p>
     *
     * @param target   the table into which the relations will be inserted
     * @param contents the contents of the relations to be inserted
     * @param <T>      the type of the insert relation
     * @return a list of values representing the result of the insert operation
     */
    @SafeVarargs
    public final <T extends InsertRelation> List<Value> insertRelations(String target, T... contents) {
        final long[] valueMutPtrs = contents2longs(contents);
        final long[] valuePtrs = insertRelationTargetValues(getPtr(), target, valueMutPtrs);
        return Arrays.stream(valuePtrs).mapToObj(Value::new).collect(Collectors.toList());
    }

    /**
     * Inserts multiple relations of a specified type into the target.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/insert#insert-relation-tables">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>      the type of InsertRelation
     * @param type     the class type of the InsertRelation
     * @param target   the table in which the relations are to be inserted
     * @param contents the array of InsertRelation objects to be inserted
     * @return a list of inserted relations of the specified type
     */
    @SafeVarargs
    public final <T extends InsertRelation> List<T> insertRelations(Class<T> type, String target, T... contents) {
        try (final Stream<Value> s = insertRelations(target, contents).stream()) {
            return s.map(v -> v.get(type)).collect(Collectors.toList());
        }
    }

    /**
     * Establishes a relation between two records identified by `from` and `to` within a specified table.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/relate">SurrealQL documentation</a>.
     * <p>
     *
     * @param from  the record identifier from which the relation originates
     * @param table the name of the table where the relation will be established
     * @param to    the record identifier to which the relation points
     * @return a new {@code Value} instance representing the relation
     */
    public Value relate(RecordId from, String table, RecordId to) {
        final long valuePtr = relate(getPtr(), from.getPtr(), table, to.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Establishes and retrieves a relation of a specified type between two records.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/relate">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>   The type of the relation extending Relation.
     * @param type  The class type of the relation.
     * @param from  The starting record of the relation.
     * @param table The name of the table that holds the relation.
     * @param to    The ending record of the relation.
     * @return The established relation of the specified type.
     */
    public <T extends Relation> T relate(Class<T> type, RecordId from, String table, RecordId to) {
        return relate(from, table, to).get(type);
    }

    /**
     * Establishes a relationship between two records within a specified table,
     * attaching the provided content to this relationship.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/relate">SurrealQL documentation</a>.
     *
     * @param <T>     the type of the content associated with the relation
     * @param from    The record ID that the relationship starts from.
     * @param table   The table in which the relationship is being created.
     * @param to      The record ID that the relationship points to.
     * @param content The content to attach to the relationship.
     * @return A Value object representing the newly created relationship.
     */
    public <T> Value relate(RecordId from, String table, RecordId to, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = relateContent(getPtr(), from.getPtr(), table, to.getPtr(), valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Establishes a relation between two records and retrieves it based on the specified relation type.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/relate">SurrealQL documentation</a>.
     *
     * @param <R>     the type of the relation
     * @param <T>     the type of the content associated with the relation
     * @param type    the class of the relation type
     * @param from    the record identifier of the source record
     * @param table   the name of the table where the relation is to be established
     * @param to      the record identifier of the target record
     * @param content the content to be associated with the relation
     * @return the established relation of the specified type
     */
    public <R extends Relation, T> R relate(Class<R> type, RecordId from, String table, RecordId to, T content) {
        return relate(from, table, to, content).get(type);
    }

    /**
     * Updates the value of a record with the specified content and update type.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param <T>     The type of the content to be updated.
     * @param recordId The RecordId of the record to be updated.
     * @param upType   The type of update to be performed.
     * @param content  The new content to set for the specified record.
     * @return A Value object representing the updated value.
     */
    public <T> Value update(RecordId recordId, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = updateRecordIdValue(getPtr(), recordId.getPtr(), upType.code, valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Updates a record of the specified type and returns the updated record.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param type      the class type of the record to be updated
     * @param recordId  the identifier of the record to be updated
     * @param upType    the type of update operation to be performed
     * @param content   the new content to update the record with
     * @param <T>       the type of the record
     * @return the updated record of the specified type
     */
    public <T> T update(Class<T> type, RecordId recordId, UpType upType, T content) {
        return update(recordId, upType, content).get(type);
    }

    /**
     * Updates the table with the given content based on the specified update type.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param <T>     the type of the content to be used for the update
     * @param target  the table to be updated
     * @param upType  the type of update operation to be performed
     * @param content the content to update the target with
     * @return an Iterator of Value objects reflecting the updated state of the target
     */
    public <T> Iterator<Value> update(String target, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new ValueIterator(updateTargetValue(getPtr(), target, upType.code, valueMut.getPtr()));
    }

    /**
     * Updates the specified tables with the given content.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param targets an array of strings representing the table identifiers to be updated
     * @param upType  the type of update operation to be performed
     * @param content the content to update the targets with; the content can be of any type
     * @param <T>     the type of the content
     * @return an Iterator of Value objects representing the updated values
     */
    public <T> Iterator<Value> update(String[] targets, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new ValueIterator(updateTargetsValue(getPtr(), targets, upType.code, valueMut.getPtr()));
    }

    /**
     * Updates the specified table with the provided content and returns an iterator
     * for the updated values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param <T>     the type of the content and the type parameter for the iterator
     * @param type    the class type of the content
     * @param target  the table to be updated
     * @param upType  the type of update operation to be performed
     * @param content the content to update the target with
     * @return an iterator over the updated values
     */
    public <T> Iterator<T> update(Class<T> type, String target, UpType upType, T content) {
        return new ValueObjectIterator<>(type, update(target, upType, content));
    }

    /**
     * Updates the specified tables with the given content and returns an iterator
     * over the updated elements of the specified type.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param type    The class type of the elements to be updated.
     * @param targets An array of table identifiers to be updated.
     * @param upType  The type of update operation to be performed.
     * @param content The content to update the targets with.
     * @param <T>     The type of the elements being updated.
     * @return An iterator over the updated elements of the specified type.
     */
    public <T> Iterator<T> update(Class<T> type, String[] targets, UpType upType, T content) {
        return new ValueObjectIterator<>(type, update(targets, upType, content));
    }

    /**
     * Updates the specified table with the provided content.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param <T>     The type of the content to be synchronized.
     * @param target  The table identifier to be updated.
     * @param upType  The type of update to be performed, represented by an UpType object.
     * @param content The content to update the target with.
     * @return A thread-safe Iterator of Value objects that reflects the updated state.
     */
    public <T> Iterator<Value> updateSync(String target, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new SynchronizedValueIterator(updateTargetValueSync(getPtr(), target, upType.code, valueMut.getPtr()));
    }

    /**
     * Updates the tables using the provided content and update type.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param <T>     the type of the content being updated
     * @param targets an array of strings representing the tables to be updated
     * @param upType  an instance of {@code UpType} indicating the type of update to be performed
     * @param content the content to be used for the update, which will be converted to a {@code Value}
     * @return a thread-safe iterator over the updated {@code Value} objects
     */
    public <T> Iterator<Value> updateSync(String[] targets, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new SynchronizedValueIterator(updateTargetsValueSync(getPtr(), targets, upType.code, valueMut.getPtr()));
    }

    /**
     * Updates the table with the provided content.
     * The updated resource is then returned as a thread-safe iterator of the specified type.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param <T>     the type of the content being updated
     * @param type    the class type of the elements that the returned iterator will contain
     * @param target  the identifier of the table resource to be updated
     * @param upType  the type of update operation to be performed
     * @param content the data to update the target resource with
     * @return a thread-safe iterator of the specified type containing the updated resource
     */
    public <T> Iterator<T> updateSync(Class<T> type, String target, UpType upType, T content) {
        return new ValueObjectIterator<>(type, updateSync(target, upType, content));
    }

    /**
     * Updates the provided tables with the provided content and returns an iterator for the updated values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param <T>     the type of the content being updated
     * @param type    the class type of the content
     * @param targets an array of target identifiers to be updated
     * @param upType  the type of update to be performed
     * @param content the content to be used for the update
     * @return a thread-safe iterator for the updated values
     */
    public <T> Iterator<T> updateSync(Class<T> type, String[] targets, UpType upType, T content) {
        return new ValueObjectIterator<>(type, updateSync(targets, upType, content));
    }

    /**
     * Inserts a new record or updates an existing record with the given content.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param <T>     The type of the content.
     * @param recordId The record identifier.
     * @param upType    The update type specifying how to handle the upsert.
     * @param content   The content to be inserted or updated.
     * @return The resulting value after the upsert operation.
     */
    public <T> Value upsert(RecordId recordId, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = upsertRecordIdValue(getPtr(), recordId.getPtr(), upType.code, valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Upserts a record and returns the updated or inserted entity.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param <T>       The type of the entity to be upserted.
     * @param type      The class type of the entity.
     * @param recordId  The record identifier.
     * @param upType    The type of the update.
     * @param content   The content of the entity to be upserted.
     * @return The upserted entity of the specified type.
     */
    public <T> T upsert(Class<T> type, RecordId recordId, UpType upType, T content) {
        return upsert(recordId, upType, content).get(type);
    }

    /**
     * Performs an upsert operation on the specified table with the provided content.
     * The operation type is determined by the {@code UpType} enumeration.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param <T>     The type of the content to be upserted.
     * @param target  The target on which the upsert operation is to be performed.
     * @param upType  The type of upsert operation to be executed.
     * @param content The content to be upserted.
     * @return An iterator over the values resulting from the upsert operation.
     */
    public <T> Iterator<Value> upsert(String target, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new ValueIterator(upsertTargetValue(getPtr(), target, upType.code, valueMut.getPtr()));
    }

    /**
     * Inserts or updates values in the given tables.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param targets The array of tables to upsert values.
     * @param upType  The type specifying the upserting strategy to use.
     * @param content The content to be inserted or updated.
     * @param <T>     The type of the content to upsert.
     * @return An iterator over the upserted values.
     */
    public <T> Iterator<Value> upsert(String[] targets, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new ValueIterator(upsertTargetsValue(getPtr(), targets, upType.code, valueMut.getPtr()));
    }

    /**
     * Inserts or updates a record of the specified type with the given content.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param <T>     the type of the object to be upserted.
     * @param type    the Class object representing the type of the object.
     * @param target  the table identifier where the records should be upserted.
     * @param upType  the type of upsert operation to perform.
     * @param content the content of the object to be upserted.
     * @return an iterator of the type {@code T} containing the results of the upsert operation.
     */
    public <T> Iterator<T> upsert(Class<T> type, String target, UpType upType, T content) {
        return new ValueObjectIterator<>(type, upsert(target, upType, content));
    }

    /**
     * Updates or inserts the provided content based on the specified tables and update type.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param <T>     The type of the content to upsert.
     * @param type    the Class object representing the type of the object.
     * @param targets An array of target identifiers for the upsert operation.
     * @param upType  The type of the upsert operation specifying how to merge the content.
     * @param content The content to be upserted.
     * @return An iterator over the result of the upsert operation.
     */
    public <T> Iterator<T> upsert(Class<T> type, String[] targets, UpType upType, T content) {
        return new ValueObjectIterator<>(type, upsert(targets, upType, content));
    }

    /**
     * Inserts or updates the table with the provided content and
     * returns a thread-safe iterator over the resulting values.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>     the type of the content to be upserted
     * @param target  the target identifier where the content will be upserted
     * @param upType  the type of upsert operation
     * @param content the content to be upserted
     * @return a thread-safe iterator over the resulting values after the upsert operation
     */
    public <T> Iterator<Value> upsertSync(String target, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new SynchronizedValueIterator(upsertTargetValueSync(getPtr(), target, upType.code, valueMut.getPtr()));
    }

    /**
     * Performs an upsert (update or insert) operation on the specified tables.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param <T>     the type of the record to upsert
     * @param targets an array of target identifiers to perform the upsert operation on
     * @param upType  the type of upsert operation to perform
     * @param content the content to be upserted
     * @return a thread-safe Iterator of the resulting values from the upsert operation
     */
    public <T> Iterator<Value> upsertSync(String[] targets, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        return new SynchronizedValueIterator(upsertTargetsValueSync(getPtr(), targets, upType.code, valueMut.getPtr()));
    }

    /**
     * Inserts or updates a record and returns an iterator over the result.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param <T>     the type of the record to upsert
     * @param type    the class representing the type of the record
     * @param target  the target location for the upsert operation
     * @param upType  the type of the upsert operation
     * @param content the content of the record to be upserted
     * @return a thread-safe iterator over the upserted record
     */
    public <T> Iterator<T> upsertSync(Class<T> type, String target, UpType upType, T content) {
        return new ValueObjectIterator<>(type, upsertSync(target, upType, content));
    }

    /**
     * Performs an upsert operation with the specified content on the given tables.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param <T>     The type of the content being upserted and returned iterator's elements.
     * @param type    The class type of the content.
     * @param targets The array of table identifiers on which to perform the upsert operation.
     * @param upType  The type of upsert operation to be performed.
     * @param content The content to be upserted.
     * @return A thread-safe iterator over the upserted content of the specified type.
     */
    public <T> Iterator<T> upsertSync(Class<T> type, String[] targets, UpType upType, T content) {
        return new ValueObjectIterator<>(type, upsertSync(targets, upType, content));
    }

    @SafeVarargs
    private final <T> long[] contents2longs(T... contents) {
        final long[] ptrs = new long[contents.length];
        int index = 0;
        for (final T c : contents) {
            ptrs[index++] = ValueBuilder.convert(c).getPtr();
        }
        return ptrs;
    }

    /**
     * Selects a record by its RecordId and retrieves the corresponding Value.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/select">SurrealQL documentation</a>.
     *
     * @param recordId the unique identifier of the record to be selected
     * @return an Optional containing the Value if the record is found, or an empty Optional if not found
     */
    public Optional<Value> select(RecordId recordId) {
        final long valuePtr = selectRecordId(getPtr(), recordId.getPtr());
        if (valuePtr == 0) {
            return Optional.empty();
        }
        return Optional.of(new Value(valuePtr));
    }

    /**
     * Selects an instance of the specified type from a record identified by the given RecordId.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/select">SurrealQL documentation</a>.
     *
     * @param <T>      the type of the instance to be selected
     * @param type     the class type of the instance to be selected
     * @param recordId the unique identifier of the record from which to select the instance
     * @return an Optional containing the selected instance of the specified type if present,
     * otherwise an empty Optional
     */
    public <T> Optional<T> select(Class<T> type, RecordId recordId) {
        return select(recordId).map(v -> v.get(type));
    }

    /**
     * Selects values based on the provided RecordIds.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/select">SurrealQL documentation</a>.
     *
     * @param recordIds an array of RecordId objects to be used in the selection.
     * @return a list of Value objects corresponding to the selected RecordIds.
     */
    public List<Value> select(RecordId... recordIds) {
        final long[] recordIdsPtr = recordIds2longs(recordIds);
        final long[] valuePtrs = selectRecordIds(getPtr(), recordIdsPtr);
        try (final LongStream s = Arrays.stream(valuePtrs)) {
            return s.mapToObj(Value::new).collect(Collectors.toList());
        }
    }

    private long[] recordIds2longs(RecordId... recordIds) {
        final long[] ptrs = new long[recordIds.length];
        int index = 0;
        for (final RecordId r : recordIds) {
            ptrs[index++] = r.getPtr();
        }
        return ptrs;
    }

    /**
     * Selects and retrieves a list of objects of the specified type based on the given record IDs.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/select">SurrealQL documentation</a>.
     *
     * @param <T>        the type of objects to be retrieved
     * @param type       the Class object of the type to be retrieved
     * @param recordIds  an array of RecordId instances identifying the records to be selected
     * @return a list of objects of the specified type corresponding to the given record IDs
     */
    public <T> List<T> select(Class<T> type, RecordId... recordIds) {
        try (final Stream<Value> s = select(recordIds).stream()) {
            return s.map(v -> v.get(type)).collect(Collectors.toList());
        }
    }

    /**
     * Selects and returns an iterator over the values corresponding to the given targets.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/select">SurrealQL documentation</a>.
     *
     * @param targets A string representing the targets to be selected.
     * @return An iterator over the values corresponding to the specified targets.
     */
    public Iterator<Value> select(String targets) {
        return new ValueIterator(selectTargetsValues(getPtr(), targets));
    }

    /**
     * Selects and returns a thread-safe iterator to traverse values associated with the given targets.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/select">SurrealQL documentation</a>.
     *
     * @param targets The specified targets for which values need to be selected.
     * @return A thread-safe iterator to traverse the values associated with the specified targets.
     */
    public Iterator<Value> selectSync(String targets) {
        return new SynchronizedValueIterator(selectTargetsValuesSync(getPtr(), targets));
    }

    /**
     * Selects and retrieves an iterator of specified type for given targets.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/select">SurrealQL documentation</a>.
     * <p>
     *
     * @param <T>     The type of objects to be selected.
     * @param type    The class type of the objects to be selected.
     * @param targets A string specifying the targets to select from.
     * @return An iterator of the specified type for the selected targets.
     */
    public <T> Iterator<T> select(Class<T> type, String targets) {
        return new ValueObjectIterator<>(type, select(targets));
    }

    /**
     * Selects and returns a thread-safe iterator over a collection of objects of the specified type
     * from the given targets.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/select">SurrealQL documentation</a>.
     *
     * @param <T>     the type of objects to be iterated over
     * @param type    the class of the type of objects to be selected
     * @param targets the targets from which to select objects
     * @return a thread-safe iterator over a collection of objects of the specified type
     */
    public <T> Iterator<T> selectSync(Class<T> type, String targets) {
        return new ValueObjectIterator<>(type, selectSync(targets));
    }

    /**
     * Deletes a record identified by the provided RecordId.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/delete">SurrealQL documentation</a>.
     *
     * @param recordId the identifier of the record to be deleted
     */
    public void delete(RecordId recordId) {
        deleteRecordId(getPtr(), recordId.getPtr());
    }

    /**
     * Deletes the specified records.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/delete">SurrealQL documentation</a>.
     *
     * @param recordIds An array of RecordId objects representing the records to be deleted.
     */
    public void delete(RecordId... recordIds) {
        final long[] recordIdsPtr = recordIds2longs(recordIds);
        deleteRecordIds(getPtr(), recordIdsPtr);
    }

    /**
     * Deletes the specified target.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/delete">SurrealQL documentation</a>.
     *
     * @param target the name of the target to be deleted
     */
    public void delete(String target) {
        deleteTarget(getPtr(), target);
    }

    /**
     * Closes and releases any resources associated with this instance.
     * This method is typically called when the instance is no longer needed.
     * The underlying resources are safely cleaned up.
     */
    @Override
    public void close() {
        deleteInstance();
    }
}
