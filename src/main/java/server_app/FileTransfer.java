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
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            int read = 0;
            byte[] buffer = new byte[8 * 1024];
            while ((read = randomAccessFile.read(buffer)) > 0) {
                ctx.writeAndFlush(Arrays.copyOfRange(buffer, 0, read));
            }
            randomAccessFile.close();
            MainHandler.setWaitAction();
    }


}

