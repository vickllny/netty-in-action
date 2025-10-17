package com.vickllny.netty.rpc.provider;

import com.vickllny.netty.MyThreadFactory;
import com.vickllny.netty.rpc.HelloService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

public class RpcServer {


    @Test
    public void server() throws Exception{
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup(new MyThreadFactory("bossGroup"));
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup(new MyThreadFactory("workerGroup"));

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();

            final ChannelFuture channelFuture = bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new RpcServerChannelInitializer())
                    .bind(9999)
                    .sync();

            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    static class RpcServerChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(final SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new StringDecoder());
            pipeline.addLast(new StringEncoder());

            pipeline.addLast(new RpcServerHandler());
        }
    }

    @Slf4j
    static class RpcServerHandler extends SimpleChannelInboundHandler<String> {

        static final HelloService helloService = new HelloServiceServerImpl();

        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
            final String servicePrefix = "HelloService#hello#";
            //获取客户端消息并调用服务
            if(msg.startsWith(servicePrefix)){
                final String res = helloService.hello(msg.substring(servicePrefix.length()));
                ctx.writeAndFlush(res);
            }
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            log.error("发生异常", cause);
            ctx.close();
        }
    }
}
