package com.vickllny;

import org.junit.Test;

import java.nio.IntBuffer;

public class NioTests {

    @Test
    public void bufferTest(){
        final IntBuffer buffer = IntBuffer.allocate(5);

        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put(i * 2);
        }
        /**
         * position 指向数组下一个读写的位置
         * limit 标识对数组的写的下标不能大于该值，否则抛异常
         * capacity 初始化容量，一经初始化不能修改
         */
        buffer.flip();

        while (buffer.hasRemaining()){
            System.out.println(buffer.get());
        }
    }
}
