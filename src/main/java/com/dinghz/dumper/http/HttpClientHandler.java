package com.dinghz.dumper.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpClientHandler
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class HttpClientHandler extends SimpleChannelInboundHandler<Object> {
    static final Logger logger = LoggerFactory.getLogger(HttpClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        ChannelHandlerContext serverCtx = ctx.channel()
                .attr(AttributeKey.<ChannelHandlerContext>valueOf("serverCtx"))
                .get();

        Object obj = null;
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;

            obj = new DefaultHttpResponse(httpResponse.protocolVersion(), httpResponse.status(), httpResponse.headers());
        } else if (msg instanceof LastHttpContent) {
            LastHttpContent httpContent = (LastHttpContent) msg;

            obj = httpContent.copy();
        } else if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            obj = httpContent.copy();
        }

        if (obj != null) {
            serverCtx.write(obj);
            serverCtx.flush();
        }

        if (msg instanceof LastHttpContent) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);

        ctx.close();
    }
}
