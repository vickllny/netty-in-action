package com.vickllny.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class NettyClient {


    public static void main(String[] args) throws InterruptedException {
        //线程组
        final NioEventLoopGroup workGroup = new NioEventLoopGroup();

        //客户端启动对象
        final Bootstrap bootstrap = new Bootstrap();
        try {
            //设置参数并启动
            final ChannelFuture channelFuture = bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class) //客户端通道实现类
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new NettyClientHandler());
                        }
                    }).connect("127.0.0.1", NettyServer.PORT).sync();

            channelFuture.channel().closeFuture().sync();
        }finally {
            workGroup.shutdownGracefully();
        }

    }

    static class NettyClientHandler extends ChannelInboundHandlerAdapter {

        /**
         * 当通道就绪时会触发该方法
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.copiedBuffer("hello, server: 喵喵喵", StandardCharsets.UTF_8));
        }

        /**
         * 当通道有读取事件时触发
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            log.debug("收到服务器[{}]消息: {}", ctx.channel().remoteAddress(), buf.toString(StandardCharsets.UTF_8));
            log.debug("");
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            log.error("客户端捕获异常", cause);
            ctx.close();
        }
    }
}
