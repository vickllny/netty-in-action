package com.vickllny.netty.heartbeat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;

            final IdleState state = event.state();

            switch (state) {
                case READER_IDLE:
                    log.debug("读空闲");
                    break;
                case WRITER_IDLE:
                    log.debug("写空闲");
                    break;
                case ALL_IDLE:
                    log.debug("写写空闲");
                    break;
            }

            final Channel channel = ctx.channel();
            final String address = channel.remoteAddress().toString().substring(1);
            log.debug("[{}]出现[{}]空闲", address, state);

        }
    }
}
