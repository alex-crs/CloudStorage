package client_app.Main_Functional;

import client_app.Main_Functional.NetworkManager;
import client_app.ObjectEditors.RenameWindowStage;
import client_app.ObjectMakers.MakeFileOrDirStage;
import client_app.QuestionWindow.QuestionWindowStage;
import client_app.Registration.RegistrationWindowStage;
import client_app.Resources.Action;
import client_app.Resources.WorkPanel;
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
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static client_app.Resources.Action.*;
import static client_app.Main_Functional.FileOperations.*;

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

    @FXML
    ChoiceBox<String> leftSortBox;

    @FXML
    ChoiceBox<String> rightSortBox;

    @FXML
    Label spaceCalc;

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
    public NetworkManager networkManager;
    //----------------------------------------------------

    //...

    //управление WorkPanels и текущими задачами
    //----------------------------------------------------
    private static WorkPanel leftWorkPanel;
    private static WorkPanel rightWorkPanel;
    public static NumberFormat nf = NumberFormat.getNumberInstance();
    //спрашивать каждый раз при удалении или замене файла
    public static boolean isClarifyEveryTime = true;
    float totalSpace;
    //----------------------------------------------------

    //...

    //управление действиями
    //----------------------------------------------------
    public static Action copyAction = COPY;
    public static Action deleteAction = DELETE;
    public static Action renameAction = RENAME;
    public static Action makeDirAction = CREATE;
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
        try {
            String totalSpaceString = nf.format(Files.getFileStore(Path.of("c:\\")).getUnallocatedSpace());
            totalSpace = Files.getFileStore(Path.of("c:\\")).getUnallocatedSpace();

//            totalSpace = Float.parseFloat(total.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        leftWorkPanel = new WorkPanel("c:\\", leftList, leftPathView, leftSortBox);
        rightWorkPanel = new WorkPanel("c:\\", rightList, rightPathView, rightSortBox);
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

    /*переопределяет действия на кнопки при определенном состоянии панелей.*/
    private void getCurrentActionCondition(WorkPanel firstPanel, WorkPanel secondPanel) {
        //Если слева и справа OFFLINE
        //Паттерн 1
        if (!firstPanel.isOnline() && !secondPanel.isOnline()) {
            copyAction = COPY;
            deleteAction = DELETE;
            renameAction = RENAME;
            makeDirAction = CREATE;
            moveAction = MOVE;
            move.setDisable(false);
        }
        //Если слева и справа ONLINE
        //Паттерн 2
        if (firstPanel.isOnline() && secondPanel.isOnline()) {
            copyAction = COPY_REMOTE;
            deleteAction = DELETE_REMOTE;
            renameAction = RENAME_REMOTE;
            makeDirAction = CREATE_REMOTE;
            moveAction = null;
            move.setDisable(true);
        }
        //Если слева ONLINE и справа OFFLINE
        //Паттерн 3
        if (firstPanel.isOnline() && !secondPanel.isOnline()) {
            copyAction = DOWNLOAD;
            deleteAction = DELETE_REMOTE;
            renameAction = RENAME_REMOTE;
            makeDirAction = CREATE_REMOTE;
            moveAction = null;
            move.setDisable(true);
        }
        //Если слева OFFLINE и справа ONLINE
        //Паттерн 4
        if (!firstPanel.isOnline() && secondPanel.isOnline()) {
            copyAction = UPLOAD;
            deleteAction = DELETE;
            renameAction = RENAME;
            makeDirAction = CREATE;
            moveAction = null;
            move.setDisable(true);
        }
    }

    public void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            rbc = Channels.newChannel(in);
            stage = (Stage) leftList.getScene().getWindow();
            threadManager = Executors.newFixedThreadPool(5);
            networkManager = new NetworkManager(out, in, rbc, byteBuffer, threadManager);
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
                String[] serverAnswer = networkManager.queryStringListener();
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
                    rightWorkPanel.connectToServer(networkManager);
                    leftWorkPanel.connectToServer(networkManager);
                    rightWorkPanel.setOnline(true);
                    rightWorkPanel.showDirectory();
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
            getCurrentActionCondition(leftWorkPanel, rightWorkPanel);
            rightWorkPanel.clearSelectionFiles();
            fileLengthView(leftWorkPanel);
//            leftWorkPanel.getSelectedFiles();

        }
        if (mouseEvent.getClickCount() == 2) {
            leftWorkPanel.treeMovement();
        }
    }

    public void rightEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            getCurrentActionCondition(rightWorkPanel, leftWorkPanel);
            leftWorkPanel.clearSelectionFiles();
            fileLengthView(rightWorkPanel);
//            rightWorkPanel.getSelectedFiles();
        }
        if (mouseEvent.getClickCount() == 2) {
            rightWorkPanel.treeMovement();
        }
    }
    //--------------------------------------------------------------------------------

    public void fileLengthView(WorkPanel workPanel) {
        float sumLength = 0;
        File file;
        int fileChoice = 0;
        for (String elements : workPanel.getSelectedFiles()) {
            file = new File(workPanel.getCurrentPath() + File.separator + elements);
            if (file.exists()) {
                sumLength = sumLength + file.length();
                fileChoice++;
            }
        }
        spaceCalc.setText("Выделено файлов " + fileChoice +
                " Размер файла(ов) на диске " + String.format("%.2f", sumLength / 1000000) + " mb."
                + "Доступно " + String.format("%.2f", totalSpace / 1000000) + " mb");
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
        QuestionWindowStage qws;
        switch (action) {
            case COPY:
                if (isFilesExist(sourcePanel, targetPanel) && isClarifyEveryTime) {
                    qws = new QuestionWindowStage(sourcePanel, targetPanel, action);
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
                break;
            case COPY_REMOTE:
            case DOWNLOAD:
            case UPLOAD:
                qws = new QuestionWindowStage(sourcePanel, targetPanel, action);
                qws.setResizable(false);
                qws.show();
                break;
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
                QuestionWindowStage qws = new QuestionWindowStage(leftWorkPanel, rightWorkPanel, moveAction);
                qws.setResizable(false);
                qws.show();
            }
        }
        if (rightWorkPanel.getMarkedFileList().size() > 0) { //если выделенные файлы справа
            if (!leftWorkPanel.getCurrentPath().toString().equals(rightWorkPanel.getCurrentPath().toString())) {
                QuestionWindowStage qws = new QuestionWindowStage(rightWorkPanel, leftWorkPanel, moveAction);
                qws.setResizable(false);
                qws.show();
            }
        }
    }

    //позволяет выставить каталог слева равный каталогу справа (для удобства работы)
    public void sourceEquallyTarget() {
        if (leftWorkPanel.getListView().isFocused()) { //если выделенное окно слева правый==левому
            rightWorkPanel.takePropertyFrom(leftWorkPanel);
            //если выделено левое окно, то данный метод принимает текущий путь левого окна
        }
        if (rightWorkPanel.getListView().isFocused()) { //если выделенное окно справа левый=правому
            leftWorkPanel.takePropertyFrom(rightWorkPanel);
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

/*Что доделать:
 * 1. Размер файла на диске
 * 2. Доступное место на диске
 * 3. Сортировка (готово)
 * 4. Ограничить место на диске*/
