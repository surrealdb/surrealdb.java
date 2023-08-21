package com.surrealdb.refactor.driver;

import com.google.gson.Gson;
import com.surrealdb.refactor.exception.UnhandledSurrealDBNettyState;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.logging.Logger;

public class SurrealDBWebsocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger log =Logger.getLogger(SurrealDBWebsocketClientHandler.class.toString());
    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    private final URI address;
    private Channel channel;

    public SurrealDBWebsocketClientHandler(URI address) {
        this.address = address;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshaker = WebSocketClientHandshakerFactory.newHandshaker(
            address, WebSocketVersion.V13, null, false, null);
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
        handshaker.handshake(this.channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            System.out.println("WebSocket Client connected!");
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse (status=" + response.status() +
                ", content=" + response.content().toString(CharsetUtil.UTF_8) + ")");
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            System.out.println("Received message: " + textFrame.text());
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("Received Pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
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

    public void signin(SigninMessage signinMessage) {
        System.out.println("Signin started");
        if (this.channel==null || !channel.isActive()) {
            log.finest("Channel was null or inactive during signin");
            throw new UnhandledSurrealDBNettyState("We should have a better error for handling this state or perhaps prevent this from happening via the API", "signin failed because channel was either null or inactive");
        }
        try {
            log.finest("Signing in");
            this.channel.writeAndFlush(new TextWebSocketFrame(new Gson().toJson(signinMessage)).content()).sync();
            System.out.println("signin flushed");
        } catch (InterruptedException e) {
            throw new UnhandledSurrealDBNettyState("We should have a better way of handling these edge cases", "failed to write and flush synchronously during signin");
        }
    }
}
