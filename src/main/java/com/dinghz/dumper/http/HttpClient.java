package com.dinghz.dumper.http;

import com.dinghz.dumper.core.Config;
import com.dinghz.dumper.core.Dumper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HttpClient
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class HttpClient implements Dumper {
    static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    final EventLoopGroup workerGroup = new NioEventLoopGroup();
    final Bootstrap bootstrap = new Bootstrap();

    static final HttpClient httpClient = new HttpClient();

    static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private HttpClient() {
    }

    public static HttpClient instance() {
        return httpClient;
    }

    @Override
    public void start() throws Exception {
        // Configure SSL context if necessary.
        final SslContext sslCtx;
        if (Config.SSL) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new HttpClientInitializer(sslCtx));
    }

    public ChannelFuture connect() throws InterruptedException {
        return bootstrap.connect(Config.REMOTE_HOST, Config.REMOTE_PORT).sync();
    }

    public void send(ChannelFuture clientChannelFuture, ChannelHandlerContext ctx, Object msg) {
        executorService.execute(() -> {
            try {
                Object obj = null;
                if (msg instanceof HttpRequest) {
                    HttpRequest httpRequest = (HttpRequest) msg;

                    obj = new DefaultHttpRequest(httpRequest.protocolVersion(), httpRequest.method(), httpRequest.uri(), httpRequest.headers().copy());
                } else if (msg instanceof LastHttpContent) {
                    LastHttpContent httpContent = (LastHttpContent) msg;

                    obj = httpContent.copy();
                } else if (msg instanceof HttpContent) {
                    HttpContent httpContent = (HttpContent) msg;

                    obj = httpContent.copy();
                }

                if (obj == null) {
                    return;
                }

                clientChannelFuture.channel().attr(AttributeKey.valueOf("serverCtx")).set(ctx);
                clientChannelFuture.channel().write(msg);
                clientChannelFuture.channel().flush();

                if (msg instanceof LastHttpContent) {
                    clientChannelFuture.channel().closeFuture().sync();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        });
    }

    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
    }

}
