package com.vickllny.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
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
                            channel.pipeline().addLast(new StringEncoder());
                            channel.pipeline().addLast(new StringDecoder());
                            channel.pipeline().addLast(new MessageDecoder());
                            channel.pipeline().addLast(new NettyClientHandler());
                        }
                    }).connect("127.0.0.1", NettyServer.PORT).sync();

            final Channel channel = channelFuture.channel();

            final String clientId = channel.localAddress().toString().substring(1);
            // ========== 启动一个线程，读取控制台输入并发送给服务端 ==========
            Thread inputThread = new Thread(() -> {

                try {
                    BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                    String line;
                    while ((line = console.readLine()) != null) {
                        final String lingString = line.trim();
                        if ("exit".equalsIgnoreCase(lingString)) {
                            System.out.println("【客户端】正在退出...");
                            channel.close(); // 关闭连接
                            break;
                        }if(lingString.startsWith("/msg")){
                            final String substring = lingString.substring(4).trim();
                            final String target = substring.substring(0, substring.indexOf(" "));
                            final String message = substring.substring(target.length());
                            final Message msg = new Message(message, clientId, target);
                            channel.writeAndFlush(msg.buf());
                        }else {
                            final Message msg = new Message(lingString, clientId);
                            channel.writeAndFlush(msg.buf());
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

    static class NettyClientHandler extends SimpleChannelInboundHandler<Message> {

        /**
         * 当通道就绪时会触发该方法
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            final String clientId = ctx.channel().localAddress().toString().substring(1);
            String message = "hello, i am [" + clientId + "]";
            ctx.writeAndFlush(new Message(message, clientId).buf());
        }

        /**
         * 当通道有读取事件时触发
         * @param ctx
         * @param message
         * @throws Exception
         */
        @Override
        public void channelRead0(final ChannelHandlerContext ctx, final Message message) throws Exception {
            log.debug(message.revcMessage());
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            log.error("客户端捕获异常", cause);
            ctx.close();
        }
    }
}
