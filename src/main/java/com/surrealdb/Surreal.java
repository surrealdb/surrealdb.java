package com.surrealdb;

import com.surrealdb.signin.Jwt;
import com.surrealdb.signin.Root;
import com.surrealdb.signin.Signin;

import java.util.Map;

public class Surreal implements AutoCloseable {

    static {
        Loader.loadNative();
    }

    // Unique internal ID used by the native library to locate the SurrealDB instance
    private final long id;

    public Surreal() {
        id = Surreal.newInstance();
    }

    private static native long newInstance();

    private static native boolean deleteInstance(long id);

    private static native boolean connect(long id, String connect);

    private static native String signinRoot(long id, String username, String password);

    private static native boolean useNs(long id, String ns);

    private static native boolean useDb(long id, String ns);

    private static native long query(long id, String sql);

    private static native long queryBind(long id, String sql, Map<String, Object> params);

    public Surreal connect(String connect) {
        connect(id, connect);
        return this;
    }

    public Jwt signin(Signin signin) {
        if (signin instanceof Root) {
            final Root r = (Root) signin;
            return new Jwt(signinRoot(id, r.getUsername(), r.getPassword()));
        }
        throw new SurrealDBException("Unsupported signin");
    }

    public Surreal useNs(String ns) {
        useNs(id, ns);
        return this;
    }

    public Surreal useDb(String ns) {
        useDb(id, ns);
        return this;
    }

    public Response query(String sql) {
        return new Response(query(id, sql));
    }

    public Response queryBind(String sql, Map<String, Object> params) {
        return new Response(queryBind(id, sql, params));
    }

    @Override
    public void close() {
        deleteInstance(id);
    }


}