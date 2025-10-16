package com.vickllny.netty.websocket;

import com.vickllny.netty.MyThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.LocalDateTime;

public class WebSocketServer {

    @Test
    public void server() throws Exception{
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup(new MyThreadFactory("bossGroup"));
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup(new MyThreadFactory("workerGroup"));

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();

            final ChannelFuture channelFuture = bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) throws Exception {
                            final ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(new HttpServerCodec());

                            pipeline.addLast(new ChunkedWriteHandler());

                            pipeline.addLast(new HttpObjectAggregator(8192));

                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                            //自定义处理器
                            pipeline.addLast(new TextWebSocketHandler());
                        }
                    })
                    .bind(9999)
                    .sync();


            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Slf4j
    static class TextWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final TextWebSocketFrame msg) throws Exception {
            log.debug("服务端收到客户端[{}]的消息: {}", ctx.channel().remoteAddress().toString().substring(1), msg.text());
            //回复
            ctx.writeAndFlush(new TextWebSocketFrame("服务器时间：" + LocalDateTime.now() + ", 消息：" + msg.text()));
        }

        @Override
        public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
            //唯一id
            final String clientId = ctx.channel().id().asLongText();
            log.debug("handlerAdded 客户端id={}", clientId);
        }

        @Override
        public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
            //唯一id
            final String clientId = ctx.channel().id().asLongText();
            log.debug("handlerRemoved 客户端id={}", clientId);
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            log.error("异常发生", cause);
            ctx.close();
        }
    }
}
