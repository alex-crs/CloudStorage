package client_app;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;
import java.util.regex.Matcher;

public class MainWindowController implements Initializable {
    @FXML
    ListView<String> leftList;

    @FXML
    ListView<String> rightList;

    StringBuilder rightPath = new StringBuilder();
    StringBuilder leftPath = new StringBuilder();
    String leftMarkedFile;
    String rightMarkedFile;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        leftPath.append("c:\\");
        rightPath.append("k:\\");
        showDirectory(rightPath, rightList);
        showDirectory(leftPath, leftList);
    }

    public void showDirectory(StringBuilder directory, ListView<String> fileList) {
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

    public void leftEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            leftMarkedFile = leftList.getSelectionModel().getSelectedItem();
        }
        if (mouseEvent.getClickCount() == 2) {
            String currentElement = leftList.getSelectionModel().getSelectedItem();
            eventAction(currentElement, leftPath, leftList);
        }
    }

    public void rightEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            rightMarkedFile = rightList.getSelectionModel().getSelectedItem();
        }
        if (mouseEvent.getClickCount() == 2) {
            String currentElement = rightList.getSelectionModel().getSelectedItem();
            eventAction(currentElement, rightPath, rightList);
        }
    }

    private void eventAction(String element, StringBuilder currentPath, ListView<String> fileList) {
        String[] tokens = currentPath.toString().split(Matcher.quoteReplacement(File.separator));
        if ("BACK".equals(element)) {
            currentPath.delete((currentPath.length() - tokens[tokens.length - 1].length() - 1), currentPath.length());
            showDirectory(currentPath, fileList);
        } else {
            if (new File(currentPath.toString() + File.separator + element).isDirectory()) {
                currentPath.append(element + File.separator);
                showDirectory(currentPath, fileList);
            }
        }
    }

    public void copyAction() throws IOException {
        try {
            Path fromPath = Path.of(leftPath + File.separator + leftMarkedFile);
            Path toPath = Path.of(rightPath + File.separator + leftMarkedFile);
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
            showDirectory(rightPath, rightList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
        }
    }


}
