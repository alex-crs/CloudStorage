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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

import static client_app.FileOperations.*;
import static client_app.FileOperations.showOnlineDirectory;
import static client_app.RegistrationWindowController.DELIMETER;

public class WorkPanel {
    private StringBuilder currentPath;
    private ObservableList<String> markedFileList;
    private MultipleSelectionModel<String> markedElementsListener;
    private boolean isOnline;
    private ListView<String> listView;
    TextField pathView;
    NetworkOperator networkOperator;
    Path tempPath;


    public WorkPanel(String path, ListView<String> listView, TextField pathView) {
        this.isOnline = false;
        this.pathView = pathView;
        this.listView = listView;
        this.currentPath = new StringBuilder();
        currentPath.append(path);
        this.markedFileList = FXCollections.emptyObservableList();
        this.markedElementsListener = listView.getSelectionModel();
        this.markedElementsListener.setSelectionMode(SelectionMode.MULTIPLE);
        try {
            tempPath = Files.createTempDirectory(Path.of("c:\\" + File.separator + "temp"), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ListView<String> getListView() {
        return listView;
    }

    public ObservableList<String> getMarkedFileList() {
        return markedFileList;
    }

    private void setPathView() {
        pathView.setText(currentPath.toString());
    }

    public StringBuilder getCurrentPath() {
        return currentPath;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void showDirectory() {
        if (!isOnline) {
            showLocalDirectory();
        } else {
            showOnlineDirectory();
        }
    }

    private void showLocalDirectory() {
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

    private void showOnlineDirectory() {
        try {

            String[] serverAnswer = networkOperator.receiveFileList(currentPath);
            setCurrentPath(serverAnswer[0]);
            setPathView();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    listView.getItems().clear();
                    if (currentPath.toString().split(Matcher.quoteReplacement(File.separator)).length > 2) {
                        listView.getItems().add("BACK");
                    }
                    for (int index = 1; index < serverAnswer.length; index++) {
                        listView.getItems().add(serverAnswer[index]);
                    }
                }
            });
            if (!tempPath.toFile().exists()) {
                tempPath.toFile().createNewFile();
            }
            tempPath.toFile().deleteOnExit();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void connectToServer(NetworkOperator networkOperator) {
        this.networkOperator = networkOperator;
    }

    /*Данный метод строит путь как вперед так и назад, если дважды кликнуть по кнопке BACK, то данный метод
     * возвращает в предыдущую директорию, если было передано имя директории, то метод строит путь дальше.
     * Также в данном методе производится проверка на то, является ли файлом переданное имя, если да, то файл
     * запускается в программе по умолчанию*/
    public void treeMovement() {
        if (!isOnline) {
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
        } else {
//            try {
                if ("BACK".equals(markedElementsListener.getSelectedItems().get(0))) {
                    networkOperator.outFromDirectory();
                    showDirectory();
//                    String[] tokens = currentPath.toString().split(Matcher.quoteReplacement(File.separator));
//                    currentPath.delete((currentPath.length() - tokens[tokens.length - 1].length() - 1), currentPath.length());
//                    networkOperator.out.write(("/cd" + DELIMETER + currentPath).getBytes());
//                    String[] serverAnswer = queryStringListener(networkOperator);
//                    changeCurrentPath(currentPath, serverAnswer[0], pathView);
//                    showOnlineDirectory(serverAnswer, renewableFileList, currentPath);
                } else {
                    networkOperator.enterToDirectory(markedElementsListener.getSelectedItems().get(0).replaceAll(".:",""));
showDirectory();
//                    out.write(("/cd" + DELIMETER + currentPath + File.separator + element.replaceAll(".:", "")).getBytes());
//                    String[] serverAnswer = queryStringListener(rbc, byteBuffer);
//                    changeCurrentPath(currentPath, serverAnswer[0], pathView);
//                    showOnlineDirectory(serverAnswer, renewableFileList, currentPath);
                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public void setCurrentPath(WorkPanel sourcePanel) {
        currentPath.delete(0, currentPath.length());
        currentPath.append(sourcePanel.getCurrentPath());
        showDirectory();
    }

    public void setCurrentPath(String newPath) {
        currentPath.delete(0, currentPath.length());
        currentPath.append(newPath);
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
