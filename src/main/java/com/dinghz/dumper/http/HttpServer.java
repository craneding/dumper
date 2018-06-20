package com.dinghz.dumper.http;

import com.dinghz.dumper.core.Config;
import com.dinghz.dumper.core.Dumper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpServer
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class HttpServer implements Dumper {
    static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    static final HttpServer httpServer = new HttpServer();

    private HttpServer() {
    }

    public static HttpServer instance() {
        return httpServer;
    }

    @Override
    public void start() throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (Config.SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024)
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new HttpServerInitializer(sslCtx));

            Channel ch = b.bind(Config.LOCAL_PORT).sync().channel();

            logger.info("{} {}", Config.SSL ? "https" : "http", Config.LOCAL_PORT);

            ch.closeFuture().sync();
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
    }
}
