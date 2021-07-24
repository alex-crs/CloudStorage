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
    Socket socket;
    public static final String ADDRESS = "127.0.0.1";
    public static final int PORT = 5679;
    public static DataOutputStream out;
    public static DataInputStream in;
    public static ReadableByteChannel rbc;
    public static ByteBuffer byteBuffer = ByteBuffer.allocate(8 * 1024);
    private static ExecutorService threadManager;
    public String DELIMETER = ";";
    private String AUTH_COMMAND = "/auth";
    private NetworkOperator networkOperator;
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

    private static WorkPanel leftWorkPanel;
    private static WorkPanel rightWorkPanel;
    static boolean isClarifyEveryTime = true; //спрашивать каждый раз при удалении или замене файла
    public static boolean userAnswer;
    //----------------------------------------------------

    //...

    //управление действиями
    //----------------------------------------------------
    public static Action copyAction = COPY;
    public static Action deleteAction = DELETE;
    public static Action renameAction = RENAME;
    public static Action makeDirAction = CREATE_LOCAL;
    public static Action moveAction = MOVE;

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
        leftWorkPanel = new WorkPanel("c:\\", leftList, leftPathView);
        rightWorkPanel = new WorkPanel("c:\\", rightList, rightPathView);
        leftWorkPanel.showDirectory();
        rightWorkPanel.showDirectory();
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
                String[] serverAnswer = queryStringListener(rbc, byteBuffer);
                if (!serverAnswer[0].isEmpty() && "/auth-ok".equals(serverAnswer[0])) {
                    setAuthorized(true);
                    nickname.append(serverAnswer[1].replace("\n", ""));
                    hideAuthFields();
                    networkOperator = new NetworkOperator(out, in, rbc, byteBuffer);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stage.setTitle("Pixel Cloud Explorer. User: " + nickname);
                        }
                    });
                    byteBuffer.clear();
//                    String[] queryAnswer = receiveFileList((rightPath), out, rbc, byteBuffer);
//                    try {
                        rightWorkPanel.connectToServer(networkOperator);
                        rightWorkPanel.setOnline(true);
                        rightWorkPanel.showDirectory();
//                        rightList.setCellFactory(null);
//                        isRightListOnline = true;
//                        changeCurrentPath(rightPath, queryAnswer[0], rightPathView);
//                        showOnlineDirectory(queryAnswer, rightList, rightPath);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
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


    public void disconnect() {
        hideAuthFields();
    }

    //--------------------------------------------------------------------------------
    /*управляют активными окнами. В случае выделения одного или нескольких файлов
     * добавляют их в лист для дальнейших операций. При этом если файлы выбираются в одном окне
     * в другом список очищается, таким образом мы работаем только с одним активным окном*/
    public void leftEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            rightWorkPanel.clearSelectionFiles();
            leftWorkPanel.getSelectedFiles();

        }
        if (mouseEvent.getClickCount() == 2) {
            leftWorkPanel.treeMovement();
        }
    }

    public void rightEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            leftWorkPanel.clearSelectionFiles();
            rightWorkPanel.getSelectedFiles();
        }
        if (mouseEvent.getClickCount() == 2) {
            rightWorkPanel.treeMovement();
        }
    }
    //--------------------------------------------------------------------------------

    private void eventOnlineAction(String element, StringBuilder currentPath, ListView<String> renewableFileList, TextField pathView) {
        Thread eventAction = new Thread(() -> {
            try {
                if ("BACK".equals(element)) {
                    String[] tokens = currentPath.toString().split(Matcher.quoteReplacement(File.separator));
                    currentPath.delete((currentPath.length() - tokens[tokens.length - 1].length() - 1), currentPath.length());
                    out.write(("/cd" + DELIMETER + currentPath).getBytes());
                    String[] serverAnswer = queryStringListener(rbc, byteBuffer);
                    changeCurrentPath(currentPath, serverAnswer[0], pathView);
                    showOnlineDirectory(serverAnswer, renewableFileList, currentPath);
                } else {
                    out.write(("/cd" + DELIMETER + currentPath + File.separator + element.replaceAll(".:", "")).getBytes());
                    String[] serverAnswer = queryStringListener(rbc, byteBuffer);
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
        if (leftWorkPanel.getMarkedFileList().size() > 0) { //если выделенные файлы слева
            prepareAndCopy(leftWorkPanel, rightWorkPanel, copyAction);
        }
        if (rightWorkPanel.getMarkedFileList().size() > 0) { //если выделенные файлы справа
            prepareAndCopy(rightWorkPanel, leftWorkPanel, copyAction);
        }
    }

    //данный метод подготавливает файлы и директории для копирования,
    public static void prepareAndCopy(WorkPanel sourcePanel, WorkPanel targetPanel, Action action) {
        if (isFilesExist(sourcePanel, targetPanel) && isClarifyEveryTime) {
            QuestionWindowStage qws = new QuestionWindowStage(sourcePanel, targetPanel, action);
            qws.setResizable(false);
            qws.show();
        } else {
            for (String element : sourcePanel.getMarkedFileList()) {
                try {
                    copy(sourcePanel.getPathByElement(element), targetPanel.getPathByElement(element));
                    sourcePanel.showDirectory();
                    targetPanel.showDirectory();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isClarifyEveryTime = true;
        }
    }

    public void deleteAction() throws IOException {
        if (leftWorkPanel.getMarkedFileList().size() > 0) { //если выделенные файлы слева
            prepareAndDelete(leftWorkPanel, rightWorkPanel, deleteAction);
        }
        if (rightWorkPanel.getMarkedFileList().size() > 0) { //если выделенные файлы справа
            prepareAndDelete(rightWorkPanel, leftWorkPanel, deleteAction);
        }
    }

    public static void prepareAndDelete(WorkPanel sourcePanel, WorkPanel targetPanel, Action action) throws IOException {
        if (isClarifyEveryTime) {
            QuestionWindowStage qws = new QuestionWindowStage(sourcePanel, targetPanel, action);
            qws.setResizable(false);
            qws.show();
        } else {
            Iterator<String> iterator = sourcePanel.getMarkedFileList().iterator();
            while (iterator.hasNext()) {
                String fileName = iterator.next();
                delete(sourcePanel.getCurrentPath(), fileName);
                sourcePanel.showDirectory();
                targetPanel.showDirectory();
            }
        }
        isClarifyEveryTime = true;
    }

    public void renameAction() {
        if (leftWorkPanel.getMarkedFileList().size() > 0) { //если выделенные файлы слева
            prepareAndRename(leftWorkPanel, rightWorkPanel, renameAction);
        }
        if (rightWorkPanel.getMarkedFileList().size() > 0) { //если выделенные файлы справа
            prepareAndRename(rightWorkPanel, leftWorkPanel, renameAction);
        }
    }

    //переименовывает файл в отдельном окне
    private void prepareAndRename(WorkPanel sourcePanel, WorkPanel secondPanel, Action action) {
        RenameWindowStage rws = new RenameWindowStage(sourcePanel, secondPanel, action);
        rws.setMinWidth(410);
        rws.setMinHeight(25);
        rws.setResizable(false);
        rws.show();
    }

    public void makeDirAction() {
        if (leftWorkPanel.getListView().isFocused()) { //если выделенное окно слева
            prepareAndMakeDir(leftWorkPanel, makeDirAction);
        }
        if (rightWorkPanel.getListView().isFocused()) { //если выделенное окно слева
            prepareAndMakeDir(rightWorkPanel, makeDirAction);
        }
    }

    //создание файла/директории производится по первому пути
    private void prepareAndMakeDir(WorkPanel sourcePanel, Action action) {
        MakeFileOrDirStage mds = new MakeFileOrDirStage(sourcePanel, action);
        mds.setMinWidth(480);
        mds.setMinHeight(25);
        mds.setResizable(false);
        mds.show();
    }

    //перемещает выбранные файлы
    public void moveAction() {
        if (leftWorkPanel.getMarkedFileList().size() > 0) { //если выделенные файлы слева
            if (!leftWorkPanel.getCurrentPath().toString().equals(rightWorkPanel.getCurrentPath().toString())) {
                QuestionWindowStage qws = new QuestionWindowStage(leftWorkPanel, rightWorkPanel, MOVE);
                qws.setResizable(false);
                qws.show();
            }
        }
        if (rightWorkPanel.getMarkedFileList().size() > 0) { //если выделенные файлы справа
            if (!leftWorkPanel.getCurrentPath().toString().equals(rightWorkPanel.getCurrentPath().toString())) {
                QuestionWindowStage qws = new QuestionWindowStage(rightWorkPanel, leftWorkPanel, MOVE);
                qws.setResizable(false);
                qws.show();
            }
        }
    }

    //позволяет выставить каталог слева равный каталогу справа (для удобства работы)
    public void sourceEquallyTarget() {
        if (leftWorkPanel.getListView().isFocused()) { //если выделенное окно слева правый==левому
            rightWorkPanel.setCurrentPath(leftWorkPanel);
            //если выделено левое окно, то данный метод принимает текущий путь левого окна
        }
        if (rightWorkPanel.getListView().isFocused()) { //если выделенное окно справа левый=правому
            leftWorkPanel.setCurrentPath(rightWorkPanel);
            //если выделено правое окно, то данный метод принимает текущий путь правого окна
        }
    }

    public static void updateAllFilesLists() throws IOException {
        leftWorkPanel.showDirectory();
        rightWorkPanel.showDirectory();
    }

    public static boolean isFilesExist(WorkPanel sourcePanel, WorkPanel targetPanel) {
        for (String element : sourcePanel.getMarkedFileList()) {
            Path target = targetPanel.getPathByElement(element);
            if (target.toFile().exists()) {
                return true;
            }
        }
        return false;
    }

    public void prepareAndUpload(String fileName, StringBuilder sourcePath) {
        Thread uploadThread = new Thread(() -> {
            try {
                File file = new File(sourcePath + File.separator + fileName);
                if (file.isFile()) {
                    out.write(("/upload" + DELIMETER + "f" + DELIMETER + fileName + DELIMETER
                            + file.length()).getBytes());
                    while (true) {
                        String[] serverAnswer = queryStringListener(rbc, byteBuffer);
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
                    String[] serverAnswer = queryStringListener(rbc, byteBuffer);
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
                String[] serverAnswer = queryStringListener(rbc, byteBuffer);
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
