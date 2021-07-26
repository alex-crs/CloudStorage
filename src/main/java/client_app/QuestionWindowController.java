package client_app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.ResourceBundle;

import static client_app.Action.*;
import static client_app.FileOperations.*;
import static client_app.MainWindowController.*;
import static client_app.QuestionWindowStage.getMessage;

public class QuestionWindowController implements Initializable {

    @FXML
    Button actionRun;

    @FXML
    Button noBtn;

    @FXML
    Label message;

    Stage stage;

    @FXML
    Button replace;

    @FXML
    Button replaceAll;

    int fileNumber;

    String element;
    WorkPanel sourcePanel;
    WorkPanel targetPanel;
    Path source;
    Path target;
    Action action;
    int answer;
    String fileName;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        action = QuestionWindowStage.action;
        message.setText(getMessage());
        fileNumber = -1;
        switch (action) {
            case COPY:
                replace.setVisible(false);
                break;
            case DELETE:
            case DELETE_REMOTE:
            case MOVE:
            case COPY_REMOTE:
            case UPLOAD:
            case DOWNLOAD:
                replace.setVisible(false);
                replaceAll.setVisible(false);
                actionRun.setLayoutX(50);
                actionRun.setLayoutY(50);
                actionRun.setText("Yes");
                break;
        }
    }

    public void actionRun() throws IOException {
        fileNumber++;
        sourcePanel = ((QuestionWindowStage) actionRun.getScene().getWindow()).sourcePanel;
        targetPanel = ((QuestionWindowStage) actionRun.getScene().getWindow()).targetPanel;
        if (fileNumber == sourcePanel.getMarkedFileList().size()) {
            updateAllFilesLists();
            closeButton();
        } else {
            switch (action) {
                case COPY:
                    for (; fileNumber < sourcePanel.getMarkedFileList().size(); ) {
                        element = sourcePanel.getMarkedFileList().get(fileNumber);
                        source = sourcePanel.getPathByElement(element);
                        target = targetPanel.getPathByElement(element);
                        if (!target.toFile().exists()) {
                            copy(source, target);
                            fileNumber++;
                        } else if (target.toFile().exists()) {
                            message.setText(element + "\r\nуже существует. Заменить?");
                            actionRun.setVisible(false);
                            replace.setVisible(true);
                            replaceAll.setVisible(false);
                            replace.setLayoutX(30);
                            replace.setLayoutY(50);
                            break;
                        }
                    }
                    break;
                case COPY_REMOTE:
                    String fileName;
                    for (String element : sourcePanel.getMarkedFileList()) {
                        fileName = element.replaceAll(".:", "");
                        answer = sourcePanel.getNetworkManager().copyObject(
                                (sourcePanel.getCurrentPath() + File.separator + fileName),
                                (targetPanel.getCurrentPath() + File.separator + fileName));
                        while (true) {
                            if (answer > 0) {
                                break;
                            }
                        }
                        sourcePanel.showDirectory();
                        targetPanel.showDirectory();
                    }
                    closeButton();
                    break;
                case DELETE:
                    isClarifyEveryTime = false;
                    prepareAndDelete(sourcePanel, targetPanel, action);
                    closeButton();
                    break;
                case MOVE:
                    isClarifyEveryTime = false;
                    prepareAndCopy(sourcePanel, targetPanel, COPY);
                    isClarifyEveryTime = false;
                    prepareAndDelete(sourcePanel, targetPanel, DELETE);
                    closeButton();
                    break;
                case DELETE_REMOTE:
                    Iterator<String> iterator = sourcePanel.getMarkedFileList().iterator();
                    while (iterator.hasNext()) {
                        fileName = iterator.next();
                        answer = sourcePanel.getNetworkManager().deleteObject(sourcePanel.getCurrentPath()
                                + File.separator + fileName.replaceAll(".:", ""));
                        while (true) {
                            if (answer > 0) {
                                break;
                            }
                        }
                        sourcePanel.showDirectory();
                        targetPanel.showDirectory();
                    }
                    closeButton();
                    break;
                case UPLOAD:

                    for (String element : sourcePanel.getMarkedFileList()) {
                        multipleElementUpload(sourcePanel, targetPanel, element);
                        sourcePanel.showDirectory();
                        targetPanel.showDirectory();
                    }
                    updateAllFilesLists();
                    closeButton();
                    break;
                case DOWNLOAD:
                    downloadElement(sourcePanel.getCurrentPath().toString(), targetPanel.getCurrentPath().toString(),
                            sourcePanel.getMarkedFileList().get(0), sourcePanel);
//                    download(sourcePanel,targetPanel);
                    closeButton();
                    break;
            }
        }
    }

    public void replace() throws IOException {
        copy(source, target);
        actionRun();
    }

    public void replaceAll() {
        sourcePanel = ((QuestionWindowStage) actionRun.getScene().getWindow()).sourcePanel;
        targetPanel = ((QuestionWindowStage) actionRun.getScene().getWindow()).targetPanel;
        isClarifyEveryTime = false;
        prepareAndCopy(sourcePanel, targetPanel, action);
        closeButton();
    }


    public void closeButton() {
        stage = (Stage) actionRun.getScene().getWindow();
        stage.close();
    }

}
