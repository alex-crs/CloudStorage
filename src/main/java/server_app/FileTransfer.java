package server_app;

import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static server_app.Action.DOWNLOAD;

public class FileTransfer {
    private static final String READY_STATUS = "ok";
    private static final String FILE_EXIST = "ex\n";
    private static final String FILE_NOT_EXIST = "nex\n";

    public static void uploadFile(Object msg, File file, long transferFileLength) {
            ByteBuf byteBuf = (ByteBuf) msg;
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            FileChannel fileChannel = randomAccessFile.getChannel();
            while (byteBuffer.hasRemaining()) {
                fileChannel.position(file.length());
                fileChannel.write(byteBuffer);
            }

            //------ не забыть убрать
            //временная служебная информация о перемещении файла в %
            System.out.print("\r" + "Transfer %: " + (file.length() * 100) / transferFileLength);
            //------ не забыть убрать

            if (transferFileLength == file.length()) {
                MainHandler.setWaitAction();
            }

            byteBuf.release();
            fileChannel.close();
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downloadFile(ChannelHandlerContext ctx, Object msg, File file) throws IOException {
        //ByteBuf byteBuf = (ByteBuf) msg;
        //if ("ok".equals(byteBuf.toString(StandardCharsets.UTF_8))) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            int read = 0;
            byte[] buffer = new byte[8 * 1024];
            while ((read = randomAccessFile.read(buffer)) > 0) {
                ctx.writeAndFlush(Arrays.copyOfRange(buffer, 0, read));
            }
            randomAccessFile.close();
            MainHandler.setWaitAction();
        //}
        //byteBuf.release();

    }


}

