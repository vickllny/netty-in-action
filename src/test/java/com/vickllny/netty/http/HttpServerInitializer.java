package com.vickllny.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ch.pipeline().addLast("httpServerCodec", new HttpServerCodec()); //netty 提供的http编解码器
//        ch.pipeline().addLast(new HttpObjectAggregator(65536)); // 聚合 HTTP 请求为 FullHttpRequest
//        ch.pipeline().addLast(new ChunkedWriteHandler());
        ch.pipeline().addLast(new HttpServerHandler());
    }
}
