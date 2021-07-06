package server_app;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static server_app.Action.*;
import static server_app.FilePrepare.*;

public class InputStreamHandler extends ChannelInboundHandlerAdapter {
    private static Action action = WAIT;


    public static void setAction(Action action) {
        InputStreamHandler.action = action;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (action){
            case UPLOAD:
                ctx.fireChannelRead(msg);
                break;
            case DOWNLOAD:
                //ctx.fireChannelRead(msg);
                break;
            default:
                stringListener(ctx,msg);
                break;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected " + ctx.channel().localAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected " + ctx.channel().localAddress());
    }

    public static void stringListener(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf byteBuf = (ByteBuf) msg;
        String[] query = byteBuf.toString(StandardCharsets.UTF_8)
                .replace("\n", "")
                .replace("\r", "")
                .split("  ", 0);
        switch (query[0]) {
            case ("u"):
                prepareForUpload(ctx, msg, query);
                action = UPLOAD;
                break;
            case ("d"):
                prepareForDownload(ctx, msg, query);
                action = DOWNLOAD;
                break;
        }
    }
}
