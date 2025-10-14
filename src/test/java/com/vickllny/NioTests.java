package com.vickllny;

import org.junit.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        try (final FileInputStream is = new FileInputStream(path)){
            final FileChannel channel = is.getChannel();
            final ByteBuffer buffer = ByteBuffer.allocate((int) new File(path).length());

            final int read = channel.read(buffer);
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

    /**
     * UnderflowException: put不同类型的数据，get时也要保证相同的类型顺序进行获取
     */
    @Test
    public void byteBuffer1(){
        final ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.putInt(100);
        buffer.putLong(5000L);
        buffer.putChar('c');
        buffer.putShort((short) 5);

        //反转
        buffer.flip();

        System.out.println(buffer.getInt());
        System.out.println(buffer.getLong());
        System.out.println(buffer.getChar());
        System.out.println(buffer.getShort());

        final ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();
        readOnlyBuffer.putInt(111);
    }


    /**
     * MappedByteBuffer 在堆外内存中创建的buffer，可以直接在内存中修改文件，操作系统不需要拷贝
     * 使用：通过调用channel.map 方法获取mappedByteBuffer
     */
    @Test
    public void mappedByteBufferTest1() throws IOException{
        try (final RandomAccessFile file = new RandomAccessFile(path, "rw");){
            /**
             * 参数1：文件的模式，读写、只读
             * 参数2：起始位置
             * 参数3：映射的文件大小
             */
            final MappedByteBuffer byteBuffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 5);
            byteBuffer.put(0, (byte) 'H');
            byteBuffer.put(2, (byte) 'L');

        }catch (IOException e){

        }
    }

    /**
     * scattering: 将数据往buffer中写时，可同时往buffer数组写，依次写入（分散）
     * Gathering：从buffer读取数据时，可以依次读到buffer数组，依次读
     */
    @Test
    public void scatteringAndGathering() throws IOException{
        try (final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();){

            final InetSocketAddress address = new InetSocketAddress(7000);
            serverSocketChannel.socket().bind(address);

            final SocketChannel socketChannel = serverSocketChannel.accept();

            ByteBuffer[] buffers = new ByteBuffer[2];
            buffers[0] = ByteBuffer.allocate(5);
            buffers[1] = ByteBuffer.allocate(3);

            while (true){
                int maxLen = 8, readByte = 0;
                //读取
                while (readByte < maxLen){
                    final long l = socketChannel.read(buffers);
                    readByte += (int) l;
                }

                Arrays.stream(buffers).forEach(ByteBuffer::flip);
                //写回
                int writeByte = 0;
                while (writeByte < maxLen){
                    final long l = socketChannel.write(buffers);
                    writeByte += (int) l;
                }

                Arrays.stream(buffers).forEach(ByteBuffer::clear);
            }
        }
    }

    /**
     * Selector
     * 1.NIO，使用非阻塞的IO方式。可以用一个线程处理多个客户端的连接，就会使用到Selector
     * 2.多个Channel以事件的方式注册到Selector，Selector能够检测到注册的Channel是否有事件发生，如果有，便获取事件然后针对每个事件进行处理，这样就可以只用一个线程去管理多个通过的连接和请求
     * 3.只有在通道 连接或者有事件 时才会进行读写，不用一直阻塞等待，也不用为每一个通道建立创建一个线程去处理
     * 4.单线程的话也避免了线程上下文的切换开销
     *
     * 使用：
     * 服务端：
     * 1.创建 ServerSocketChannel，绑定端口，配置非阻塞
     * 2.创建Selector
     * 3.将ServerSocketChannel注册到Selector，操作类型为 OP_ACCEPT
     * 4.开启while循环，selector.select(1000)获取事件的数量
     * 5.当有事件时遍历所有的SelectionKey，判断是否有 OP_ACCEPT事件，如果有则 ServerSocketChannel.accept 返回一个 SocketChannel
     * 5.1 将 SocketChannel 设置为非阻塞，然后将 SocketChannel 注册到 Selector，注册时操作参数为 OP_READ、att 为ByteBuffer
     * 5.2 如果是 OP_READ 事件，则将 selectionKey.channel() 转换为 SocketChanel ，并获取key 绑定的att（buffer），SocketChannel 读取数据到buffer中
     * 6.最后移除 当前的SelectionKey
     */

    @Test
    public void selectorServer() throws IOException, InterruptedException {
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(7000));
        serverSocketChannel.configureBlocking(false);


        final Selector selector = Selector.open();

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            final int selected = selector.select(1000);
            if(selected == 0){
                System.out.println("没有事件发生，等待1秒");
                continue;
            }
            final Set<SelectionKey> selectionKeys = selector.selectedKeys();
            final Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()){
                final SelectionKey selectKey = keyIterator.next();
                if(selectKey.isAcceptable()){
                    final ServerSocketChannel channel = (ServerSocketChannel)selectKey.channel();
                    final SocketChannel socketChannel = channel.accept();
                    socketChannel.configureBlocking(false);
                    //将socketChannel注册到selector、设置操作类型为读、关联一个buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }else if(selectKey.isReadable()){
                    final SocketChannel socketChannel = (SocketChannel) selectKey.channel();
                    final ByteBuffer byteBuffer = (ByteBuffer) selectKey.attachment();

                    int read = socketChannel.read(byteBuffer);
                    if(read > 0){
                        System.out.println("客户端的数据：" + new String(byteBuffer.array(), 0, read));
                    }

                    byteBuffer.clear();
                }
                //手动从集合中移除key
                keyIterator.remove();
            }
        }
    }

    /**
     * 客户端使用
     * 1.创建 SocketChannel，并设置ip和端口（InetSocketAddress），同时设置为非阻塞
     * 2.使用while循环判断是否连接成功
     * 3.创建数据 ByteBuffer，然后write写入
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void selectorClient() throws IOException, InterruptedException {
        final SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        final InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 7000);

        if(!socketChannel.connect(socketAddress)){
            while (!socketChannel.finishConnect()){
                System.out.println("等待连接成功，我没有阻塞，可以做其他工作");
                TimeUnit.MICROSECONDS.sleep(200);
            }
        }

        String text = "hello, selector~~";
        final ByteBuffer buffer = ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));

        //发送数据
        socketChannel.write(buffer);
        System.in.read();
    }


}
