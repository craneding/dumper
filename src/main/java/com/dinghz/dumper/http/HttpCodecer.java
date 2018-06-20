package com.dinghz.dumper.http;

import com.dinghz.dumper.http.xml.Body;
import com.dinghz.dumper.http.xml.Header;
import com.dinghz.dumper.http.xml.Headers;
import com.dinghz.dumper.http.xml.Http;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HttpCodecer
 *
 * @author dinghz
 * @date 2018/6/17
 * @company 广州易站通计算机科技有限公司
 * @email dinghz@gzyitop.com
 */
public class HttpCodecer {
    private static final AtomicLong index = new AtomicLong(0);

    private static String getNewIndex() {
        String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return yyyyMMddHHmmss + "-" + index.incrementAndGet();
    }

    public static Http encode(HttpRequest request, byte[] content) {
        // base field
        Http http = new Http();
        http.setId(getNewIndex());
        http.setHost(request.headers().get(HttpHeaderNames.HOST));
        http.setUri(request.uri());
        http.setHttpVersion(request.protocolVersion().toString());
        http.setMethod(request.method().toString());
        http.setHeaders(new Headers());
        http.setBody(new Body());
        http.setStatus(BigInteger.valueOf(-1));

        // header field
        HttpHeaders headers = request.headers();
        for (Map.Entry<String, String> entry : headers.entries()) {
            Header header = new Header();
            header.setKey(entry.getKey());
            header.setValue(entry.getValue());
            http.getHeaders().getHeader().add(header);
        }

        // body field
        http.getBody().setDt("bin.base64");
        http.getBody().setValue(content);

        return http;
    }

    public static Http encode(HttpResponse response, Http httpReq, byte[] content) {
        // base field
        Http http = new Http();
        http.setId(httpReq.getId());
        http.setHost(response.headers().get(HttpHeaderNames.HOST));
        http.setUri(httpReq.getUri());
        http.setHttpVersion(response.protocolVersion().toString());
        http.setMethod(httpReq.getMethod());
        http.setHeaders(new Headers());
        http.setBody(new Body());
        http.setStatus(BigInteger.valueOf(response.status().code()));

        // header field
        HttpHeaders headers = response.headers();
        for (Map.Entry<String, String> entry : headers.entries()) {
            Header header = new Header();
            header.setKey(entry.getKey());
            header.setValue(entry.getValue());
            http.getHeaders().getHeader().add(header);
        }

        // body field
        http.getBody().setDt("bin.base64");
        http.getBody().setValue(content);

        return http;
    }

    public static Http encode(HttpRequest request, ByteBuf contentBuf) {
        return encode(request, toBytes(contentBuf));
    }

    public static byte[] toBytes(ByteBuf byteBuf) {
        byteBuf.markReaderIndex();
        byte[] value = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(value);
        byteBuf.resetReaderIndex();

        return value;
    }
}
