package com.surrealdb.refactor.driver;

import com.surrealdb.refactor.exception.SurrealDBUnimplementedException;
import com.surrealdb.refactor.types.Credentials;
import com.surrealdb.refactor.types.Param;
import com.surrealdb.refactor.types.Value;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;
import java.util.List;

public class WsPlaintextConnection {

    private static final EventLoopGroup group = new NioEventLoopGroup();
    private static final int MAX_CONTENT_LENGTH = 8192;

    public static UnauthenticatedSurrealDB<BidirectionalSurrealDB> connect(URI uri) {
        try {
            System.out.printf("Connecting to %s\n", uri);
            Channel conn = bootstrapProtocol(uri).connect(uri.getHost(), uri.getPort()).sync().channel();
            conn.writeAndFlush("This is the message sent").sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new UnauthenticatedSurrealDB<BidirectionalSurrealDB>() {
            @Override
            public BidirectionalSurrealDB authenticate(Credentials credentials) {
                return new BidirectionalSurrealDB() {

                    @Override
                    public List<Value> query(String query, List<Param> params) {
                        throw SurrealDBUnimplementedException.withTicket("https://github.com/surrealdb/surrealdb.java/issues/62").withMessage("Plaintext websocket connections are not supported yet");
                    }
                };
            }
        };
    }

    private static Bootstrap bootstrapProtocol(URI uri) {
        return new Bootstrap().group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new HttpClientCodec()).addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH))
                        .addLast(new WebSocketClientProtocolHandler(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, null)))
                        .addLast(new SurrealDBWebsocketClientHandler(uri));
                }
            });
    }
}
