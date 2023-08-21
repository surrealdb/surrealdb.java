package com.surrealdb.refactor.driver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.UUID;

public class SurrealDBWebsocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    private final URI address;

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
        handshaker.handshake(ctx.channel());
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
            try {
                String signinMessage = new Gson().toJson(new SigninMessage(UUID.randomUUID().toString(), "root", "root"));
                ctx.channel().writeAndFlush(new TextWebSocketFrame(signinMessage).content()).sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
}
