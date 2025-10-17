package com.vickllny;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;

@Slf4j
public class BigEndianTests {

    @Test
    public void test(){
        byte a = 4, b = 7, c = 9, d = 2;
        //转为int
        int  val = ((a & 0XFF) << 24) |
                ((b & 0XFF) << 16) |
                ((c & 0XFF) << 8) |
                (d & 0XFF);

        int value = ((a & 0xFF) << 24) |
                ((b & 0xFF) << 16) |
                ((c & 0xFF) << 8)  |
                ((d & 0xFF));

        log.debug("val ===>>> {}", val);


        byte b1 = 4;
        byte b2 = 7;
        byte b3 = 9;
        byte b4 = 2;

        // 注意：Java 的 ByteBuffer 默认是大端序（Big-Endian）
        int value1 = ((b1 & 0xFF) << 24) |
                ((b2 & 0xFF) << 16) |
                ((b3 & 0xFF) << 8)  |
                ((b4 & 0xFF) << 0);

        System.out.println("转换后的 int 值是: " + value1);
    }

    @Test
    public void test2(){
        byte b1 = 4;
        byte b2 = 7;
        byte b3 = 9;
        byte b4 = 2;

        int value =   ((b1 & 0xFF) << 24)  // 第1个 byte，最高位
                | ((b2 & 0xFF) << 16)  // 第2个 byte
                | ((b3 & 0xFF) << 8)   // 第3个 byte
                |  (b4 & 0xFF);        // 第4个 byte，最低位

        System.out.println("转换后的 int 值（大端序）是: " + value);
    }

    @Test
    public void test3(){
        byte[] bytes = {4, 7, 9, 2};

        int bigEndian = toIntBigEndian(bytes);
        int littleEndian = toIntLittleEndian(bytes);

        System.out.println("大端序 (Big-endian) 结果: " + bigEndian);
        System.out.println("小端序 (Little-endian) 结果: " + littleEndian);

        int value = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
        System.out.println("大端序 (Big-endian) ByteBuffer 结果: " + value);


    }



    // 大端序转换（网络字节序）
    public static int toIntBigEndian(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8)  |
                ((bytes[3] & 0xFF));
    }

    // 小端序转换（Intel x86常用）
    public static int toIntLittleEndian(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) |
                ((bytes[2] & 0xFF) << 16) |
                ((bytes[1] & 0xFF) << 8)  |
                ((bytes[0] & 0xFF));
    }
}
