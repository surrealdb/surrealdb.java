package com.surrealdb.refactor.driver;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.surrealdb.refactor.exception.SurrealDBUnimplementedException;
import com.surrealdb.refactor.exception.UnhandledSurrealDBNettyState;
import com.surrealdb.refactor.exception.UnknownResponseToRequest;
import com.surrealdb.refactor.types.Credentials;
import com.surrealdb.refactor.types.Param;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.Promise;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class SurrealDBWebsocketClientProtocolHandler
        extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log =
            Logger.getLogger(SurrealDBWebsocketClientProtocolHandler.class.toString());
    private static final String PROPERTY_REQUEST_ID = "id";
    private ChannelPromise handshakeFuture;
    private final ConcurrentMap<String, Promise<JsonObject>> requestMap = new ConcurrentHashMap();

    private Channel channel;

    public SurrealDBWebsocketClientProtocolHandler() {}

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        Channel ch = ctx.channel();
        System.out.println("Received message: " + msg.text());
        JsonElement parsed = JsonParser.parseString(msg.text());
        if (parsed.isJsonObject()) {
            JsonObject obj = parsed.getAsJsonObject();
            if (!obj.has(PROPERTY_REQUEST_ID)) {
                throw new UnhandledSurrealDBNettyState(
                        "All requests and responses should contain a request id but that isn't enforced by the database; if there is no request id 'id' then the response will not have one either as of this writing",
                        "Received a message presumed to be a response without a request id");
            }
            String requestID = obj.getAsJsonPrimitive(PROPERTY_REQUEST_ID).getAsString();
            Promise<JsonObject> promise = requestMap.remove(requestID);
            if (promise == null) {
                promise.setFailure(
                        new UnknownResponseToRequest(
                                requestID,
                                "Unhandled response where request ID was missing from driver's tracked requests"));
            } else {
                promise.setSuccess(obj);
            }
        } else {
            throw new SurrealDBUnimplementedException(
                    "https://github.com/surrealdb/surrealdb.java/issues/68",
                    "JSON array responses are unimplemented in the plaintext websocket protocol");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    public Future<JsonObject> query(String requestID, String query, List<Param> params) {
        String method = "query";
        checkChannelAndThrow(method);
        QueryMessage queryMessage = new QueryMessage(requestID, query, params);
        String serialised = new Gson().toJson(queryMessage);
        System.out.printf("Sending query: %s\n", serialised);
        return sendAndPromise(method, requestID, serialised);
    }

    public Future<JsonObject> signin(Credentials credentials) {
        return signin(UUID.randomUUID().toString(), credentials);
    }

    public Future<JsonObject> signin(String requestID, Credentials credentials) {
        String method = "signin";
        checkChannelAndThrow(method);
        // Construct message to be sent
        SigninMessage signinMessage =
                new SigninMessage(requestID, credentials.getUsername(), credentials.getPassword());
        // Handle request response
        return sendAndPromise(method, requestID, new Gson().toJson(signinMessage));
    }

    public Future<JsonObject> use(String namespace, String database) {
        return use(UUID.randomUUID().toString(), namespace, database);
    }

    public Future<JsonObject> use(String requestID, String namespace, String database) {
        String method = "use";
        checkChannelAndThrow(method);
        UseMessage useMessage = new UseMessage(requestID, namespace, database);
        return sendAndPromise(method, requestID, new Gson().toJson(useMessage));
    }

    private void checkChannelAndThrow(String method) {
        // Check if we have an established connection
        if (channel == null || !channel.isActive()) {
            log.finest(String.format("Channel was null or inactive during %s", method));
            throw new UnhandledSurrealDBNettyState(
                    "We should have a better error for handling this state or perhaps prevent this from happening via the API",
                    String.format("%s failed because channel was either null or inactive", method));
        }
    }

    private Promise<JsonObject> sendAndPromise(
            String method, String requestID, String textFrameContent) {
        Promise<JsonObject> promise = channel.eventLoop().newPromise();
        registerRequest(requestID, promise);
        try {
            channel.writeAndFlush(new TextWebSocketFrame(textFrameContent)).sync();
        } catch (InterruptedException e) {
            throw new UnhandledSurrealDBNettyState(
                    "We should have a better way of handling these edge cases",
                    String.format("failed to write and flush synchronously during %s", method));
        }
        return promise;
    }

    private void registerRequest(String requestID, Promise<JsonObject> promise) {
        Promise<JsonObject> popped = requestMap.putIfAbsent(requestID, promise);
        if (popped != null) {
            // Reinsert whatever we removed; This is actually quite problematic, and we should do a
            // contains check before in case
            // There will always be race conditions without locks on this
            requestMap.put(requestID, popped);
            throw new UnhandledSurrealDBNettyState(
                    "this should probably be a different error as we know what is happening",
                    String.format(
                            "There was an already existing request ID '%s' so unable to perform signin",
                            requestID));
        }
    }
}
