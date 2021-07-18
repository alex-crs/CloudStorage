package client_app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import static client_app.Action.*;
import static client_app.FileOperations.*;

public class MainWindowController implements Initializable {

    //взаимодействие с javafx
    //----------------------------------------------------
    @FXML
    ListView<String> leftList;

    @FXML
    ListView<String> rightList;

    @FXML
    Button copy;

    @FXML
    Button delete;

    @FXML
    Button rename;

    @FXML
    Button newButton;

    @FXML
    Button move;

    @FXML
    TextField leftPathView;

    @FXML
    TextField rightPathView;

    Stage stage;
    //----------------------------------------------------

    //...

    //сетевое взаимодействие
    //----------------------------------------------------
    public static final String ADDRESS = "127.0.0.1";
    public static final int PORT = 5679;
    Socket socket;
    DataOutputStream out;
    DataInputStream in;
    ReadableByteChannel rbc;
    ByteBuffer byteBuffer = ByteBuffer.allocate(8 * 1024);
    private ExecutorService threadManager;
    //----------------------------------------------------

    //...

    //управление ListView и текущими путями
    //----------------------------------------------------
    StringBuilder rightPath = new StringBuilder();
    StringBuilder leftPath = new StringBuilder();
    ObservableList<String> leftFiles = FXCollections.emptyObservableList();
    ObservableList<String> rightFiles = FXCollections.emptyObservableList();
    MultipleSelectionModel<String> leftMarkedFiles;
    MultipleSelectionModel<String> rightMarkedFiles;
    //----------------------------------------------------

    //...

    //авторизация и статусы подключения
    //----------------------------------------------------
    private StringBuilder nickname = new StringBuilder();
    private boolean isAuthorized;

    public void setAuthorized(boolean authorized) {
        this.isAuthorized = authorized;

        if (!isAuthorized) {
        } else {
        }
    }
    //----------------------------------------------------

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        leftPath.append("c:\\");
        rightPath.append("c:\\");
        showDirectory(rightPath, rightList);
        showDirectory(leftPath, leftList);
        leftPathView.setText(leftPath.toString());
        rightPathView.setText(rightPath.toString());
        rightMarkedFiles = rightList.getSelectionModel();
        rightMarkedFiles.setSelectionMode(SelectionMode.MULTIPLE);
        leftMarkedFiles = leftList.getSelectionModel();
        leftMarkedFiles.setSelectionMode(SelectionMode.MULTIPLE);
        copy.setFocusTraversable(false);
        delete.setFocusTraversable(false);
        rename.setFocusTraversable(false);
        newButton.setFocusTraversable(false);
        move.setFocusTraversable(false);
        leftPathView.setFocusTraversable(false);
        rightPathView.setFocusTraversable(false);
    }

    public void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            rbc = Channels.newChannel(in);
            stage = (Stage) leftList.getScene().getWindow();
            threadManager = Executors.newFixedThreadPool(2);
            out.write("auth".getBytes());
            Thread netClientThread = new Thread(() -> {
                try {
                    while (true) {
                        String[] serverAnswer = queryFileInfo();
                        if (!serverAnswer[0].isEmpty() && "/end".equals(serverAnswer[0])) {
                            break;
                        }
                        if (!serverAnswer[0].isEmpty() && "/auth-ok".equals(serverAnswer[0])) {
                            setAuthorized(true);
                            nickname.append(serverAnswer[1].replace("\n", ""));
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    stage.setTitle("Pixel Cloud Explorer. User: " + nickname);

                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            threadManager.execute(netClientThread);
        } catch (IOException e) {
            threadManager.shutdown();
            e.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------------
    /*управляют активными окнами. В случае выделения одного или нескольких файлов
     * добавляют их в лист для дальнейших операций. При этом если файлы выбираются в одном окне
     * в другом список очищается, таким образом мы работаем только с одним активным окном*/
    public void leftEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            leftFiles = leftMarkedFiles.getSelectedItems();
            rightMarkedFiles.clearSelection();
        }
        if (mouseEvent.getClickCount() == 2) {
            String currentElement = leftList.getSelectionModel().getSelectedItem();
            eventAction(currentElement, leftPath, leftList, leftPathView);
        }
    }

    public void rightEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            rightFiles = rightMarkedFiles.getSelectedItems();
            leftMarkedFiles.clearSelection();
        }
        if (mouseEvent.getClickCount() == 2) {
            String currentElement = rightList.getSelectionModel().getSelectedItem();
            eventAction(currentElement, rightPath, rightList, rightPathView);
        }
    }
    //--------------------------------------------------------------------------------


    /*Данный метод строит путь как вперед так и назад, если дважды кликнуть по кнопке BACK, то данный метод
     * возвращает в предыдущую директорию, если было передано имя директории, то метод строит путь дальше.
     * Также в данном методе производится проверка на то, является ли файлом переданное имя, если да, то файл
     * запускается в программе по умолчанию*/
    private void eventAction(String element, StringBuilder currentPath, ListView<String> renewableFileList, TextField pathView) {
        String[] tokens = currentPath.toString().split(Matcher.quoteReplacement(File.separator));
        if ("BACK".equals(element)) {
            currentPath.delete((currentPath.length() - tokens[tokens.length - 1].length() - 1), currentPath.length());
            showDirectory(currentPath, renewableFileList);
            pathView.setText(currentPath.toString());
        } else {
            File file = new File(currentPath.toString() + File.separator + element);
            if (file.isDirectory()) {
                currentPath.append(element + File.separator);
                showDirectory(currentPath, renewableFileList);
                pathView.setText(currentPath.toString());
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

    /*Проверяем текущий выделенный файл в окне (если например выделен файл в правом окне, то копируем в левый
     * если выделен в левом окне, то копируем в правое окно
     * Если левое или правое окно подключены к облачному хранилищу, то вместо команды copy(), запускается метод upload*/
    public void copyAction() throws IOException {
        if (leftFiles.size() > 0) { //если выделенные файлы слева
            prepareAndCopy(leftPath, rightPath, leftFiles, rightList);
        }
        if (rightFiles.size() > 0) { //если выделенные файлы справа
            prepareAndCopy(rightPath, leftPath, rightFiles, leftList);
        }
    }

    /*позволяет построить пути для копирования и проверить существует ли файл, если файл существует,
     * появится окно с запросом о замене файла*/
    public void prepareAndCopy(StringBuilder sourcePath, StringBuilder targetPath,
                               ObservableList<String> files, ListView<String> renewableFileList) throws IOException {
        for (String element : files) {
            Path source = Path.of(sourcePath + File.separator + element);
            Path target = Path.of(targetPath + File.separator + element);
            if (target.toFile().exists()) {
                QuestionWindowStage qws = new QuestionWindowStage(source, target, element,
                        targetPath, renewableFileList, COPY);
                qws.setResizable(false);
                qws.show();
            } else {
                copy(source, target, targetPath, renewableFileList);
            }
        }
    }

    public void deleteAction() throws IOException {
        if (leftFiles.size() > 0) { //если выделенные файлы слева
            prepareAndDelete(leftPath, leftFiles, leftList);
        }
        if (rightFiles.size() > 0) { //если выделенные файлы справа
            prepareAndDelete(rightPath, rightFiles, rightList);
        }
    }

    private void prepareAndDelete(StringBuilder path, ObservableList<String> files,
                                  ListView<String> renewableFileList) throws IOException {
        Iterator<String> iterator = files.iterator();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            delete(path, fileName, renewableFileList);
        }
        updateAllFilesLists();
    }


    public void renameAction() {
        if (leftFiles.size() > 0) { //если выделенные файлы слева
            prepareAndRename(leftFiles.get(0), leftPath, leftList, rightPath, rightList);
        }
        if (rightFiles.size() > 0) { //если выделенные файлы справа
            prepareAndRename(rightFiles.get(0), rightPath, rightList, leftPath, leftList);
        }
    }

    //переименовывает файл в отдельном окне
    private void prepareAndRename(String fileName, StringBuilder renamePath, ListView<String> renameList,
                                  StringBuilder secondPath, ListView<String> secondList) {
        RenameWindowStage rws = new RenameWindowStage(fileName, renamePath, renameList, secondPath, secondList);
        rws.setMinWidth(410);
        rws.setMinHeight(25);
        rws.setResizable(false);
        rws.show();
    }

    public void makeDirAction() {
        if (leftList.isFocused()) { //если выделенное окно слева
            prepareAndMakeDir(leftPath, leftList, rightPath, rightList);
        }
        if (rightList.isFocused()) { //если выделенное окно слева
            prepareAndMakeDir(rightPath, rightList, leftPath, leftList);
        }
    }

    //создание файла/директории производится по первому пути
    private void prepareAndMakeDir(StringBuilder firstPath, ListView<String> firstList,
                                   StringBuilder secondPath, ListView<String> secondList) {
        MakeFileOrDirStage mds = new MakeFileOrDirStage(firstPath, firstList, secondPath, secondList);
        mds.setMinWidth(480);
        mds.setMinHeight(25);
        mds.setResizable(false);
        mds.show();
    }

    //перемещает выбранные файлы
    public void moveAction() throws IOException {
        if (leftFiles.size() > 0) { //если выделенные файлы слева
            prepareAndCopy(leftPath, rightPath, leftFiles, rightList);
            prepareAndDelete(leftPath, leftFiles, leftList);
        }
        if (rightFiles.size() > 0) { //если выделенные файлы справа
            prepareAndCopy(rightPath, leftPath, rightFiles, leftList);
            prepareAndDelete(rightPath, rightFiles, rightList);
        }
    }

    public void updateAllFilesLists() {
        showDirectory(leftPath, leftList);
        showDirectory(rightPath, rightList);
        leftPathView.setText(leftPath.toString());
        rightPathView.setText(rightPath.toString());
    }

    public String[] queryFileInfo() throws IOException {
        int readNumberBytes = rbc.read(byteBuffer);
        return new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes)).split("  ");
    }


}

/*Обнаруженныне косяки:
 * 1. при вставке пути вместо имени файла (например C:\path\) выскакивает исключение
 * 2. */
