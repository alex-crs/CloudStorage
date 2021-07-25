package client_app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import static client_app.FileOperations.clearEmptySymbolsAfterName;
import static client_app.MainWindowController.*;

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

    boolean isDirectory = true;

    Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileName.requestFocus();
        choiceBox.setFocusTraversable(false);
        choiceBox.getItems().add("Directory");
        choiceBox.getItems().add("File");
        extension.setVisible(false);
        choiceBox.setValue("Directory");


        choiceBox.setOnAction((event -> {
            if ("Directory".equals(choiceBox.getSelectionModel().getSelectedItem())) {
                extension.setVisible(false);
                isDirectory = true;
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
        WorkPanel sourcePanel = ((MakeFileOrDirStage) apply.getScene().getWindow()).sourcePanel;
        Action action = ((MakeFileOrDirStage) apply.getScene().getWindow()).action;
        Path path = Path.of(sourcePanel.getCurrentPath() + File.separator + clearEmptySymbolsAfterName(fileName.getText())
                + (!isDirectory ? "." + clearEmptySymbolsAfterName(extension.getText()) : ""));
        try {
            switch (action) {
                case CREATE:
                    if (path.toFile().exists()) {
                        stage.setTitle("Error! File or directory exist");
                    } else {
                        if (isDirectory) {
                            Files.createDirectory(path);
                        } else {
                            Files.createFile(path);
                        }
                        stage.close();
                    }
                    break;
                case CREATE_REMOTE:
                    int i;
                    if (isDirectory) {
                        i = sourcePanel.getNetworkManager().makeDir(path.toString());
                    } else {
                        i = sourcePanel.getNetworkManager().touchFile(path.toString());
                    }
                    if (i > 0) {
                        updateAllFilesLists();
                    }
                    break;
            }
            updateAllFilesLists();
            stage = (Stage) apply.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        stage = (Stage) apply.getScene().getWindow();
        stage.close();
    }
}
