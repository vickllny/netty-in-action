package com.vickllny.netty.rpc.provider;

import com.vickllny.netty.rpc.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloServiceServerImpl implements HelloService {


    @Override
    public String hello(String message) {
        log.debug("收到客户端消息: {}", message);
        return "你好客户端，我已经收到你的消息[" + message + "]";
    }
}
