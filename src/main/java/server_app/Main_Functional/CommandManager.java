package server_app.Main_Functional;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import server_app.Handlers.MainHandler;
import server_app.Resources.CSUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import static server_app.Handlers.MainHandler.*;

public class CommandManager {

    public static void uploadFile(ChannelHandlerContext ctx, Object msg, File file, long transferFileLength) {
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
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok" + "\n").getBytes()));
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

    public static void copy(CSUser user, String source, String target, ChannelHandlerContext ctx) throws IOException {
        try {
            Path fromPath = Path.of(user.getRoot() + source);
            Path toPath = Path.of(user.getRoot() + target);
            Files.walkFileTree(fromPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = toPath.resolve(fromPath.relativize(dir));
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, toPath.resolve(fromPath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        }
        ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok").getBytes()));
    }

    public static String getFilesList(CSUser client, String path) {
        String[] directoryElements = new File(client.getRoot() + path).list();
        for (int i = 0; i < directoryElements.length; i++) {
            File file = new File(client.getRoot() + path + directoryElements[i]);
            if (file.isDirectory()) {
                directoryElements[i] = "d:" + directoryElements[i];
            } else if (file.isFile()) {
                directoryElements[i] = "f:" + directoryElements[i];
            }
        }
        return String.join(DELIMETER, directoryElements);
    }

    public static void makeDir(CSUser user, String fileName, ChannelHandlerContext ctx) {
        try {
            Files.createDirectory(Path.of(user.getRoot() + fileName));
        } catch (IOException e) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-bad").getBytes()));
        }
        ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok").getBytes()));
    }

    public static void touchFile(CSUser user, String fileName, ChannelHandlerContext ctx) {
        try {
            Files.createFile(Path.of(user.getRoot() + fileName));
        } catch (IOException e) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-bad").getBytes()));
        }
        ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok").getBytes()));
    }

    public static void renameDir(CSUser user, String oldName, String newName) {
        File oldFileName = new File(user.getRoot() + oldName);
        File newFileName = new File(user.getRoot() + newName);
        oldFileName.renameTo(newFileName);
    }

    public static void removeFileOrDirectory(CSUser user, String name, ChannelHandlerContext ctx) throws IOException {
        Files.walkFileTree(Path.of(user.getRoot() + name), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok").getBytes()));
    }

}
