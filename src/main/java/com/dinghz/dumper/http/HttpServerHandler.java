package com.dinghz.dumper.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpServerHandler
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class HttpServerHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        AttributeKey<ByteBuf> attributeKey = AttributeKey.valueOf("contentBuf");

        if (msg instanceof HttpRequest) {
            int contentLength = HttpUtil.getContentLength((HttpRequest) msg, 0);
            ctx.channel().attr(AttributeKey.valueOf("request")).set(msg);
            ctx.channel().attr(attributeKey).set(Unpooled.buffer(contentLength));
        } else if (msg instanceof HttpContent) {
            HttpRequest request = (HttpRequest) ctx.channel().attr(AttributeKey.valueOf("request")).get();
            HttpContent content = (HttpContent) msg;
            ByteBuf contentBuf = ctx.channel().attr(attributeKey).get();

            content.content().readBytes(contentBuf, content.content().readableBytes());

            if (msg instanceof LastHttpContent) {
                HttpClient.instance().send(ctx, request, ctx.channel().attr(attributeKey).get());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);

        ctx.close();

        ChannelFuture clientChannelFuture = ctx.channel()
                .attr(AttributeKey.<ChannelFuture>valueOf("clientChannelFuture"))
                .get();

        if (clientChannelFuture != null && clientChannelFuture.channel().isOpen()) {
            clientChannelFuture.channel().closeFuture();
        }
    }
}
