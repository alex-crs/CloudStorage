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

    public static void prepareForUpload(ChannelHandlerContext ctx, Object msg, String[] fileInfo) throws IOException {
        String fileName = fileInfo[1].replace("\"", "");
        long fileLength = Long.parseLong(fileInfo[2].replace("\"", ""));
        File file = new File("root" + File.separator + fileName);
        if (!file.exists()) {
            file.createNewFile();
        } else {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(FILE_EXIST.getBytes()));
        }
        //прописать логику сообщения если файл существует
        ctx.writeAndFlush(Unpooled.wrappedBuffer(READY_STATUS.getBytes()));
        FileUploader.setFile(file);
        FileUploader.setFileLength(fileLength);
    }
}
