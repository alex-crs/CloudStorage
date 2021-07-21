package client_app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static client_app.RenameWindowStage.*;
import static client_app.FileOperations.*;

public class RenameWindowController implements Initializable {
    @FXML
    TextField fileName;

    @FXML
    TextField extension;

    @FXML
    Button apply;

    @FXML
    Button cancel;

    Stage stage;

    String[] tokens;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tokens = getFileName().replace(".", ":").split(":", 2);
        if (tokens.length == 1) {
            extension.setVisible(false);
            extension.setPrefWidth(0);
            fileName.setPrefWidth(300);
            fileName.setText(tokens[0]);
            fileName.focusedProperty();
        } else {
            fileName.setText(tokens[0]);
            extension.setText(tokens[1].replace(":", "."));
            fileName.focusedProperty();
        }
    }

    public void apply() {
        StringBuilder renamePath = ((RenameWindowStage) apply.getScene().getWindow()).renamePath;
        StringBuilder secondPath = ((RenameWindowStage) apply.getScene().getWindow()).secondPath;
        ListView<String> renameList = ((RenameWindowStage) apply.getScene().getWindow()).renameList;
        ListView<String> secondList = ((RenameWindowStage) apply.getScene().getWindow()).secondList;
        File sourceFile = new File(renamePath + File.separator + getFileName());
        File targetFile;
        if (sourceFile.isFile()) {
            targetFile = new File(renamePath + File.separator + fileName.getText() + "." + extension.getText());
        } else {
            targetFile = new File(renamePath + File.separator + fileName.getText());
        }
        sourceFile.renameTo(targetFile);
        showLocalDirectory(renamePath, renameList);
        showLocalDirectory(secondPath, secondList);
        stage = (Stage) apply.getScene().getWindow();
        stage.close();
    }

    public void cancel() {
        stage = (Stage) apply.getScene().getWindow();
        stage.close();
    }
}
