package com.vickllny.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    protected HttpServerHandler() {
        super();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest){
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            final String uri = request.uri();
            log.debug(uri);
            if(uri.equals("/favicon.ico")){

                String path = "/Users/zouq/Downloads/favicon.ico";
                final File file = new File(path);

                try (RandomAccessFile raf = new RandomAccessFile(path, "r");){

                    final DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                    final HttpHeaders headers = response.headers();
                    headers.set(HttpHeaderNames.CONTENT_TYPE, "image/vnd.microsoft.icon");
                    headers.set(HttpHeaderNames.CONTENT_LENGTH, file.length());
                    ctx.write(response);

                    ctx.write(new DefaultFileRegion(raf.getChannel(), 0, file.length())); // 零拷贝方式
                    // 5. 写入结束标记并刷新
                    ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    future.addListener(ChannelFutureListener.CLOSE);
                }
                return;
            }

            final ByteBuf buf = Unpooled.copiedBuffer("hello, http", StandardCharsets.UTF_8);

            final DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
            final HttpHeaders headers = response.headers();
            headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            headers.set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());

            ctx.writeAndFlush(response);
        }else {

        }
    }
}
