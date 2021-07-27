package client_app.Resources;

import client_app.Main_Functional.NetworkManager;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;

public class WorkPanel {
    private final StringBuilder currentPath;  //текущий локальный путь
    private ObservableList<String> markedFileList; //список выделенных элементов
    private final MultipleSelectionModel<String> markedElementsListener; //список выделенных строк
    private boolean isOnline;
    private ListView<String> listView;
    ChoiceBox<String> sortBox;
    TextField pathView;
    private NetworkManager networkManager;
    Path tempPath;
    int sortType = 1;
    float totalSpace;
    private String root;

    public float getTotalSpace() {
        return totalSpace;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public ListView<String> getListView() {
        return listView;
    }

    public ObservableList<String> getMarkedFileList() {
        return markedFileList;
    }

    private void setLocalPathView() {
        pathView.setText(currentPath.toString());
    }

    private void setOnlinePathView() {
        pathView.setText("cloud:" + currentPath);
    }

    public StringBuilder getCurrentPath() {
        return currentPath;
    }

    public void setOnline(boolean online) {
        isOnline = online;
        setCurrentPath("");
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void showDirectory() {
        if (!isOnline) {
            showLocalDirectory();
        } else {
            showOnlineDirectory();
        }
    }

    public WorkPanel(String path, ListView<String> listView, TextField pathView, ChoiceBox<String> sortBox) {
        this.root = path;
        this.isOnline = false;
        this.pathView = pathView;
        this.listView = listView;
        this.sortBox = sortBox;
        this.currentPath = new StringBuilder();
        currentPath.append(path);
        this.markedFileList = FXCollections.emptyObservableList();
        this.markedElementsListener = listView.getSelectionModel();
        this.markedElementsListener.setSelectionMode(SelectionMode.MULTIPLE);
        try {
            totalSpace = Files.getFileStore(Path.of(root)).getUnallocatedSpace();
            tempPath = Files.createTempDirectory(Path.of(path + File.separator + "temp"), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        sortBox.getItems().add("Прямая по имени");  //Тип 1
        sortBox.getItems().add("Обратная по имени"); //Тип 2
        sortBox.getItems().add("Прямая по типу объекта"); //Тип 3
        sortBox.getItems().add("Обратная по типу объекта"); //Тип 4
        sortBox.setValue("Прямая по имени");

        sortBox.setOnAction((event -> {
            if ("Прямая по имени".equals(sortBox.getSelectionModel().getSelectedItem())) {
                setSortType(1);
            }
            if ("Обратная по имени".equals(sortBox.getSelectionModel().getSelectedItem())) {
                setSortType(2);
            }
            if ("Прямая по типу объекта".equals(sortBox.getSelectionModel().getSelectedItem())) {
                setSortType(3);
            }
            if ("Обратная по типу объекта".equals(sortBox.getSelectionModel().getSelectedItem())) {
                setSortType(4);
            }
        }));
    }

    private void listViewInitialise(String[] tokens) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    listView.getItems().clear();
                    if (currentPath.toString().split(Matcher.quoteReplacement(File.separator)).length >= 2) {
                        listView.getItems().add("BACK");
                    }
                    for (int i = 0; i < tokens.length; i++) {
                        if (!tokens[i].isEmpty()) {
                            listView.getItems().add(tokens[i]);
                        }
                    }
                } catch (NullPointerException e) {
                    System.out.println("Переключение на online список");
                }
            }
        });
    }

    public float getAvailableSpace() {
        try {
            if (!isOnline) {
                totalSpace = Files.getFileStore(Path.of(root)).getUnallocatedSpace();
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalSpace;
    }

    private void directorySort(String[] fileArray) {
        if (!isOnline) {
            if (sortType == 2) {
                Arrays.sort(fileArray, Collections.reverseOrder());
            }
            if (sortType == 3) {
                sortByFolders(fileArray);
            }
            if (sortType == 4) {
                Arrays.sort(fileArray, Collections.reverseOrder());
                sortByFolders(fileArray);
            }
        }
    }

    private void setSortType(int i) {
        if (!isOnline) {
            sortType = i;
            showDirectory();
        }
        if (isOnline) {
            int answer = networkManager.sortRemoteObjects(i);
            if (answer > 0) {
                showDirectory();
            }
        }
    }

    private void sortByFolders(String[] fileArray) {
        ArrayList<String> folders = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();
        File file;
        for (String element : fileArray) {
            file = new File(getCurrentPath() + element);
            if (file.isFile()) {
                files.add(element);
            } else {
                folders.add(element);
            }
        }
        if (sortType == 3) {
            folders.addAll(files);
            for (int i = 0; i < fileArray.length; i++) {
                fileArray[i] = folders.get(i);
            }
        } else {
            files.addAll(folders);
            for (int i = 0; i < fileArray.length; i++) {
                fileArray[i] = files.get(i);
            }
        }
    }

    public String fileLengthView() {
        File file;
        float sumLength = 0;
        int fileChoice = 0;
        for (String elements : getSelectedFiles()) {
            file = new File(getCurrentPath() + File.separator + elements);
            if (file.exists() && file.isFile()) {
                sumLength = sumLength + file.length();
                fileChoice++;
            }
        }
        float availableSpace = getAvailableSpace();
        return "Выделено объектов " + fileChoice + "." +
                " Размер файла(ов) на диске " + spaceToString(sumLength) + "."
                + " Доступно " + spaceToString(availableSpace);
    }

    public String spaceToString(float digit) {
        if (digit < 1000) {
            return String.format("%.0f byte", digit);
        } else if (digit < 1000000) {
            return String.format("%.0f kb", digit / 1000);
        } else if (digit < 1000000000) {
            return String.format("%.2f mb", digit / 1000000);
        } else if (digit < 1000000000000L) {
            return String.format("%.2f Gb", digit / 1000000000L);
        }
        return digit + "bytes";
    }

    private void showLocalDirectory() {
        try {
            File fileDirectory = new File(currentPath.toString());
            String[] tokens = fileDirectory.list();
            directorySort(tokens);
            listViewInitialise(tokens);
            setLocalPathView();
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

    private void showOnlineDirectory(String path) {
        setCurrentPath(path);
        showOnlineDirectory();
    }

    private void showOnlineDirectory() {
        try {
            String[] serverAnswer = networkManager.receiveFileList(currentPath);
            setOnlinePathView();
            listViewInitialise(serverAnswer);
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
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void connectToServer(NetworkManager networkManager) {
        this.networkManager = networkManager;
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
            if ("BACK".equals(markedElementsListener.getSelectedItems().get(0))) {
                String[] tokens = currentPath.toString().split(Matcher.quoteReplacement(File.separator));
                showOnlineDirectory(currentPath.delete(
                        currentPath.length() - tokens[tokens.length - 1].length() - 1,
                        currentPath.length()).toString());
            } else {
                String markedElement = markedElementsListener.getSelectedItems().get(0);
                if (!markedElement.contains("f:")) {
                    String newPath = currentPath + File.separator +
                            markedElement.replaceAll(".:", "");
                    if (networkManager.enterToDirectory(newPath) > 0) {
                        showOnlineDirectory(newPath);
                    } else {
                        System.out.println(String.format("Директория [%s] не найдена!", newPath));
                    }
                }
            }
        }
    }

    public boolean isObjectExistInList(String fileName) {
        for (String elements : listView.getItems()) {
            if ((elements.replaceAll(".:", "")).equals(fileName)) {
                return true;
            }
        }
        return false;
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

    public void addElementsToWorkPanel() {
        markedFileList = markedElementsListener.getSelectedItems();
    }

    public ObservableList<String> getSelectedFiles() {
        return markedElementsListener.getSelectedItems();
    }

    public Path getPathByElement(String element) {
        return Path.of(currentPath + File.separator + element);
    }

    public void takePropertyFrom(WorkPanel sourcePanel) {
        isOnline = sourcePanel.isOnline();
        setCurrentPath(sourcePanel);
    }

}
