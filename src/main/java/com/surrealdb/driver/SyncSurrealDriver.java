package com.surrealdb.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.patch.Patch;
import com.surrealdb.connection.exception.SurrealException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Khalid Alharisi
 */
public class SyncSurrealDriver {

    private final AsyncSurrealDriver asyncDriver;

    public SyncSurrealDriver(SurrealConnection connection){
        this.asyncDriver = new AsyncSurrealDriver(connection);
    }

    public void ping(){
        getResultSynchronously(asyncDriver.ping());
    }

    public Map<String, String> info(){
        return getResultSynchronously(asyncDriver.info());
    }

    public void signIn(String username, String password){
        getResultSynchronously(asyncDriver.signIn(username, password));
    }

	public String signUp(String namespace, String database, String scope, String email, String password) {
		return getResultSynchronously(asyncDriver.signUp(namespace, database, scope, email, password));
	}

	public void authenticate(String token){
		getResultSynchronously(asyncDriver.authenticate(token));
	}

	public void invalidate(){
		getResultSynchronously(asyncDriver.invalidate());
	}

    public void use(String namespace, String database){
        getResultSynchronously(asyncDriver.use(namespace, database));
    }

    public void let(String key, String value){
        getResultSynchronously(asyncDriver.let(key, value));
    }

    public <T> List<QueryResult<T>> query(String query, Map<String, String> args, Class<? extends T> rowType){
        return getResultSynchronously(asyncDriver.query(query, args, rowType));
    }

    public <T> List<T> select(String thing, Class<? extends T> rowType){
        return getResultSynchronously(asyncDriver.select(thing, rowType));
    }

    public <T> T create(String thing, T data){
        return getResultSynchronously(asyncDriver.create(thing, data));
    }

    public <T> List<T> update(String thing, T data){
        return getResultSynchronously(asyncDriver.update(thing, data));
    }

    public <T, P> List<T> change(String thing, P data, Class<T> rowType){
        return getResultSynchronously(asyncDriver.change(thing, data, rowType));
    }

    public void patch(String thing, List<Patch> patches){
        getResultSynchronously(asyncDriver.patch(thing, patches));
    }

    public void delete(String thing){
        getResultSynchronously(asyncDriver.delete(thing));
    }

    private <T> T getResultSynchronously(CompletableFuture<T> completableFuture){
        try {
            return completableFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof SurrealException){
                throw (SurrealException) e.getCause();
            }else{
                throw new RuntimeException(e);
            }
        }
    }

}
