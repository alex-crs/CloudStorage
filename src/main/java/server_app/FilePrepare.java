package server_app;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FilePrepare {
    private static final String READY_STATUS = "ok\n";
    private static final String FILE_EXIST = "ex\n";
    private static final String FILE_NOT_EXIST = "nex\n";

    public static void prepareForUpload(ChannelHandlerContext ctx, Object msg, String[] fileInfo) throws IOException {
        File file = new File("root" + File.separator + fileInfo[1]);
        if (!file.exists()) {
            file.createNewFile();
        }

//        } else {
//            ctx.writeAndFlush(Unpooled.wrappedBuffer(FILE_EXIST.getBytes()));
//        }
        //прописать логику сообщения если файл существует
        ctx.writeAndFlush(Unpooled.wrappedBuffer(READY_STATUS.getBytes()));
        FileUploader.setFile(file);
        FileUploader.setFileLength(Long.parseLong(fileInfo[2]));
    }
    public static void prepareForDownload(ChannelHandlerContext ctx, Object msg, String[] fileInfo) throws IOException {
        ByteBuf byteBuf = (ByteBuf) msg;
        String fileName = fileInfo[1].replace("\"", "");
        long fileLength = Long.parseLong(fileInfo[2].replace("\"", ""));
        File file = new File("root" + File.separator + fileName);
        if (!file.exists()) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(FILE_NOT_EXIST.getBytes()));
        } else {

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            int read = 0;
            byte[] buffer = new byte[8 * 1024];
            while ((read = randomAccessFile.read(buffer)) != -1) {
                ctx.writeAndFlush(buffer);
            }

        }
    }
}
