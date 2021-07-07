package server_app;

import io.netty.buffer.*;
import io.netty.channel.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.Arrays;

import static server_app.Action.OVERWRITE;

public class FilePrepare {
    private static final String READY_STATUS = "ok\n";
    private static final String FILE_EXIST = "ex\n";
    private static final String FILE_NOT_EXIST = "nex\n";


    public static FileInfo prepareFile(ChannelHandlerContext ctx, String[] header) {
        File file = new File("root" + File.separator + header[1]);
        ctx.writeAndFlush(Unpooled.wrappedBuffer(READY_STATUS.getBytes()));
        return new FileInfo(file, Long.parseLong(header[2]), OVERWRITE);
    }

    public static void download(ChannelHandlerContext ctx, Object msg, String[] header) throws IOException {

        String fileName = header[1];
        long fileLength = Long.parseLong(header[2]);
        File file = new File("root" + File.separator + fileName);
        if (!file.exists()) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(FILE_NOT_EXIST.getBytes()));
        } else {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(READY_STATUS.getBytes()));
        }

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        int read = 0;
        byte[] buffer = new byte[8 * 1024];
        while ((read = randomAccessFile.read(buffer)) != -1) {
            ctx.writeAndFlush(Arrays.copyOfRange(buffer, 0, read));
        }
        randomAccessFile.close();
    }
}

