package com.surrealdb;

import com.surrealdb.signin.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * The {@code Surreal} class provides methods to interact with a Surreal database.
 * It includes functionality to connect to the database, sign in with different scopes,
 * set the namespace and database, execute queries, and perform CRUD operations on records.
 */
public class Surreal extends Native implements AutoCloseable {

    static {
        Loader.loadNative();
    }

    /**
     * Constructs a new Surreal object.
     */
    public Surreal() {
        super(Surreal.newInstance());
    }

    private static native long newInstance();

    private static native boolean connect(long ptr, String connect);

    private static native String signinRoot(long ptr, String username, String password);

    private static native String signinNamespace(long ptr, String username, String password, String ns);

    private static native String signinDatabase(long ptr, String username, String password, String ns, String db);

    private static native boolean useNs(long ptr, String ns);

    private static native boolean useDb(long ptr, String ns);

    private static native long query(long ptr, String sql);

    private static native long queryBind(long ptr, String sql, String[] paramsKey, long[] valuePtrs);

    private static native long createThingValue(long ptr, long thingPtr, long valuePtr);

    private static native long[] createTargetValues(long ptr, String target, long[] valuePtrs);

    private static native long[] insertTargetValues(long ptr, String target, long[] valuePtrs);

    private static native long insertRelationTargetValue(long ptr, String target, long valuePtr);

    private static native long[] insertRelationTargetValues(long ptr, String target, long[] valuePtrs);

    private static native long relate(long ptr, long from, String table, long to);

    private static native long relateContent(long ptr, long from, String table, long to, long valuePtr);

    private static native long updateThingValue(long ptr, long thingPtr, int update, long valuePtr);

    private static native long updateTargetValue(long ptr, String target, int update, long valuePtr);

    private static native long updateTargetsValue(long ptr, String[] targets, int update, long valuePtr);

    private static native long updateTargetValueSync(long ptr, String target, int update, long valuePtr);

    private static native long updateTargetsValueSync(long ptr, String[] targets, int update, long valuePtr);

    private static native long upsertThingValue(long ptr, long thingPtr, int update, long valuePtr);

    private static native long upsertTargetValue(long ptr, String target, int update, long valuePtr);

    private static native long upsertTargetsValue(long ptr, String[] targets, int update, long valuePtr);

    private static native long upsertTargetValueSync(long ptr, String target, int update, long valuePtr);

    private static native long upsertTargetsValueSync(long ptr, String[] targets, int update, long valuePtr);

    private static native long selectThing(long ptr, long thing);

    private static native long[] selectThings(long ptr, long[] things);

    private static native long selectTargetsValues(long ptr, String... targets);

    private static native long selectTargetsValuesSync(long ptr, String... targets);

    private static native boolean deleteThing(long ptr, long thing);

    private static native boolean deleteThings(long ptr, long[] things);

    private static native boolean deleteTarget(long ptr, String target);


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
    final native boolean deleteInstance(long ptr);

    /**
     * Establishes a connection to the Surreal database using the provided connection string.
     *
     * @param connect the connection string used to establish the connection
     * @return the current instance of the {@code Surreal} class
     */
    public Surreal connect(String connect) {
        connect(getPtr(), connect);
        return this;
    }

    /**
     * Attempts to sign in to the Surreal system using the provided credentials.
     * The type of signin object determines the scope of the sign-in (Root, Namespace, or Database).
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealdb/security/authentication">authentication documentation</a>.
     * <p>
     *
     * @param signin the credentials for signing in, which can be an instance of Root, Namespace, or Database
     * @return a Token representing the session token after a successful sign-in
     * @throws SurrealException if the signin type is unsupported
     */
    public Token signin(Signin signin) {
        if (signin instanceof Database) {
            final Database db = (Database) signin;
            return new Token(signinDatabase(getPtr(), db.getUsername(), db.getPassword(), db.getNamespace(), db.getDatabase()));
        } else if (signin instanceof Namespace) {
            final Namespace ns = (Namespace) signin;
            return new Token(signinNamespace(getPtr(), ns.getUsername(), ns.getPassword(), ns.getNamespace()));
        } else if (signin instanceof Root) {
            final Root r = (Root) signin;
            return new Token(signinRoot(getPtr(), r.getUsername(), r.getPassword()));
        }
        throw new SurrealException("Unsupported sign in");
    }

    /**
     * Sets the namespace for the Surreal instance.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/use">use statement documentation</a>.
     * <p>
     *
     * @param ns the namespace to use
     * @return the current instance of the {@code Surreal} class
     */
    public Surreal useNs(String ns) {
        useNs(getPtr(), ns);
        return this;
    }

    /**
     * Sets the database for the current instance of the Surreal class.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/use">use statement documentation</a>.
     * <p>
     *
     * @param db the database name to use
     * @return the current instance of the {@code Surreal} class
     */
    public Surreal useDb(String db) {
        useDb(getPtr(), db);
        return this;
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
     * @param thg     the RecordId associated with the new record
     * @param content the content of the created record
     * @return a new Value object initialized with the provided RecordId and content
     */
    public <T> Value create(RecordId thg, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = createThingValue(getPtr(), thg.getPtr(), valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Creates a record in the database with the given `RecordID` as the key and the provided content as the value.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/create">SurrealQL documentation</a>.
     * <p>
     *
     * @param type    The class type of the object to create
     * @param thg     The RecordId used with the new record
     * @param content The content of the created record
     * @return An instance of the specified type
     */
    public <T> T create(Class<T> type, RecordId thg, T content) {
        return create(thg, content).get(type);
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
     * @param thg     The RecordId of the thing to be updated.
     * @param upType  The type of update to be performed.
     * @param content The new content to set for the specified record.
     * @return A Value object representing the updated value.
     */
    public <T> Value update(RecordId thg, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = updateThingValue(getPtr(), thg.getPtr(), upType.code, valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Updates a record of the specified type and returns the updated record.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update">SurrealQL documentation</a>.
     *
     * @param type    the class type of the record to be updated
     * @param thg     the identifier of the record to be updated
     * @param upType  the type of update operation to be performed
     * @param content the new content to update the record with
     * @param <T>     the type of the record
     * @return the updated record of the specified type
     */
    public <T> T update(Class<T> type, RecordId thg, UpType upType, T content) {
        return update(thg, upType, content).get(type);
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
     * @param thg     The record identifier.
     * @param upType  The update type specifying how to handle the upsert.
     * @param content The content to be inserted or updated.
     * @return The resulting value after the upsert operation.
     */
    public <T> Value upsert(RecordId thg, UpType upType, T content) {
        final ValueMut valueMut = ValueBuilder.convert(content);
        final long valuePtr = upsertThingValue(getPtr(), thg.getPtr(), upType.code, valueMut.getPtr());
        return new Value(valuePtr);
    }

    /**
     * Upserts a record and returns the updated or inserted entity.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/upsert">SurrealQL documentation</a>.
     *
     * @param <T>     The type of the entity to be upserted.
     * @param type    The class type of the entity.
     * @param thg     The record identifier.
     * @param upType  The type of the update.
     * @param content The content of the entity to be upserted.
     * @return The upserted entity of the specified type.
     */
    public <T> T upsert(Class<T> type, RecordId thg, UpType upType, T content) {
        return upsert(thg, upType, content).get(type);
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
        final long valuePtr = selectThing(getPtr(), recordId.getPtr());
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
     * @param things an array of RecordId objects to be used in the selection.
     * @return a list of Value objects corresponding to the selected RecordIds.
     */
    public List<Value> select(RecordId... things) {
        final long[] thingsPtr = things2longs(things);
        final long[] valuePtrs = selectThings(getPtr(), thingsPtr);
        try (final LongStream s = Arrays.stream(valuePtrs)) {
            return s.mapToObj(Value::new).collect(Collectors.toList());
        }
    }

    private long[] things2longs(RecordId... things) {
        final long[] ptrs = new long[things.length];
        int index = 0;
        for (final RecordId t : things) {
            ptrs[index++] = t.getPtr();
        }
        return ptrs;
    }

    /**
     * Selects and retrieves a list of objects of the specified type based on the given record IDs.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/select">SurrealQL documentation</a>.
     *
     * @param <T>    the type of objects to be retrieved
     * @param type   the Class object of the type to be retrieved
     * @param things an array of RecordId instances identifying the records to be selected
     * @return a list of objects of the specified type corresponding to the given record IDs
     */
    public <T> List<T> select(Class<T> type, RecordId... things) {
        try (final Stream<Value> s = select(things).stream()) {
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
        deleteThing(getPtr(), recordId.getPtr());
    }

    /**
     * Deletes the specified records.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/delete">SurrealQL documentation</a>.
     *
     * @param things An array of RecordId objects representing the records to be deleted.
     */
    public void delete(RecordId... things) {
        final long[] thingsPtr = things2longs(things);
        deleteThings(getPtr(), thingsPtr);
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
