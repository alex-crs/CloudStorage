package server_app;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static server_app.Action.*;
import static server_app.FilePrepare.*;
import static server_app.FileUploader.setFileInfo;

public class MainHandler extends ChannelInboundHandlerAdapter {
    /*  Фиксирует текущеее состояния MainHandler
    * - по умолчанию MainHandler читает только заголовки;
    * - после обработки заголовка присваевается определенное состояние;
    * -- WAIT - режим ожидания (читает только заголовки);
    * -- UPLOAD - режим загрузки, после его установки пакеты летят только в FileUploader, после того
    *    как он отработает, action переходит в режим ожидания WAIT и MainHandler снова читает только заголовки */
    private static Action action = WAIT;
    private static final String READY_STATUS = "ok\n";
    private static final String FILE_EXIST = "ex\n";
    private static final String FILE_NOT_EXIST = "nex\n";

    public static void setAction(Action action) {
        MainHandler.action = action;
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
        String[] header = byteBuf.toString(StandardCharsets.UTF_8)
                .replace("\n", "")
                .replace("\r", "")
                .split("  ", 0);
        switch (header[0]) {
            case ("u"):
                setFileInfo(prepareFile(ctx,header));
                action = UPLOAD;
                break;
            case ("d"):
                download(ctx, msg, header);
                action = DOWNLOAD;
                break;
        }
    }

    public static FileInfo prepareFile(ChannelHandlerContext ctx, String[] header) throws IOException {
        File file = new File("root" + File.separator + header[1]);
        ctx.writeAndFlush(Unpooled.wrappedBuffer(READY_STATUS.getBytes()));
        return new FileInfo(file, Long.parseLong(header[2]), OVERWRITE);
    }
}
