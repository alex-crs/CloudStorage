package client_app;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;


import static client_app.Action.COPY;
import static client_app.Action.DELETE;
import static client_app.MainWindowController.*;
import static client_app.RegistrationWindowController.DELIMETER;

public class FileOperations {

    public static void upload(String source, String target, WorkPanel sourcePanel) {
        try {
            File sourceObject = new File(source);
            File targetObject = new File(target);
            if (sourceObject.isFile()) {
                out.write(("/upload" + DELIMETER + "f" + DELIMETER + targetObject + DELIMETER
                        + sourceObject.length()).getBytes());
                while (true) {
                    String[] serverAnswer = sourcePanel.getNetworkManager().queryStringListener();
                    if ("/upload-ok".equals(serverAnswer[0].replace("\n", ""))) {
                        break;
                    } else {
                        throw new IOException();
                    }
                }
            } else {
                out.write(("/upload" + DELIMETER + "d" + DELIMETER + targetObject + DELIMETER
                        + sourceObject.length()).getBytes());
            }
            if (sourceObject.isFile() && sourceObject.length() != 0) {
                RandomAccessFile randomAccessFile = new RandomAccessFile(sourceObject, "rw");
                int read = 0;
                byte[] buffer = new byte[8 * 1024];
                while ((read = randomAccessFile.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                byteBuffer.clear();
                randomAccessFile.close();
                out.flush();
            }
            while (true) {
                String[] serverAnswer = sourcePanel.getNetworkManager().queryStringListener();
                if ("/status-ok".equals(serverAnswer[0].replace("\n", ""))) {
                    updateAllFilesLists();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void download(WorkPanel sourcePanel, WorkPanel targetPanel) {
            File file = new File(targetPanel.getCurrentPath() + File.separator
                    + sourcePanel.getMarkedFileList().get(0).replaceAll(".:", ""));
            long downloadFileLength = 0;
            try {
                out.write(("/download" + DELIMETER + sourcePanel.getCurrentPath() + File.separator
                        + sourcePanel.getMarkedFileList().get(0).replaceAll(".:", "")).getBytes());
                String[] serverAnswer = sourcePanel.getNetworkManager().queryStringListener();
                if (!file.exists()) {
                    file.createNewFile();
                }
                while (true) {
                    if ("/download-ok".equals(serverAnswer[0])) {
                        downloadFileLength = Long.parseLong(serverAnswer[1]);
                        break;
                    }
                }
                out.write(" ".getBytes());
                out.flush();
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                FileChannel fileChannel = randomAccessFile.getChannel();
                while ((rbc.read(byteBuffer)) > 0) {
                    byteBuffer.flip();
                    fileChannel.position(file.length());
                    fileChannel.write(byteBuffer);
                    byteBuffer.compact();
                    if (file.length() == downloadFileLength) {
                        updateAllFilesLists();
                        break;
                    }
                }
                byteBuffer.clear();
                fileChannel.close();
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void multipleElementCopy(WorkPanel sourcePanel, WorkPanel targetPanel, String element) throws IOException {
        Path source = Path.of(sourcePanel.getCurrentPath() + File.separator + element);
        Path target = Path.of(targetPanel.getCurrentPath() + File.separator + element);
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = target.resolve(source.relativize(dir));
                    sourcePanel.getNetworkManager().makeDir(targetPath.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    upload(file.toString(), target.resolve(source.relativize(file)).toString(),sourcePanel);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        }
    }



    /*Метод для копирования по заданному пути. Необходимо передать текущую директорию для обновления списка файлов
    после копирования.*/
    public static void copy(Path source, Path target) throws IOException {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = target.resolve(source.relativize(dir));
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    public static void delete(StringBuilder path, String fileName) throws IOException {
        Files.walkFileTree(Path.of(path + File.separator + fileName), new SimpleFileVisitor<Path>() {
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
        updateAllFilesLists();
    }

    //удаляет пробелы в конце пути (полезно при создании папки с пробелами на конце, во избежании ошибки)
    public static String clearEmptySymbolsAfterName(String name) {
        StringBuilder string = new StringBuilder().append(name);
        while (true) {
            if (!string.toString().endsWith(" ")) {
                break;
            }
            string.deleteCharAt(string.length() - 1);
        }
        return string.toString();
    }


}
