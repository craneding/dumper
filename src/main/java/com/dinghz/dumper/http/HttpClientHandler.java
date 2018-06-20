package com.dinghz.dumper.http;

import com.dinghz.dumper.core.Config;
import com.dinghz.dumper.http.xml.Http;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * HttpClientHandler
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class HttpClientHandler extends ChannelInboundHandlerAdapter {
    static final Logger logger = LoggerFactory.getLogger(HttpClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ChannelHandlerContext serverCtx = ctx.channel()
                .attr(AttributeKey.<ChannelHandlerContext>valueOf("serverCtx"))
                .get();
        Http httpReq = ctx.channel().attr(AttributeKey.<Http>valueOf("httpReq")).get();
        ByteArrayOutputStream os = ctx.channel().attr(AttributeKey.<ByteArrayOutputStream>valueOf("os")).get();

        Object obj = null;
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;

            obj = new DefaultHttpResponse(httpResponse.protocolVersion(), httpResponse.status(),
                    httpResponse.headers().copy());

            ctx.channel().attr(AttributeKey.valueOf("httpResponse")).set(httpResponse);
        } else if (msg instanceof LastHttpContent) {
            LastHttpContent httpContent = (LastHttpContent) msg;

            if (Config.LOG) {
                os.write(HttpCodecer.toBytes(httpContent.content()));
            }

            obj = new DefaultLastHttpContent(Unpooled.wrappedBuffer(httpContent.content()));
        } else if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            if (Config.LOG) {
                os.write(HttpCodecer.toBytes(httpContent.content()));
            }

            obj = new DefaultHttpContent(Unpooled.wrappedBuffer(httpContent.content()));
        }

        if (obj != null) {
            serverCtx.writeAndFlush(obj);
        }

        if (msg instanceof LastHttpContent) {
            if (Config.LOG) {
                HttpResponse httpResponse = ctx.channel()
                        .attr(AttributeKey.<HttpResponse>valueOf("httpResponse"))
                        .get();
                Http httpResp = HttpCodecer.encode(httpResponse, httpReq, os.toByteArray());

                JAXB.marshal(httpResp, new File(System.getProperty("log.dir"), httpResp.getId() + "-resp.xml"));
            }

            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);

        ctx.close();
    }
}
