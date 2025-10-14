package com.vickllny;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BioTests {

    @Test
    public void test() throws IOException {
        final ExecutorService pool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(6666)) {
            while (true) {
                final Socket socket = serverSocket.accept();
                pool.execute(() -> handler(socket));
            }
        }
    }

    private void handler(final Socket socket) {
        byte[] data = new byte[1024];

        try {
            while (true){
                final InputStream inputStream = socket.getInputStream();
                int read;
                if((read = inputStream.read(data)) != -1){
                    System.out.println("收到了客户端的数据：" + new String(data, 0, read));
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
