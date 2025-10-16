package com.vickllny.netty.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class ProtoBufServer {

    @Test
    public void server() throws Exception{
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            final ChannelFuture channelFuture = bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) throws Exception {
                            final ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast("decoder", new ProtobufDecoder(UserPOJO.User.getDefaultInstance()));

                            pipeline.addLast(new ProtoBufHandler());
                        }

                    })
                    .bind(7000)
                    .sync();

            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Slf4j
    static class ProtoBufHandler extends SimpleChannelInboundHandler<UserPOJO.User> {

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final UserPOJO.User msg) throws Exception {
             log.debug("id: {}, name: {}", msg.getId(), msg.getName());
        }
    }

    @Test
    public void client() throws Exception{
        final NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            final Bootstrap bootstrap = new Bootstrap();
            final ChannelFuture channelFuture = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) throws Exception {
                            final ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(new ProtobufEncoder());

                            pipeline.addLast(new ProtoBufClientHandler());
                        }
                    })
                    .connect("127.0.0.1", 7000)
                    .sync();

            channelFuture.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }
    }

    static class ProtoBufClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            final UserPOJO.User user = UserPOJO.User.newBuilder().setId(123456).setName("vickllny").build();

            ctx.writeAndFlush(user);
        }
    }
}
