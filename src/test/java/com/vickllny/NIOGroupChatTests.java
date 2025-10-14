package com.vickllny;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NIOGroupChatTests {

    @Test
    public void server() throws IOException {
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(7000));
        serverSocketChannel.configureBlocking(false);


        final Selector selector = Selector.open();

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        while (true){
            final int selected = selector.select(1000);
            if(selected == 0){
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
                    System.out.println(socketChannel.getRemoteAddress().toString() + "  上线");
                }else if(selectKey.isReadable()){
                    final SocketChannel socketChannel = (SocketChannel) selectKey.channel();

                    final ByteBuffer byteBuffer = (ByteBuffer) selectKey.attachment();

                    try {
                        int read = socketChannel.read(byteBuffer);
                        if(read > 0){
                            final String msg = new String(byteBuffer.array(), 0, read);
                            System.out.println("客户端 " + socketChannel.getRemoteAddress().toString() + "的数据：" + msg);
                            final ByteBuffer msgBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
                            //转发到其他客户端
                            final Set<SelectionKey> keys = selector.keys();
                            for (final SelectionKey key : keys) {
                                if(key != selectKey){
                                    final SelectableChannel selectableChannel = key.channel();
                                    if(selectableChannel instanceof SocketChannel && selectableChannel != socketChannel){
                                        final SocketChannel channel = (SocketChannel) selectableChannel;
                                        if(channel.isConnected()){
                                            System.out.println("转发消息给 " + channel.getRemoteAddress().toString());
                                            channel.write(msgBuffer);
                                            msgBuffer.flip();
                                        }
                                    }
                                }
                            }
                        }else if(read == -1){
                            selectKey.cancel();
                            socketChannel.close();
                        }
                    }catch (IOException e){
                        if(e.getMessage().equals("Connection reset by peer")){
                            System.out.println(socketChannel.getRemoteAddress() + "  下线");
                        }
                        selectKey.cancel();
                        socketChannel.close();
                    }

                    byteBuffer.clear();
                }
                //手动从集合中移除key
                keyIterator.remove();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        selectorClient();
        TimeUnit.SECONDS.sleep(1800);
    }

    public static void selectorClient() throws IOException, InterruptedException {


        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 7000));

//        socketChannel = socketChannel.open(); // ????? 66666 这也能行

        socketChannel.configureBlocking(false);

        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (!socketChannel.finishConnect()){
            System.out.println("等待连接成功，我没有阻塞，可以做其他工作");
            TimeUnit.MICROSECONDS.sleep(200);
        }

        new Thread(() -> {
            while (true){
                try {
                    final int read = socketChannel.read(buffer);
                    if(read > 0){
                        System.out.println("收到 " + socketChannel.getRemoteAddress().toString() + " 的消息：" + new String(buffer.array(), 0, read));
                        buffer.clear();
                    }else {
                        TimeUnit.MICROSECONDS.sleep(100);
                    }

                }catch (IOException | InterruptedException e){
                    e.printStackTrace();
                }

            }
        }).start();


        //发送数据
        try (Scanner scanner = new Scanner(System.in);){
            System.out.println("请输入内容（输入 'exit' 退出）：");
            while (true) {
                // 阻塞，等待用户输入一行
                String input = scanner.nextLine();
                final ByteBuffer buffer1 = ByteBuffer.wrap(input.getBytes(StandardCharsets.UTF_8));
                socketChannel.write(buffer1);

                // 判断是否退出
                if ("exit".equalsIgnoreCase(input)) {
                    System.out.println("程序退出。");
                    break;
                }
            }
        }

    }
}
