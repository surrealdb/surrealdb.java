package com.surrealdb.refactor.driver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.refactor.driver.parsing.JsonQueryResultParser;
import com.surrealdb.refactor.exception.SurrealDBUnimplementedException;
import com.surrealdb.refactor.exception.UnhandledProtocolResponse;
import com.surrealdb.refactor.types.Credentials;
import com.surrealdb.refactor.types.Param;
import com.surrealdb.refactor.types.QueryBlockResult;
import com.surrealdb.refactor.types.QueryResult;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WsPlaintextConnection {

    private static final String HANDLER_ID_SURREALDB_CLIENT = "srdb-client";

    private static final EventLoopGroup group = new NioEventLoopGroup();
    private static final int MAX_CONTENT_LENGTH = 65536;

    public WsPlaintextConnection() {}

    public static UnauthenticatedSurrealDB<BidirectionalSurrealDB> connect(final URI uri) {
        final Channel channel;
        System.out.printf("Connecting to %s\n", uri);
        try {
            channel = bootstrapProtocol(uri).connect(uri.getHost(), uri.getPort()).sync().channel();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new UnauthenticatedSurrealDB<BidirectionalSurrealDB>() {
            @Override
            public UnusedSurrealDB<BidirectionalSurrealDB> authenticate(
                    final Credentials credentials) {
                final SurrealDBWebsocketClientProtocolHandler srdbHandler =
                        (SurrealDBWebsocketClientProtocolHandler)
                                channel.pipeline().get(HANDLER_ID_SURREALDB_CLIENT);
                final Object result;
                try {
                    result = srdbHandler.signin(credentials).get(2, TimeUnit.SECONDS);
                } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("Successfully signed in: %s\n", result);
                final BidirectionalSurrealDB surrealdb =
                        new BidirectionalSurrealDB() {

                            @Override
                            public QueryBlockResult query(
                                    final String query, final List<Param> params) {
                                JsonObject resp = null;
                                try {
                                    resp =
                                            srdbHandler
                                                    .query(
                                                            UUID.randomUUID().toString(),
                                                            query,
                                                            params)
                                                    .get(2, TimeUnit.SECONDS);
                                } catch (final InterruptedException
                                        | ExecutionException
                                        | TimeoutException e) {
                                    throw new RuntimeException(e);
                                }
                                // Process the query list
                                if (!resp.has("result")) {
                                    throw new UnhandledProtocolResponse(
                                            "Expected the response to contain a result");
                                }
                                final JsonElement outerResultJson = resp.get("result");
                                final QueryResult[] processedOuterResults;
                                if (outerResultJson.isJsonArray()) {
                                    final JsonArray outerResultArray =
                                            outerResultJson.getAsJsonArray();
                                    processedOuterResults =
                                            new QueryResult[outerResultArray.size()];
                                    for (int i = 0; i < outerResultArray.size(); i++) {
                                        final JsonElement innerResultJson = outerResultArray.get(i);
                                        if (!innerResultJson.isJsonObject()) {
                                            throw new UnhandledProtocolResponse(
                                                    "Expected the result to be an object");
                                        }
                                        final QueryResult val =
                                                new JsonQueryResultParser().parse(innerResultJson);
                                        processedOuterResults[i] = val;
                                    }
                                } else {
                                    throw new SurrealDBUnimplementedException(
                                            "https://github.com/surrealdb/surrealdb.java/issues/75",
                                            "The response contained results that were not in an array");
                                }
                                return new QueryBlockResult(Arrays.asList(processedOuterResults));
                            }
                        };
                return new UnusedSurrealDB<>() {
                    @Override
                    public BidirectionalSurrealDB use() {
                        try {
                            final Object use = srdbHandler.use(null, null).get(2, TimeUnit.SECONDS);
                            return surrealdb;
                        } catch (final InterruptedException
                                | ExecutionException
                                | TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public BidirectionalSurrealDB use(final String namespace) {
                        try {
                            final Object use =
                                    srdbHandler.use(namespace, null).get(2, TimeUnit.SECONDS);
                            return surrealdb;
                        } catch (final InterruptedException
                                | ExecutionException
                                | TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public BidirectionalSurrealDB use(
                            final String namespace, final String database) {
                        try {
                            final Object use =
                                    srdbHandler.use(namespace, database).get(2, TimeUnit.SECONDS);
                            return surrealdb;
                        } catch (final InterruptedException
                                | ExecutionException
                                | TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }

    private static Bootstrap bootstrapProtocol(final URI uri) {
        return new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(
                        new ChannelInitializer<>() {
                            @Override
                            protected void initChannel(final Channel ch) throws Exception {
                                final ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new HttpClientCodec())
                                        .addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH))
                                        .addLast(
                                                new WebSocketClientProtocolHandler(
                                                        WebSocketClientHandshakerFactory
                                                                .newHandshaker(
                                                                        uri,
                                                                        WebSocketVersion.V13,
                                                                        null,
                                                                        false,
                                                                        null)))
                                        .addLast(
                                                HANDLER_ID_SURREALDB_CLIENT,
                                                new SurrealDBWebsocketClientProtocolHandler());
                            }
                        });
    }
}
