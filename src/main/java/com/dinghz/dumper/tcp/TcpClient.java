package com.dinghz.dumper.tcp;

import com.dinghz.dumper.core.Config;
import com.dinghz.dumper.core.Dumper;
import com.dinghz.dumper.http.HttpClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TcpClient
 *
 * @author dinghz
 * @date 2018/6/19
 * @company 丁小样同学工作室
 * @email crane.ding@163.com
 */
public class TcpClient implements Dumper {
    static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    final EventLoopGroup workerGroup = new NioEventLoopGroup();
    final Bootstrap bootstrap = new Bootstrap();

    static final TcpClient tcpClient = new TcpClient();

    static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private TcpClient() {
    }

    public static TcpClient instance() {
        return tcpClient;
    }

    @Override
    public void start() {
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new TcpClientInitializer());
    }

    public ChannelFuture connect() throws InterruptedException {
        return bootstrap.connect(Config.REMOTE_HOST, Config.REMOTE_PORT).sync();
    }

    public void send(ChannelFuture clientChannelFuture, ChannelHandlerContext ctx, Object msg) {
        executorService.execute(() -> {
            try {
                if (Config.LOG) {
                    ByteBuf byteBuf = ((ByteBuf) msg);
                    byteBuf.markReaderIndex();

                    byte[] bytes = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(bytes);
                    logger.info("{} >> {} : {}", ctx.channel().remoteAddress(), clientChannelFuture.channel().remoteAddress(), Hex.encodeHexString(bytes).toUpperCase());
                    byteBuf.resetReaderIndex();
                }

                clientChannelFuture.channel().attr(AttributeKey.valueOf("serverCtx")).set(ctx);
                clientChannelFuture.channel().writeAndFlush(msg);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
    }

}
