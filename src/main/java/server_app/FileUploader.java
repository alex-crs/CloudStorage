package server_app;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static server_app.Action.WAIT;
import static server_app.MainHandler.*;

public class FileUploader extends ChannelInboundHandlerAdapter {
    private static FileInfo fileInfo;

    public static void setFileInfo(FileInfo fileInfo) {
        FileUploader.fileInfo = fileInfo;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!fileInfo.getFile().exists()) {
            fileInfo.getFile().createNewFile();
        }
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBuffer byteBuffer = byteBuf.nioBuffer();
        RandomAccessFile randomAccessFile = new RandomAccessFile(fileInfo.getFile(), "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        while (byteBuffer.hasRemaining()) {
            fileChannel.position(fileInfo.getFile().length());
            fileChannel.write(byteBuffer);
        }

        //------ не забыть убрать
        //временная служебная информация о перемещении файла в %
        System.out.print("\r" + "Transfer %: " +
                (fileInfo.getFile().length() * 100) / fileInfo.getTargetFileLength());
        //------ не забыть убрать

        if (fileInfo.getTargetFileLength() == fileInfo.getFile().length()) {
            setAction(WAIT);
            fileInfo = null;
        }

        byteBuf.release();
        fileChannel.close();
        randomAccessFile.close();
    }
}
