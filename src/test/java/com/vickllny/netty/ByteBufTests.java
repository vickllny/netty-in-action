package com.vickllny.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class ByteBufTests {

    @Test
    public void test(){
        final ByteBuf buffer = Unpooled.buffer(10);

        for (int i = 0; i < 10; i++) {
            buffer.writeByte(i);
            log.debug("writerIndex ===>>> {}", buffer.writerIndex());
        }

        for (int i = 0; i < 10; i++) {
            buffer.readByte();
            log.debug("readerIndex ===>>> {}", buffer.readerIndex());
        }

        log.debug("readableBytes ===>>> {}", buffer.readableBytes());
    }
}
