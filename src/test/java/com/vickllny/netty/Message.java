package com.vickllny.netty;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Message {

    private String message;

    private String sender;

    private String target;

    private String dateTime;

    public String message(){
        return JSONObject.toJSONString(this);
    }

    public Message(){}

    public Message(String message, String sender){
        this.message = message;
        this.sender = sender;
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Message(String message, String sender, String target){
        this.message = message;
        this.sender = sender;
        this.target = target;
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Message(String message){
        this.message = message;
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String revcMessage(){
        return this.dateTime + " [" + this.sender + "]说: " + this.message;
    }

    public ByteBuf buf() {
        return Unpooled.copiedBuffer(this.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString(){
        return JSONObject.toJSONString(this);
    }
}
