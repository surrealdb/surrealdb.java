package com.surrealdb;

import com.surrealdb.signin.Jwt;
import com.surrealdb.signin.Root;
import com.surrealdb.signin.Signin;

import java.util.Map;

public class Surreal implements AutoCloseable {

    static {
        Loader.load_native();
    }

    // Unique internal ID used by the native library to locate the SurrealDB instance
    private final long id;

    public Surreal() {
        id = Surreal.new_instance();
    }

    private static native long new_instance();

    private static native boolean delete_instance(long id);

    private static native boolean connect(long id, String connect) throws SurrealDBException;

    private static native String signinRoot(long id, String username, String password) throws SurrealDBException;

    private static native boolean useNs(long id, String ns) throws SurrealDBException;

    private static native boolean useDb(long id, String ns) throws SurrealDBException;

    private static native long query(long id, String sql) throws SurrealDBException;

    private static native long query_bind(long id, String sql, Map<String, Object> params) throws SurrealDBException;

    public Surreal connect(String connect) throws SurrealDBException {
        connect(id, connect);
        return this;
    }

    public Jwt signin(Signin signin) throws SurrealDBException {
        if (signin instanceof Root) {
            final Root r = (Root) signin;
            return new Jwt(signinRoot(id, r.getUsername(), r.getPassword()));
        }
        throw new SurrealDBException("Unsupported signin");
    }

    public Surreal useNs(String ns) throws SurrealDBException {
        useNs(id, ns);
        return this;
    }

    public Surreal useDb(String ns) throws SurrealDBException {
        useDb(id, ns);
        return this;
    }

    public Response query(String sql) throws SurrealDBException {
        return new Response(query(id, sql));
    }

    public Response query_bind(String sql, Map<String, Object> params) throws SurrealDBException {
        return new Response(query_bind(id, sql, params));
    }

    @Override
    public void close() {
        delete_instance(id);
    }


}