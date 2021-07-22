package client_app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
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
    Button sourceTarget;

    @FXML
    Button authEnterButton;

    @FXML
    Button authCancelButton;

    @FXML
    Label authInfo;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

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
    public String DELIMETER = ";";
    private String AUTH_COMMAND = "/auth";
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
    public static boolean isRightListOnline;
    public static boolean isLeftListOnline;
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
        showLocalDirectory(rightPath, rightList);
        showLocalDirectory(leftPath, leftList);
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
        sourceTarget.setFocusTraversable(false);
        authInfo.setVisible(false);
        loginField.setVisible(false);
        passwordField.setVisible(false);
        authEnterButton.setVisible(false);
        authCancelButton.setVisible(false);
    }

    public void showAuthFields() {
        rightPathView.setVisible(false);
        rightList.setVisible(false);
        authInfo.setVisible(true);
        loginField.setVisible(true);
        passwordField.setVisible(true);
        authEnterButton.setVisible(true);
        authCancelButton.setVisible(true);
    }

    public void hideAuthFields() {
        rightList.setVisible(true);
        rightPathView.setVisible(true);
        authInfo.setVisible(false);
        loginField.setVisible(false);
        passwordField.setVisible(false);
        authEnterButton.setVisible(false);
        authCancelButton.setVisible(false);
    }

    public void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            rbc = Channels.newChannel(in);
            stage = (Stage) leftList.getScene().getWindow();
            threadManager = Executors.newFixedThreadPool(5);
        } catch (IOException e) {
//            threadManager.shutdown();
            e.printStackTrace();
        }
    }

    public void authorisation() {
        if (socket == null) {
            connect();
        }
        try {
            out.write(("/auth" + DELIMETER + loginField.getText() + DELIMETER + passwordField.getText()).getBytes());
            Thread netClientThread = new Thread(() -> {
                String[] serverAnswer = queryStringListener();
                if (!serverAnswer[0].isEmpty() && "/auth-ok".equals(serverAnswer[0])) {
                    setAuthorized(true);
                    nickname.append(serverAnswer[1].replace("\n", ""));
                    hideAuthFields();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.setTitle("Pixel Cloud Explorer. User: " + nickname);
                        }
                    });
                    byteBuffer.clear();
                    String[] queryAnswer = receiveFileList(out);
                    try {
                        rightList.setCellFactory(null);
                        isRightListOnline = true;
                        changeCurrentPath(rightPath, queryAnswer[0], rightPathView);
                        showOnlineDirectory(queryAnswer, rightList, rightPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (!serverAnswer[0].isEmpty() && "/auth-no".equals(serverAnswer[0].replace("\n", ""))) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            authInfo.setText("Wrong login/password, try again");
                        }
                    });
//                        }
                }
            });
            threadManager.execute(netClientThread);
            byteBuffer.clear();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeCurrentPath(StringBuilder currentPath, String newPath, TextField pathView) {
        currentPath.delete(0, currentPath.length());
        currentPath.append(newPath);
        pathView.setText(currentPath.toString());
    }

    public void disconnect() {
        hideAuthFields();
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
            if (!isLeftListOnline) {
                eventAction(currentElement, leftPath, leftList, leftPathView);
            } else {
                eventOnlineAction(currentElement, leftPath, leftList, leftPathView);
            }
        }
    }

    public void rightEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            rightFiles = rightMarkedFiles.getSelectedItems();
            leftMarkedFiles.clearSelection();
        }
        if (mouseEvent.getClickCount() == 2) {
            String currentElement = rightList.getSelectionModel().getSelectedItem();
            if (!isRightListOnline) {
                eventAction(currentElement, rightPath, rightList, rightPathView);
            } else {
                eventOnlineAction(currentElement, rightPath, rightList, rightPathView);
            }
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
            showLocalDirectory(currentPath, renewableFileList);
            pathView.setText(currentPath.toString());
        } else {
            File file = new File(currentPath.toString() + File.separator + element);
            if (file.isDirectory()) {
                currentPath.append(element + File.separator);
                showLocalDirectory(currentPath, renewableFileList);
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

    private void eventOnlineAction(String element, StringBuilder currentPath, ListView<String> renewableFileList, TextField pathView) {
        Thread eventAction = new Thread(() -> {
            try {
                if ("BACK".equals(element)) {
                    String[] tokens = currentPath.toString().split(Matcher.quoteReplacement(File.separator));
                    currentPath.delete((currentPath.length() - tokens[tokens.length - 1].length() - 1), currentPath.length());
                    out.write(("/cd" + DELIMETER + currentPath).getBytes());
                    String[] serverAnswer = queryStringListener();
                    changeCurrentPath(currentPath, serverAnswer[0], pathView);
                    showOnlineDirectory(serverAnswer, renewableFileList, currentPath);
                } else {
                    out.write(("/cd" + DELIMETER + currentPath + File.separator + element.replaceAll(".:", "")).getBytes());
                    String[] serverAnswer = queryStringListener();
                    changeCurrentPath(currentPath, serverAnswer[0], pathView);
                    showOnlineDirectory(serverAnswer, renewableFileList, currentPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        threadManager.execute(eventAction);
    }

    /*Проверяем текущий выделенный файл в окне (если например выделен файл в правом окне, то копируем в левый
     * если выделен в левом окне, то копируем в правое окно
     * Если левое или правое окно подключены к облачному хранилищу, то вместо команды copy(), запускается метод upload*/
    public void copyAction() throws IOException {
        if (leftFiles.size() > 0) { //если выделенные файлы слева
            if (!isLeftListOnline && !isRightListOnline) {
                prepareAndCopy(leftPath, rightPath, leftFiles, rightList);
            }
            if (!isLeftListOnline && isRightListOnline) {
                prepareAndUpload(leftList.getSelectionModel().getSelectedItem(), leftPath); //дописать логику множественного копирования
            }
            if (isLeftListOnline && !isRightListOnline) {
                prepareAndDownload(leftList.getSelectionModel().getSelectedItem(), leftPath);
            }
            if (isLeftListOnline && isRightListOnline) {

            }
        }
        if (rightFiles.size() > 0) { //если выделенные файлы справа
            if (!isRightListOnline && !isLeftListOnline) {
                prepareAndCopy(rightPath, leftPath, rightFiles, leftList);
            }
            if (isRightListOnline && !isLeftListOnline) {
                prepareAndDownload(rightList.getSelectionModel().getSelectedItem(), rightPath);
            }
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

    public void prepareAndUpload(String fileName, StringBuilder fromPath) {
        Thread uploadThread = new Thread(() -> {
            try {
                File file = new File(fromPath + File.separator + fileName);
                if (file.isFile()) {
                    out.write(("/upload" + DELIMETER + "f" + DELIMETER + fileName + DELIMETER
                            + file.length()).getBytes());
                    while (true) {
                        String[] serverAnswer = queryStringListener();
                        if ("/upload-ok".equals(serverAnswer[0].replace("\n", ""))) {
                            break;
                        } else if ("nex".equals(serverAnswer[0])) {
                            System.out.println("Файл уже существует заменить?"); //отработать этот модуль
                            throw new FileAlreadyExistsException(fileName);
                        }
                    }
                } else {
                    out.write(("/upload" + DELIMETER + "d" + DELIMETER + fileName + DELIMETER
                            + file.length()).getBytes());
                }
                if (file.isFile() && file.length() != 0) {
                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
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
                    String[] serverAnswer = queryStringListener();
                    if ("/status-ok".equals(serverAnswer[0].replace("\n", ""))) {
                        updateAllFilesLists();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        uploadThread.interrupt();
        threadManager.execute(uploadThread);
    }

    private void prepareAndDownload(String fileName, StringBuilder fromPath) {
        Thread downloadThread = new Thread(() -> {
            File file = new File(leftPath + File.separator + fileName.replaceAll(".:", ""));
            long downloadFileLength = 0;
            try {
                out.write(("/download" + DELIMETER + fromPath + File.separator + fileName.replaceAll(".:", "")).getBytes());
                String[] serverAnswer = queryStringListener();
                if (!file.exists()) {
                    file.createNewFile();
                }
                while (true) {
                    if ("/download-ok".equals(serverAnswer[0])) {
                        downloadFileLength = Long.parseLong(serverAnswer[1].replace("\n", ""));
                        break;
                    } else if ("nex".equals(serverAnswer[0])) {
                        System.out.println("File not found!"); //отработать этот модуль
                        throw new FileNotFoundException();
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
        });
        downloadThread.interrupt();
        threadManager.execute(downloadThread);
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

    public void updateAllFilesLists() throws IOException {
        if (!isLeftListOnline) {
            showLocalDirectory(leftPath, leftList);
            leftPathView.setText(leftPath.toString());
        }
        if (!isRightListOnline) {
            showLocalDirectory(rightPath, rightList);
            rightPathView.setText(rightPath.toString());
        }
        if (isRightListOnline) {
            showOnlineDirectory(receiveFileList(out), rightList, rightPath);
        }
        if (isLeftListOnline) {
            showOnlineDirectory(receiveFileList(out), leftList, leftPath);
        }
    }

    //позволяет выставить каталог слева равный каталогу справа (для удобства работы)
    public void sourceEquallyTarget() {
        Thread directoryUpdate = new Thread(() -> {
            if (leftList.isFocused()) { //если выделенное окно слева правый==левому
                rightPath.delete(0, rightPath.length());
                rightPath.append(leftPath);
                isRightListOnline = isLeftListOnline;
                showLocalDirectory(rightPath, rightList);
            }
            if (rightList.isFocused()) { //если выделенное окно справа левый=правому
                leftPath.delete(0, leftPath.length());
                leftPath.append(rightPath);
                isLeftListOnline = isRightListOnline;
                showLocalDirectory(leftPath, leftList);
            }
        });
        directoryUpdate.interrupt();
        threadManager.execute(directoryUpdate);
    }

    public String[] queryStringListener() {
        int readNumberBytes = 0;
        try {
            readNumberBytes = rbc.read(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] queryAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes)).split(DELIMETER);
        byteBuffer.clear();
        return queryAnswer;
    }

    public String[] receiveFileList(DataOutputStream out) {
        try {
            out.write("/ls".getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryStringListener();
    }

    public void registration(ActionEvent actionEvent) {
        RegistrationWindowStage rs = new RegistrationWindowStage(out, rbc);
        rs.setMinWidth(400);
        rs.setMinHeight(150);
        rs.setResizable(false);
        rs.show();
    }
}

/*Обнаруженные косяки:
 * 1. при вставке пути вместо имени файла (например C:\path\) выскакивает исключение
 * 2. когда у пользователя в директории ничего нет, ничего не открывается:) надо поправить
 * 3. не забыть при создании файла или папки убирать в окончании пробелы
 * 4. При выравнивании папок (онлайн) появляется кнопка BACK
 * 5. после переименования файла также выскакивает BACK (при этом переименование происходит в локальной директории
 *    а обновляется онлайн директория)*/
