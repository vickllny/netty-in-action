package com.vickllny.netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class HeartBeatServer {

    @Test
    public void server() throws Exception{
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            final ChannelFuture channelFuture = bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) throws Exception {
                            final ChannelPipeline pipeline = ch.pipeline();
                            /**
                             * 加入空闲状态处理 handler
                             * 1. IdleStateHandler 是netty 提供的空闲状态处理器
                             * 2. readerIdleTime: 标识多长时间没有读，就会发送一个心跳检测包测试是否连接
                             * 3. writerIdleTime: 标识多长时间没有写，就会发送一个心跳检测包测试是否连接
                             * 4. allIdleTime: 标识多长时间没有读写，就会发送一个心跳检测包测试是否连接
                             */
                            pipeline.addLast(new IdleStateHandler(3, 4, 5, TimeUnit.SECONDS));
                            pipeline.addLast(new HeartBeatHandler());
                        }
                    })
                    .bind("127.0.0.1", 7000)
                    .sync();

            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
