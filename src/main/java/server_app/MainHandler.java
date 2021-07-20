package server_app;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static server_app.Action.*;
import static server_app.FileTransfer.downloadFile;
import static server_app.FileTransfer.uploadFile;

public class MainHandler extends ChannelInboundHandlerAdapter {
    /*  Фиксирует текущеее состояния MainHandler
     * - по умолчанию MainHandler читает только заголовки;
     * - после обработки заголовка присваевается определенное состояние;
     * -- WAIT - режим ожидания (читает только заголовки);
     * -- UPLOAD - режим загрузки, после его установки пакеты летят только в FileUploader, после того
     *    как он отработает, action переходит в режим ожидания WAIT и MainHandler снова читает только заголовки;
     * -- DOWNLOAD - сразу перенаправляет соответствующий метод, после работы он также переводит сервер
     *    в режим ожидания */
    private static Action action = WAIT;
    private static File file;
    private static long transferFileLength;
    private static Action transferOptions;
    private static final String READY_STATUS = "ok";
    private static final String FILE_EXIST = "ex";
    private static final String FILE_NOT_EXIST = "nex";
    private static final Logger LOGGER = Logger.getLogger(MainHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (action) {
            case UPLOAD:
                uploadFile(msg, file, transferFileLength);
                break;
            case DOWNLOAD:
                downloadFile(ctx, msg, file);
                break;
            case WAIT:
                stringListener(ctx, msg);
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
        LOGGER.info(String.format("Received header with content: [%s]", byteBuf.toString(StandardCharsets.UTF_8)));
        switch (header[0]) {
            case ("u"):
                file = new File("root" + File.separator + header[1]);
                transferFileLength = Long.parseLong(header[2]);
                transferOptions = (header[3].equals("OVERWRITE") ? OVERWRITE : null);
                if (!file.exists()) {
                    file.createNewFile();
                } else if (file.exists() && transferOptions == null) {
                    ctx.writeAndFlush(Unpooled.wrappedBuffer((FILE_EXIST + "\n").getBytes()));
                    break;
                } else {
                    //написать логику удаления файла (добавить из коммандера)
                    file.createNewFile();
                }
                ctx.writeAndFlush(Unpooled.wrappedBuffer((READY_STATUS + "\n").getBytes()));
                action = UPLOAD;
                break;
            case ("d"):
                file = new File("root" + File.separator + header[1]);
                if (!file.exists()) {
                    ctx.writeAndFlush(Unpooled.wrappedBuffer((FILE_NOT_EXIST + "\n").getBytes()));
                }
                ctx.writeAndFlush(Unpooled.wrappedBuffer((READY_STATUS + "  " + file.length() + "\n").getBytes()));
                action = DOWNLOAD;
                break;
            case ("auth"):
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/auth-ok  Alex" + "\n").getBytes()));
                break;
        }
    }


    public static void setWaitAction() {
        file = null;
        transferOptions = null;
        transferFileLength = 0;
        action = WAIT;
    }
}
