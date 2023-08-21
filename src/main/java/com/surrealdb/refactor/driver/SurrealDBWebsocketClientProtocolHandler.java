package com.surrealdb.refactor.driver;

import com.google.gson.Gson;
import com.surrealdb.refactor.exception.UnhandledSurrealDBNettyState;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.logging.Logger;

public class SurrealDBWebsocketClientProtocolHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log =Logger.getLogger(SurrealDBWebsocketClientProtocolHandler.class.toString());
    private ChannelPromise handshakeFuture;

    private Channel channel;

    public SurrealDBWebsocketClientProtocolHandler() {
    }

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
            this.channel.writeAndFlush(new TextWebSocketFrame(new Gson().toJson(signinMessage))).sync();
            System.out.println("signin flushed");
        } catch (InterruptedException e) {
            throw new UnhandledSurrealDBNettyState("We should have a better way of handling these edge cases", "failed to write and flush synchronously during signin");
        }
    }
}
