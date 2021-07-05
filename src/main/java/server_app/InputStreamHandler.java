package server_app;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.nio.charset.StandardCharsets;

import static server_app.FileTransfer.*;

public class InputStreamHandler extends ChannelInboundHandlerAdapter {
    private static final String READY_STATUS = "ok\n";
    String[] queryHistory;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (queryHistory.length > 0) {

        } else {
            String[] query = byteBuf.toString(StandardCharsets.UTF_8)
                    .replace("\n", "")
                    .replace("\r", "")
                    .split(" ", 0);
            switch (query[0]) {
                case ("u"):
                    prepareFileInServer(ctx, query);
                    break;
                case ("d"):
                    break;
            }

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
}
