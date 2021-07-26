package server_app.Handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.apache.log4j.Logger;
import server_app.Resources.Action;
import server_app.Resources.CSUser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static server_app.Resources.Action.*;
import static server_app.SQL_Controllers.AuthService.tryToAuth;
import static server_app.SQL_Controllers.AuthService.tryToReg;
import static server_app.Main_Functional.CommandManager.*;

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
    private static final String READY_STATUS = "/upload-ok";
    public static String DELIMETER = ";";
    private static CSUser csUser;
    private static final Logger LOGGER = Logger.getLogger(MainHandler.class);

    public static void setCsUser(String user) {
        csUser = new CSUser(user);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (action) {
            case UPLOAD:
                uploadFile(ctx, msg, file, transferFileLength);
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

    public void stringListener(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf byteBuf = (ByteBuf) msg;
        String[] header = byteBuf.toString(StandardCharsets.UTF_8)
                .replace("\n", "")
                .replace("\r", "")
                .split(DELIMETER, 0);
        LOGGER.info(String.format("Received header with content: [%s]", byteBuf.toString(StandardCharsets.UTF_8)));
        switch (header[0]) {
            case ("/upload"):
                if ("f".equals(header[1])) {
                    file = new File(csUser.getRoot() + header[2]);
                    transferFileLength = Long.parseLong(header[3]);
//                    transferOptions = (header[4].equals("OVERWRITE") ? OVERWRITE : null); //логика не реализована
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    ctx.writeAndFlush(Unpooled.wrappedBuffer((READY_STATUS + "\n").getBytes()));
                    if (transferFileLength != 0) {
                        action = UPLOAD;
                    } else {
                        ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok" + "\n").getBytes()));
                    }
                }
                if ("d".equals(header[1])) {
                    Files.createDirectory(Path.of(csUser.getCurrentPath() + File.separator + header[2]));
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok" + "\n").getBytes()));
                }
                break;
            case ("/download"):
                file = new File(csUser.getRoot() + header[1]);
                if (!file.exists()) {
//                    ctx.writeAndFlush(Unpooled.wrappedBuffer((FILE_NOT_EXIST + "\n").getBytes()));
                }
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/download-ok" + DELIMETER + file.length()).getBytes()));
                action = DOWNLOAD;
                break;
            case ("/auth"):
                tryToAuth(ctx, header);
                break;
            case ("/signup"):
                tryToReg(ctx, header);
                break;
            case ("/ls"):
                ctx.writeAndFlush(Unpooled.wrappedBuffer((DELIMETER + getFilesList(csUser, header[1])).getBytes()));
                break;
            case ("/cd"):
                File file = new File(csUser.getRoot() + header[1]);
                if (file.exists()) {
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok").getBytes()));
                }
                break;
            case ("/mkdir"):
                makeDir(csUser, header[1], ctx);
                break;
            case ("/touch"):
                touchFile(csUser, header[1], ctx);
                break;
            case ("/rename"):
                renameDir(csUser, header[1], header[2]);
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok").getBytes()));
                break;
            case ("/delete"):
                removeFileOrDirectory(csUser, header[1], ctx);
                break;
            case ("/copy"):
                copy(csUser, header[1], header[2], ctx);
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