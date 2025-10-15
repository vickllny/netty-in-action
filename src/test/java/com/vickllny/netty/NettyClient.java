package com.vickllny.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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

            final Channel channel = channelFuture.channel();
            // ========== 启动一个线程，读取控制台输入并发送给服务端 ==========
            Thread inputThread = new Thread(() -> {

                try (final Scanner scanner = new Scanner(System.in);){
                    while (true){
                        final String line = scanner.nextLine();
                        if(line == null || line.trim().isEmpty()){
                            continue;
                        }
                        if(line.equals("exit")){
                            break;
                        }
                    }
                }
                try {
                    BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                    String line;
                    while ((line = console.readLine()) != null) {
                        if ("exit".equalsIgnoreCase(line.trim())) {
                            System.out.println("【客户端】正在退出...");
                            channel.close(); // 关闭连接
                            break;
                        }
                        // 发送用户输入的消息到服务端
                        if (!line.trim().isEmpty()) {
                            channel.writeAndFlush(Unpooled.copiedBuffer(line.getBytes(StandardCharsets.UTF_8))); // 注意换行，与编解码器匹配
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            inputThread.start();

            channel.closeFuture().sync();

            inputThread.join();
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
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            log.error("客户端捕获异常", cause);
            ctx.close();
        }
    }
}
