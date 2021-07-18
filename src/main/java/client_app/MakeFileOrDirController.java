package client_app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static client_app.FileOperations.showDirectory;
import static client_app.RenameWindowStage.getFileName;

public class MakeFileOrDirController implements Initializable {
    @FXML
    TextField fileName;

    @FXML
    TextField extension;

    @FXML
    Button apply;

    @FXML
    Button cancel;

    @FXML
    ChoiceBox<String> choiceBox;

    boolean isDirectory;

    Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choiceBox.getItems().add("Directory");
        choiceBox.getItems().add("File");
        choiceBox.setValue("File");

        choiceBox.setOnAction((event -> {
            if ("Directory".equals(choiceBox.getSelectionModel().getSelectedItem())) {
                isDirectory = true;
                extension.setVisible(false);
                extension.setPrefWidth(0);
                fileName.setPrefWidth(315);
                fileName.focusedProperty();
            }
            if ("File".equals(choiceBox.getSelectionModel().getSelectedItem())) {
                isDirectory = false;
                extension.setVisible(true);
                extension.setPrefWidth(65);
                fileName.setPrefWidth(250);
                fileName.focusedProperty();
            }
        }));
    }

    public void apply() {
        stage = (Stage) apply.getScene().getWindow();
        fileName.focusedProperty();
        StringBuilder firstPath = ((MakeFileOrDirStage) apply.getScene().getWindow()).firstPath;
        StringBuilder secondPath = ((MakeFileOrDirStage) apply.getScene().getWindow()).secondPath;
        ListView<String> firstList = ((MakeFileOrDirStage) apply.getScene().getWindow()).firstList;
        ListView<String> secondList = ((MakeFileOrDirStage) apply.getScene().getWindow()).secondList;
        Path path = Path.of(firstPath + File.separator + fileName.getText()
                + (!isDirectory ? "." + extension.getText() : ""));
        if (path.toFile().exists()) {
            stage.setTitle("Error! File or directory exist");
        } else {
            try {
                if (isDirectory) {
                    Files.createDirectory(path);
                } else {
                    Files.createFile(path);
                }
                showDirectory(firstPath, firstList);
                showDirectory(secondPath, secondList);
                stage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancel() {
        stage = (Stage) apply.getScene().getWindow();
        stage.close();
    }
}
