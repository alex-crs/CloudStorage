package client_app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static client_app.MainWindowController.*;
import static client_app.QuestionWindowStage.*;
import static client_app.FileOperations.*;

public class QuestionWindowController implements Initializable {

    @FXML
    Button yesBtn;

    @FXML
    Button noBtn;

    @FXML
    Label message;

    Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        message.setText(getMessage());
    }

    public void yesBtnClick() throws IOException {
        Path sourcePath = ((QuestionWindowStage) yesBtn.getScene().getWindow()).sourcePath;
        Path targetPath = ((QuestionWindowStage) yesBtn.getScene().getWindow()).targetPath;
        StringBuilder currentPath = ((QuestionWindowStage) yesBtn.getScene().getWindow()).currentPath;
        ListView<String> fileList = ((QuestionWindowStage) yesBtn.getScene().getWindow()).fileList;
        Action action = ((QuestionWindowStage) yesBtn.getScene().getWindow()).action;

        switch (action){
            case COPY:
                copy(sourcePath, targetPath, currentPath, fileList);
                break;
            case DELETE:

                break;
            default:
                System.out.println("Команда не отработала");
        }
        stage = (Stage) yesBtn.getScene().getWindow();
        stage.close();
    }

    public void noBtnClick() {
        stage = (Stage) yesBtn.getScene().getWindow();
        stage.close();
    }

}
