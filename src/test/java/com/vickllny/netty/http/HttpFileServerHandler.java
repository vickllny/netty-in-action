//package com.vickllny.netty.http;
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.codec.http.FullHttpRequest;
//
//public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
//
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
//        // 1. 解析 URI，获取客户端请求的文件路径（这里简单处理，默认返回 defaultFilePath）
//        // 你可以扩展为根据 request.uri() 动态返回不同文件
//        String filePath = this.defaultFilePath;
//
//        File file = new File(filePath);
//        if (!file.exists() || !file.isFile()) {
//            sendError(ctx, HttpResponseStatus.NOT_FOUND, "File not found: " + filePath);
//            return;
//        }
//
//        // 2. 构造 HTTP 响应头
//        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
//        HttpUtil.setContentLength(response, file.length());
//        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
//        response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION,
//                "attachment; filename=\"" + file.getName() + "\"");
//
//        // 3. 返回响应头 + 文件内容
//        ctx.write(response); // 先写 HTTP 响应头
//
//        // 4. 使用 ChunkedFile 或 DefaultFileRegion 高效传输文件内容
//        RandomAccessFile raf = new RandomAccessFile(file, "r");
//        ctx.write(new DefaultFileRegion(raf.getChannel(), 0, file.length())); // 零拷贝方式，推荐 ✅
//
//        // 5. 写入结束标记并刷新
//        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
//        future.addListener(ChannelFutureListener.CLOSE); // 请求完成后关闭连接（HTTP/1.1 可保持长连接，根据需求）
//    }
//
//    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String errorMsg) {
//        FullHttpResponse response = new DefaultFullHttpResponse(
//                HttpVersion.HTTP_1_1,
//                status,
//                ctx.alloc().buffer().writeBytes(errorMsg.getBytes(StandardCharsets.UTF_8))
//        );
//        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
//        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();
//        ctx.close();
//    }
//}
