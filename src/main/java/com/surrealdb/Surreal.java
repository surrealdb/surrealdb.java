package com.surrealdb;

import com.surrealdb.signin.Jwt;
import com.surrealdb.signin.Root;
import com.surrealdb.signin.Signin;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Surreal extends Native implements AutoCloseable {

    static {
        Loader.loadNative();
    }

    public Surreal() {
        super(Surreal.newInstance());
    }

    private static native long newInstance();

    private static native boolean connect(long ptr, String connect);

    private static native String signinRoot(long ptr, String username, String password);

    private static native boolean useNs(long ptr, String ns);

    private static native boolean useDb(long ptr, String ns);

    private static native long query(long ptr, String sql);

    private static native long queryBind(long ptr, String sql, Map<String, ?> params);

    private static native long create(long ptr, String table, long valuePtr);

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
        throw new SurrealException("Unsupported signin");
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

    public <T> T create(Thing Id, T content) {
        throw new SurrealException("Not implemented yet");
    }

    public <T> Thing create(String table, T content) {
        final ValueMut value = ValueBuilder.convert(content);
        return new Thing(create(getPtr(), table, value.getPtr()));
    }

    public <T> T create(String table, T... content) {
        throw new SurrealException("Not implemented yet");
    }

    public <T> Iterator<T> update(Thing thing, Class<T> type) {
        throw new SurrealException("Not implemented yet");
    }

    public <T> Iterator<T> select(Thing thing, Class<T> type) {
        throw new SurrealException("Not implemented yet");
    }

    public <T> Iterator<T> select(Collection<Thing> things, Class<T> type) {
        throw new SurrealException("Not implemented yet");
    }

    @Override
    public void close() {
        deleteInstance();
    }
}
