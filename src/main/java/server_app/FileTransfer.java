package server_app;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileTransfer {
    private static final String READY_STATUS = "ok\n";
    private static final String FILE_EXIST = "ex\n";

    public static FileDownloader prepareFileInServer(ChannelHandlerContext ctx, String[] fileInfo) throws IOException {
        String fileName = fileInfo[1].replace("\"", "");
        long fileLength = Long.parseLong(fileInfo[2].replace("\"", ""));
        File file = new File("root" + File.separator + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        ctx.writeAndFlush(Unpooled.wrappedBuffer(READY_STATUS.getBytes()));
        return new FileDownloader(file, "d", fileLength);
    }

    public void downloadFile(Object msg) throws IOException {
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBuffer byteBuffer = byteBuf.nioBuffer();
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        while (byteBuffer.hasRemaining()) {
            fileChannel.position(file.length());
            fileChannel.write(byteBuffer);
        }

        byteBuf.release();
        fileChannel.close();
        randomAccessFile.close();
    }
}
