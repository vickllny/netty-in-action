package com.vickllny.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

@Slf4j
public class NettyServer {

    static final int PORT = 6668;

    @Test
    public void server() throws InterruptedException {
        //创建2个线程组，无限事件循环
        //bossGroup：只处理连接请求
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        //workGroup：真正处理业务的线程组，负责读写客户端数据
        final NioEventLoopGroup workGroup = new NioEventLoopGroup();

        //创建服务器端启动的对象，配置启动参数
        final ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            //配置并启动
            final ChannelFuture channelFuture = bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class) //使用NioServerSocketChannel 创建ServerSocket
                    .option(ChannelOption.SO_BACKLOG, 128) //设置线程队列的连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() { //给WorkGroup中的NioEventLoop的管理设置处理器
                        //给pipeline设置处理器
                        @Override
                        protected void initChannel(final SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new NettyServerHandler());
                        }
                    })
                    .bind(PORT).sync();

            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
        }


    }

    class NettyServerHandler extends ChannelInboundHandlerAdapter {

        /**
         * 读取客户端发送的消息
         * @param ctx 上下文，包含pipeline、channel、地址
         * @param msg 数据
         * @throws Exception
         */
        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            log.debug("server ctx = {}", ctx);
            //将msg 转为ByteBuf
            ByteBuf buf = (ByteBuf) msg;
            log.debug("客户端发送的消息: {}", buf.toString(StandardCharsets.UTF_8));
        }

        /**
         * 数据读取完毕
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
            //将数据写入到buffer并flush
            ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~", StandardCharsets.UTF_8));
        }

        /**
         * 处理异常，一般是关闭通道
         * @param ctx
         * @param cause
         * @throws Exception
         */
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            log.error("发生异常", cause);
            ctx.close();
        }
    }
}
