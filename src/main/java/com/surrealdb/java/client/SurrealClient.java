package com.surrealdb.java.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.java.client.model.RpcRequest;
import com.surrealdb.java.client.model.RpcResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SurrealClient extends WebSocketClient {
    private final Gson gson;
    private final Map<String, CompletableFuture<?>> callbacks;
    private final Map<String, Type> resultTypes;

    @SneakyThrows
    public SurrealClient(String host, int port){
        super(URI.create("ws://"+host+":"+port+"/rpc"));
        log.debug("Connecting to SurrealDB server {}", "ws://"+host+":"+port);
        connectBlocking();

        this.gson = new GsonBuilder().disableHtmlEscaping().create();
        this.callbacks = new HashMap<>();
        this.resultTypes = new HashMap<>();
    }

    public <T> CompletableFuture<T> rpc(Type resultType, String method, Object... params){
        RpcRequest request = new RpcRequest(method, params);

        CompletableFuture<T> callback = new CompletableFuture<>();
        callbacks.put(request.getId(), callback);
        if(resultType != null){
            resultTypes.put(request.getId(), resultType);
        }

        String json = gson.toJson(request);
        log.debug("Sending RPC request {}", json);
        send(json);

        return callback;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.debug("Connected");
    }

    @Override
    public void onMessage(String message) {
        RpcResponse response = gson.fromJson(message, RpcResponse.class);
        if(response.getError() == null){
            log.debug("received RPC response {}", message);
            CompletableFuture<Object> callback = (CompletableFuture<Object>) callbacks.get(response.getId());
            Type resultType = resultTypes.get(response.getId());
            if(callback != null){

                // parse result
                Object result;
                if(resultType != null){
                    result = gson.fromJson(response.getResult(), resultType);
                }else{
                    result = response.getResult();
                }

                // call the callback
                callback.complete(result);
            }
        }else{
            log.error("received RPC error: code={} message={}", response.getError().getCode(), response.getError().getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.debug("onClose");
    }

    @Override
    public void onError(Exception ex) {
        log.error("onError", ex);
    }

}
