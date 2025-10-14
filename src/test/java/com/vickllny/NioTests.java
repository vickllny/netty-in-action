package com.vickllny;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class NioTests {

    @Test
    public void bufferTest(){
        /**
         * buffer本质是一个容器对象，底层实现原理是一个数组，通过position、limit、capacity、mark控制该缓冲区的读写
         */
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

    /**
     * channel，类似于bio中的一个socket连接或者流
     * 1.可以同时进行读写，而流只能进行读或者写
     * 2.可以异步读写，而流只能读或写
     * 3.读写数据是通过buffer
     * 4.BIO中的流是单向的，而channel是双向操作，可进行读写
     *
     * Channel常用的有：FileChannel、DatagramChannel、ServerSocketChannel(ServerSocketChannelImpl)、SocketChannel(SocketChannelImpl)
     */

    String path = "/Users/zouq/code/java/netty-in-action/src/main/resources/fileChannel.txt";
    String path1 = "/Users/zouq/code/java/netty-in-action/src/main/resources/fileChannel1.txt";

    /**
     * 写文件
     * @throws IOException
     */
    @Test
    public void fileChannelTest1() throws IOException {
        String txt = "hello,fileChannel";

        try (final FileOutputStream os = new FileOutputStream(path)){
            final FileChannel channel = os.getChannel();

            final ByteBuffer buffer = ByteBuffer.allocate(1024);
            final byte[] bytes = txt.getBytes(StandardCharsets.UTF_8);
            buffer.put(bytes);
            //反转，让写变成读
            buffer.flip();

            final int write = channel.write(buffer);
            if(write > 0){
                System.out.println("写入通道成功");
            }else {
                System.out.println("写入通道失败");
            }
        }

    }

    /**
     * 读文件
     * @throws IOException
     */
    @Test
    public void fileChannelTest2() throws IOException {
        try (final FileInputStream is = new FileInputStream(path);
             final FileOutputStream os = new FileOutputStream(path1)){
            final FileChannel channel = is.getChannel();
            final ByteBuffer buffer = ByteBuffer.allocate((int) new File(path).length());

            final int read = channel.read(buffer);
            final FileChannel fileOsChannel = os.getChannel();
            channel.transferTo(0, read, fileOsChannel);
            if(read > 0){
                System.out.println("读取到的数据：" + new String(buffer.array(), 0 , read));
            }else {
                System.out.println("读取失败");
            }
        }
    }

    /**
     * 拷贝文件
     * @throws IOException
     */
    @Test
    public void fileChannelTest3() throws IOException{
        try (final FileInputStream is = new FileInputStream(path);
             final FileOutputStream os = new FileOutputStream(path1)){
            final FileChannel channel = is.getChannel();
            //使用一个buffer实现文件的拷贝
            final ByteBuffer buffer = ByteBuffer.allocate(5);

            final FileChannel fileOsChannel = os.getChannel();
            int read, l = 0;
            while ((read = channel.read(buffer)) > 0){
                l += read;
                //反转
                buffer.flip();
                fileOsChannel.write(buffer);
                buffer.clear(); //必须复位
            }

            if(l > 0){
                System.out.println("拷贝文件成功：" + l + "字节");
            }else {
                System.out.println("拷贝失败");
            }
        }
    }

    /**
     * 拷贝文件 使用transferFrom
     * @throws IOException
     */
    @Test
    public void fileChannelTest4() throws IOException{
        try (final FileInputStream is = new FileInputStream(path);
             final FileOutputStream os = new FileOutputStream(path1);
             final FileChannel channel = is.getChannel();
             final FileChannel fileOsChannel = os.getChannel();){

            final long l = fileOsChannel.transferFrom(channel, 0, channel.size());

            if(l > 0){
                System.out.println("拷贝文件成功：" + l + "字节");
            }else {
                System.out.println("拷贝失败");
            }
        }
    }

    /**
     * 拷贝文件 使用 transferTo
     * @throws IOException
     */
    @Test
    public void fileChannelTest5() throws IOException{
        try (final FileInputStream is = new FileInputStream(path);
             final FileOutputStream os = new FileOutputStream(path1);
             final FileChannel channel = is.getChannel();
             final FileChannel fileOsChannel = os.getChannel();){

//            final long l = fileOsChannel.transferFrom(channel, 0, channel.size());

            final long l = channel.transferTo(0, channel.size(), fileOsChannel);

            if(l > 0){
                System.out.println("拷贝文件成功：" + l + "字节");
            }else {
                System.out.println("拷贝失败");
            }
        }
    }
}
