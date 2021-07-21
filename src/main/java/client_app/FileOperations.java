package client_app;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.regex.Matcher;

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
                        } else {
                            File file = new File(directory + File.separator + friend);
                            ImageIcon imageIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file);
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
//        fileList.setCellFactory(null);
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

                        ImageIcon imageIcon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(tempElement.toFile());
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
    public static void copy(Path source, Path target, StringBuilder currentPath, ListView<String> fileList) throws IOException {
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
            showLocalDirectory(currentPath, fileList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

    public static void delete(StringBuilder path, String fileName, ListView<String> fileList) throws IOException {
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

    }

}
