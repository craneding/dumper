package com.dinghz.dumper.tcp;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TcpServer
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class TcpServer implements Dumper {
    static final Logger logger = LoggerFactory.getLogger(TcpServer.class);

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    static final TcpServer tcpServer = new TcpServer();

    private TcpServer() {
    }

    public static TcpServer instance() {
        return tcpServer;
    }

    @Override
    public void start() throws Exception {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024)
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new TcpServerInitializer());

            Channel ch = b.bind(Config.LOCAL_PORT).sync().channel();

            logger.info("{}", Config.LOCAL_PORT);

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
