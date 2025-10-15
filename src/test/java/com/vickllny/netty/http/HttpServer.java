package com.vickllny.netty.http;

import com.vickllny.netty.MyThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

public class HttpServer {

    static final int PORT = 8888;

    @Test
    public void httpServer() throws InterruptedException {
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup(new MyThreadFactory("bossGroup"));
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup(new MyThreadFactory("workerGroup"));

        final ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            final ChannelFuture channelFuture = bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpServerInitializer())
                    .bind(PORT)
                    .sync();

            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
