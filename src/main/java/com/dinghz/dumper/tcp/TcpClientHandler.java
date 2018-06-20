package com.dinghz.dumper.tcp;

import com.dinghz.dumper.core.Config;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TcpClientHandler
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class TcpClientHandler extends ChannelInboundHandlerAdapter {
    static final Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ChannelHandlerContext serverCtx = ctx.channel()
                .attr(AttributeKey.<ChannelHandlerContext>valueOf("serverCtx"))
                .get();

        if (Config.LOG) {
            ByteBuf byteBuf = ((ByteBuf) msg);
            byteBuf.markReaderIndex();

            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            logger.info("{} << {} : {}", serverCtx.channel().remoteAddress(), ctx.channel().remoteAddress(), Hex.encodeHexString(bytes).toUpperCase());
            byteBuf.resetReaderIndex();
        }

        serverCtx.channel().writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChannelHandlerContext serverCtx = ctx.channel()
                .attr(AttributeKey.<ChannelHandlerContext>valueOf("serverCtx"))
                .get();

        if (serverCtx != null && serverCtx.channel().isOpen()) {
            serverCtx.channel().close();

            ctx.channel().attr(AttributeKey.<ChannelHandlerContext>valueOf("serverCtx")).set(null);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);

        ctx.close();
    }
}
