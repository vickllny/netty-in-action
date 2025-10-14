package com.vickllny;

import org.junit.Test;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 零拷贝技术：并不是没有拷贝次数，目标是没有CPU拷贝（从操作系统角度看）
 * DMA技术：使用内存直接从硬件拷贝到内核态
 * 1.mmap：在用户态使用一块内存映射到内核空间buffer，根据映射的地址直接修改内核态的buffer，从而不用将buffer从内核态/用户态反复拷贝，但是还是需要进行 内核态/用户态  上下文的切换
 * 2.sendFile：从Linux2.1开始，数据根本不经过用户态，直接从内核态buffer拷贝到 socketBuffer，由于从内核态拷贝到Socket Buffer中，不经过用户态，减少了一次上下文切换，
 *      从Linux2.4开始，大部分数据从内核Buffer直接拷贝到协议栈(Protocol Engine)，极少部分数据（buffer的len、offset等信息，可以忽略）还是会拷贝到SocketBuffer
 * 4.NIO如何使用零拷贝 transferTo 底层使用的就是是零拷贝技术
 */
public class ZeroCopyTests {


    @Test
    public void client() throws Exception{
        //定义socketChannel
        final SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 7000));

        //文件
//        String filePath = "/Users/zouq/code/java/netty-in-action/src/main/resources/fileChannel.txt";
        String filePath = "/Users/zouq/Downloads/ideaIC-2025.2.1-aarch64.dmg";
        final FileChannel channel = new FileInputStream(filePath).getChannel();

        final long beginTime = System.currentTimeMillis();

        //在Linux下单次拷贝文件大小没有限制
        long copied;
        channel.transferTo(0, (copied = channel.size()), socketChannel);
        //在Windows下单次拷贝文件大小最大8M，实现如下
//        long windowsCopyMaxSize = 8 * 1024 * 1024, copied = 0;
//        while (copied < channel.size()){
//            copied += channel.transferTo(copied, Math.min(channel.size() - copied, windowsCopyMaxSize), socketChannel);
//        }

        final long endTime = System.currentTimeMillis();
        //1261937760
        System.out.println("文件大小:" + copied + ", 耗时: " + (endTime - beginTime) + "ms");

    }

    @Test
    public void server() throws Exception{
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(7000));

        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true){

            final SocketChannel socketChannel = serverSocketChannel.accept();
            int read = 0, count = 0;
            while ((read = socketChannel.read(buffer)) > 0){
                count += read;
                buffer.rewind();
            }
            System.out.println("读取到的字节：" + count);
        }
    }
}
