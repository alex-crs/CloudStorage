package client_app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static client_app.FileOperations.*;
import static client_app.MainWindowController.*;

public class QuestionWindowController implements Initializable {

    @FXML
    Button yesBtn;

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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        message.setText("Перезаписать при совпадении имен?");
        fileNumber = -1;
        replace.setVisible(false);
    }

    public void actionRun() throws IOException {
        fileNumber++;
        sourcePanel = ((QuestionWindowStage) yesBtn.getScene().getWindow()).sourcePanel;
        targetPanel = ((QuestionWindowStage) yesBtn.getScene().getWindow()).targetPanel;
        Action action = QuestionWindowStage.action;
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
                            yesBtn.setVisible(false);
                            replace.setVisible(true);
                            replaceAll.setVisible(false);
                            replace.setLayoutX(30);
                            replace.setLayoutY(50);
                            break;
                        }
                    }
                    break;
                case DELETE:
                    delete(sourcePanel.currentPath, element);
                    closeButton();
                    break;
            }
        }
    }

    public void replace() throws IOException {
        copy(source, target);
        actionRun();
    }

    public void replaceAll() throws IOException {
        sourcePanel = ((QuestionWindowStage) yesBtn.getScene().getWindow()).sourcePanel;
        targetPanel = ((QuestionWindowStage) yesBtn.getScene().getWindow()).targetPanel;
        isClarifyEveryTime = false;
        prepareAndCopy(sourcePanel, targetPanel);
        closeButton();
    }


    public void closeButton() {
        stage = (Stage) yesBtn.getScene().getWindow();
        stage.close();
    }

}
