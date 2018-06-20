package com.dinghz.dumper.tcp;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TcpServerHandler
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class TcpServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TcpServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelFuture clientChannelFuture = TcpClient.instance().connect();
        clientChannelFuture.channel().attr(AttributeKey.valueOf("serverCtx")).set(ctx);

        ctx.channel().attr(AttributeKey.valueOf("clientChannelFuture")).set(clientChannelFuture);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ChannelFuture clientChannelFuture = ctx.channel()
                .attr(AttributeKey.<ChannelFuture>valueOf("clientChannelFuture"))
                .get();

        TcpClient.instance().send(clientChannelFuture, ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelFuture clientChannelFuture = ctx.channel()
                .attr(AttributeKey.<ChannelFuture>valueOf("clientChannelFuture"))
                .get();

        if (clientChannelFuture != null && clientChannelFuture.channel().isOpen()) {
            clientChannelFuture.channel().close();

            ctx.channel().attr(AttributeKey.<ChannelFuture>valueOf("clientChannelFuture")).set(null);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);

        ctx.close();
    }

}
