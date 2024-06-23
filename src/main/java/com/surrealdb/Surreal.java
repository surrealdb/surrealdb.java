package com.surrealdb;

import com.surrealdb.signin.Jwt;
import com.surrealdb.signin.Root;
import com.surrealdb.signin.Signin;

import java.util.Map;

public class Surreal extends Native implements AutoCloseable {

    static {
        Loader.loadNative();
    }

    public Surreal() {
        super(Surreal.newInstance());
    }

    private static native long newInstance();

    private static native boolean connect(long id, String connect);

    private static native String signinRoot(long id, String username, String password);

    private static native boolean useNs(long id, String ns);

    private static native boolean useDb(long id, String ns);

    private static native long query(long id, String sql);

    private static native long queryBind(long id, String sql, Map<String, ?> params);

    final protected native boolean deleteInstance(long ptr);

    public Surreal connect(String connect) {
        connect(getPtr(), connect);
        return this;
    }

    public Jwt signin(Signin signin) {
        if (signin instanceof Root) {
            final Root r = (Root) signin;
            return new Jwt(signinRoot(getPtr(), r.getUsername(), r.getPassword()));
        }
        throw new SurrealDBException("Unsupported signin");
    }

    public Surreal useNs(String ns) {
        useNs(getPtr(), ns);
        return this;
    }

    public Surreal useDb(String ns) {
        useDb(getPtr(), ns);
        return this;
    }

    public Response query(String sql) {
        return new Response(query(getPtr(), sql));
    }

    public Response queryBind(String sql, Map<String, ?> params) {
        return new Response(queryBind(getPtr(), sql, params));
    }

    @Override
    public void close() {
        deleteInstance();
    }
}