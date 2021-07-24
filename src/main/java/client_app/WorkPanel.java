package client_app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;

public class WorkPanel {
    private StringBuilder currentPath;
    ObservableList<String> markedFileList;
    MultipleSelectionModel<String> markedElementsListener;
    boolean isListOnline;
    ListView<String> listView;
    TextField pathView;


    public WorkPanel(String path, ListView<String> listView, TextField pathView) {
        this.isListOnline = false;
        this.pathView = pathView;
        this.listView = listView;
        this.currentPath = new StringBuilder();
        currentPath.append(path);
        this.markedFileList = FXCollections.emptyObservableList();
        this.markedElementsListener = listView.getSelectionModel();
        this.markedElementsListener.setSelectionMode(SelectionMode.MULTIPLE);
    }

    public ObservableList<String> getMarkedFileList() {
        return markedFileList;
    }

    public void setPathView() {
        pathView.setText(currentPath.toString());
    }

    public StringBuilder getCurrentPath() {
        return currentPath;
    }

    public void showDirectory() {
        try {
            File fileDirectory = new File(currentPath.toString());
            String[] tokens = fileDirectory.list();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    listView.getItems().clear();
                    if (currentPath.toString().split(Matcher.quoteReplacement(File.separator)).length >= 2) {
                        listView.getItems().add("BACK");
                    }
                    for (int i = 0; i < tokens.length; i++) {
                        listView.getItems().add(tokens[i]);
                    }
                    setPathView();
                }
            });
            listView.setCellFactory(l -> new ListCell<String>() {
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
                            File file = new File(currentPath.toString() + File.separator + friend);
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

    /*Данный метод строит путь как вперед так и назад, если дважды кликнуть по кнопке BACK, то данный метод
     * возвращает в предыдущую директорию, если было передано имя директории, то метод строит путь дальше.
     * Также в данном методе производится проверка на то, является ли файлом переданное имя, если да, то файл
     * запускается в программе по умолчанию*/
    public void treeMovement() {
        String currentElement = listView.getSelectionModel().getSelectedItem();
        String[] tokens = currentPath.toString().split(Matcher.quoteReplacement(File.separator));
        if ("BACK".equals(currentElement)) {
            currentPath.delete((currentPath.length() - tokens[tokens.length - 1].length() - 1), currentPath.length());
            showDirectory();
        } else {
            File file = new File(currentPath + File.separator + currentElement);
            if (file.isDirectory()) {
                currentPath.append(currentElement + File.separator);
                showDirectory();
            } else {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void clearSelectionFiles() {
        markedElementsListener.clearSelection();
    }

    public void getSelectedFiles() {
        markedFileList = markedElementsListener.getSelectedItems();
    }

    public Path getPathByElement(String element) {
        return Path.of(currentPath + File.separator + element);
    }

}
