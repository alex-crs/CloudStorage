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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
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

    public static void showLocalDirectory(StringBuilder directory, ListView<String> fileList) {
        try {
            File fileDirectory = new File(directory.toString());
            String[] tokens = fileDirectory.list();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    fileList.getItems().clear();
                    if (directory.toString().split(Matcher.quoteReplacement(File.separator)).length >= 2) {
                        fileList.getItems().add("BACK");
                    }
                    for (int i = 0; i < tokens.length; i++) {
                        fileList.getItems().add(tokens[i]);
                    }

                }
            });
            fileList.setCellFactory(l -> new ListCell<String>() {
                @Override
                public void updateItem(String friend, boolean empty) {
                    super.updateItem(friend, empty);
                    try {
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else if ("BACK".equals(friend)) {
                            setText(friend);
                            setGraphic(new ImageView(new Image("/images/arrow.png")));
                        } else if ((!friend.contains("d:")) && (!friend.contains("f:"))) {
                            File file = new File(directory + File.separator + friend);
                            ImageIcon imageIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
                            if (imageIcon != null) {
                                java.awt.Image imageIconView = imageIcon.getImage();
                                BufferedImage bi = new BufferedImage(
                                        imageIcon.getIconWidth(),
                                        imageIcon.getIconHeight(),
                                        BufferedImage.TYPE_INT_ARGB
                                );
                                imageIcon.paintIcon(null, bi.getGraphics(), 0, 0);
                                SwingFXUtils.toFXImage(bi, null);
                                if (file.isFile()) {
                                    setText(friend);
                                    setGraphic(new ImageView(SwingFXUtils.toFXImage(bi, null)));
                                } else if (file.isDirectory()) {
                                    setText(friend);
                                    setGraphic(new ImageView(SwingFXUtils.toFXImage(bi, null)));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showOnlineDirectory(String[] onlineFileList, ListView<String> fileList, StringBuilder currentPath) throws IOException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fileList.getItems().clear();
                if (currentPath.toString().split(Matcher.quoteReplacement(File.separator)).length > 2) {
                    fileList.getItems().add("BACK");
                }
                for (int i = 1; i < onlineFileList.length; i++) {
                    fileList.getItems().add(onlineFileList[i]);
                }
            }
        });
        //создаем Temp директорию
        Path tempPath = Files.createTempDirectory(Path.of("c:\\temp\\"), "");
        if (!tempPath.toFile().exists()) {
            tempPath.toFile().createNewFile();
        }
        tempPath.toFile().deleteOnExit();
        fileList.setCellFactory(l -> new ListCell<String>() {
            @Override
            public void updateItem(String friend, boolean empty) {
                super.updateItem(friend, empty);
                try {
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else if ("BACK".equals(friend)) {
                        setText(friend);
                        setGraphic(new ImageView(new Image("/images/arrow.png")));
                    } else {
                        String[] friendPath = friend.split(":");
                        Path tempElement = null;
                        if ("d".equals(friendPath[0])) {
                            tempElement = Files.createTempDirectory(tempPath, friendPath[1].replace("\n", ""));
                        }
                        if ("f".equals(friendPath[0])) {
                            tempElement = Files.createTempFile(tempPath, "", (friendPath[1].replace("\n", "")));
                        }

                        ImageIcon imageIcon = null;
                        if (tempElement != null) {
                            imageIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(tempElement.toFile());
                        }
                        if (imageIcon != null) {
                            java.awt.Image imageIconView = imageIcon.getImage();
                            BufferedImage bi = new BufferedImage(
                                    imageIcon.getIconWidth(),
                                    imageIcon.getIconHeight(),
                                    BufferedImage.TYPE_INT_ARGB
                            );
                            imageIcon.paintIcon(null, bi.getGraphics(), 0, 0);
                            SwingFXUtils.toFXImage(bi, null);
                            if (tempElement.toFile().isFile()) {
                                setText(friendPath[1]);
                                setGraphic(new ImageView(SwingFXUtils.toFXImage(bi, null)));
                            } else if (tempElement.toFile().isDirectory()) {
                                setText(friendPath[1]);
                                setGraphic(new ImageView(SwingFXUtils.toFXImage(bi, null)));
                            }
                        }
                        tempElement.toFile().delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    showLocalDirectory(directory, fileList);
                }
            }
        });
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

    public static String[] receiveFileList(StringBuilder path, DataOutputStream out,
                                           ReadableByteChannel readableByteChannel, ByteBuffer byteBuffer) {
        try {
            out.write(("/ls" + DELIMETER + path).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryStringListener(readableByteChannel, byteBuffer);
    }

    public static String[] queryStringListener(ReadableByteChannel readableByteChannel, ByteBuffer byteBuffer) {
        int readNumberBytes = 0;
        try {
            readNumberBytes = readableByteChannel.read(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] queryAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes)).split(DELIMETER);
        byteBuffer.clear();
        return queryAnswer;
    }

    public static void changeCurrentPath(StringBuilder currentPath, String newPath, TextField pathView) {
        currentPath.delete(0, currentPath.length());
        currentPath.append(newPath);
        pathView.setText(currentPath.toString());
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
