package com.dinghz.dumper.http;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelFuture clientChannelFuture = HttpClient.instance().connect();

        ctx.channel().attr(AttributeKey.valueOf("clientChannelFuture")).set(clientChannelFuture);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ChannelFuture clientChannelFuture = ctx.channel()
                .attr(AttributeKey.<ChannelFuture>valueOf("clientChannelFuture"))
                .get();

        HttpClient.instance().send(clientChannelFuture, ctx, msg);
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
