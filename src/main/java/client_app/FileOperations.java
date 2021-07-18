package client_app;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import server_app.TelnetUser;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;

public class FileOperations {

    public static void showDirectory(StringBuilder directory, ListView<String> fileList) {
        File fileDirectory = new File(directory.toString());
        String[] tokens = fileDirectory.list();
        fileList.getItems().clear();
        if (directory.toString().split(Matcher.quoteReplacement(File.separator)).length >= 2) {
            fileList.getItems().add("BACK");
        }
        for (int i = 0; i < tokens.length; i++) {
            fileList.getItems().add(tokens[i]);
        }
        fileList.setCellFactory(l -> new ListCell<String>() {
            @Override
            public void updateItem(String friend, boolean empty) {
                super.updateItem(friend, empty);
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
            showDirectory(currentPath, fileList);
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
