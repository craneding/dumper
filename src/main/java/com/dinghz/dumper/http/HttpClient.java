package com.dinghz.dumper.http;

import com.dinghz.dumper.core.Config;
import com.dinghz.dumper.core.Dumper;
import com.dinghz.dumper.http.xml.Http;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
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

    public void send(ChannelHandlerContext ctx, HttpRequest httpRequest, ByteBuf contentBuf) {
        executorService.execute(() -> {
            ChannelFuture clientChannelFuture = null;
            try {
                Http httpReq = null;

                if (Config.LOG) {
                    httpReq = HttpCodecer.encode(httpRequest, contentBuf);

                    JAXB.marshal(httpReq, new File(System.getProperty("log.dir"), httpReq.getId() + "-req.xml"));
                }

                clientChannelFuture = HttpClient.instance().connect();

                ctx.channel().attr(AttributeKey.valueOf("clientChannelFuture")).set(clientChannelFuture);
                DefaultFullHttpRequest request = new DefaultFullHttpRequest(httpRequest.protocolVersion(), httpRequest.method(),
                        httpRequest.uri(), contentBuf);

                // 构建http请求
                HttpHeaders headers = httpRequest.headers();
                for (Map.Entry<String, String> entry : headers.entries()) {
                    request.headers().set(entry.getKey(), entry.getValue());
                }

                clientChannelFuture.channel().attr(AttributeKey.valueOf("serverCtx")).set(ctx);
                if (Config.LOG) {
                    clientChannelFuture.channel().attr(AttributeKey.valueOf("httpReq")).set(httpReq);
                    clientChannelFuture.channel().attr(AttributeKey.valueOf("os")).set(new ByteArrayOutputStream());
                }
                clientChannelFuture.channel().writeAndFlush(request);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);

                if (clientChannelFuture != null) {
                    clientChannelFuture.channel().close();
                }
            }
        });
    }

    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
    }

}
