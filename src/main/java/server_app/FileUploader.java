package server_app;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUploader extends ChannelInboundHandlerAdapter {
    private static long fileLength;
    private static File file;

    public static void setFile(File file) {
        FileUploader.file = file;
    }

    public static void setFileLength(long fileLength) {
        FileUploader.fileLength = fileLength;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBuffer byteBuffer = byteBuf.nioBuffer();
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        while (byteBuffer.hasRemaining()) {
            fileChannel.position(file.length());
            fileChannel.write(byteBuffer);
        }
        System.out.print("\r" + "Transfer %: " + (file.length() * 100) / fileLength);
        if (fileLength == file.length()) {
            InputStreamHandler.setUploadPermission(false);
            file = null;
            fileLength = 0;
        }
        byteBuf.release();
        fileChannel.close();
        randomAccessFile.close();

    }
}
