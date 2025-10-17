package com.vickllny.netty.rpc.contomer;

import com.vickllny.netty.rpc.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RpcClient {

    static RpcClientHandler rpcClientHandler = new RpcClientHandler();

    public static void main(String[] args) throws Exception {
        final RpcClient client = new RpcClient();
        client.client();
    }

    @Test
    public void client() throws Exception {
        final NioEventLoopGroup workGroup = new NioEventLoopGroup();

        final Bootstrap bootstrap = new Bootstrap();

        try {
            final ChannelFuture channelFuture = bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
//                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new RpcClientInitializer())
                    .connect("127.0.0.1", 9999)
                    .sync();

            new Thread(() -> {

                try (final Scanner scanner = new Scanner(System.in);){
                        while (true){
                            final String string = scanner.nextLine();
                            if(string == null || string.trim().isEmpty()){
                                continue;
                            }
                            final HelloService helloService = (HelloService) getBean(HelloService.class, "HelloService#hello#");
                            final String message = helloService.hello(string);
                            log.debug("收到服务端的消息: {}", message);
                        }
                }
            }).start();

            channelFuture.channel().closeFuture().sync();
        }finally {
            workGroup.shutdownGracefully();
        }
    }

    static class RpcClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(final SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new StringDecoder());
            pipeline.addLast(new StringEncoder());

            pipeline.addLast(rpcClientHandler);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class RpcClientHandler extends SimpleChannelInboundHandler<String> implements Callable<String> {
        private volatile ChannelHandlerContext context;

        private volatile String result;
        private String param;


        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            this.context = ctx;
        }

        @Override
        protected synchronized void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
            result = msg;
            notify();
        }

        @Override
        public synchronized String call() throws Exception {
            context.writeAndFlush(this.param);
            wait();
            return result;
        }
    }


    static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static Object getBean(Class<?> serviceClass, final String providerName) {

        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{serviceClass}, ((proxy, method, args) -> {
            rpcClientHandler.setParam(providerName + args[0]);
            return threadPool.submit(rpcClientHandler).get();
        }));
    }


}
