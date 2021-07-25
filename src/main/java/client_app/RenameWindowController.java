package client_app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static client_app.Action.RENAME;
import static client_app.Action.RENAME_REMOTE;
import static client_app.FileOperations.clearEmptySymbolsAfterName;
import static client_app.MainWindowController.updateAllFilesLists;
import static client_app.RenameWindowStage.*;

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

    String oldName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (action == RENAME) {
            tokens = getFileName().replace(".", ":").split(":", 2);
        }
        if (action == RENAME_REMOTE) {
            oldName = getFileName().replaceAll(".:", "");
            tokens = oldName.replace(".", ":").split(":", 2);
        }
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

    public void apply() throws IOException {
        WorkPanel sourcePanel = ((RenameWindowStage) apply.getScene().getWindow()).sourcePanel;
        switch (action) {
            case RENAME:
                File sourceFile = new File(sourcePanel.getCurrentPath() + File.separator + getFileName());
                File targetFile;
                if (sourceFile.isFile()) {
                    targetFile = new File(sourcePanel.getCurrentPath()
                            + File.separator + clearEmptySymbolsAfterName(fileName.getText())
                            + "." + clearEmptySymbolsAfterName(extension.getText()));
                } else {
                    targetFile = new File(sourcePanel.getCurrentPath()
                            + File.separator + clearEmptySymbolsAfterName(fileName.getText()));
                }
                sourceFile.renameTo(targetFile);
                updateAllFilesLists();
                stage = (Stage) apply.getScene().getWindow();
                stage.close();
                break;
            case RENAME_REMOTE:
                int answer;
                if (getFileName().contains("f:")) {
                    answer = sourcePanel.getNetworkManager().renameObject(
                            (sourcePanel.getCurrentPath() + File.separator + oldName),
                            (sourcePanel.getCurrentPath() + File.separator
                                    + clearEmptySymbolsAfterName(fileName.getText())
                                    + "." + clearEmptySymbolsAfterName(extension.getText())));
                } else {
                    answer = sourcePanel.getNetworkManager().renameObject(
                            (sourcePanel.getCurrentPath() + File.separator + oldName),
                            (sourcePanel.getCurrentPath() + File.separator
                                    + clearEmptySymbolsAfterName(fileName.getText())));
                }
                if (answer > 0) {
                    updateAllFilesLists();
                }
                stage = (Stage) apply.getScene().getWindow();
                stage.close();
                break;
        }
    }

    public void cancel() {
        stage = (Stage) apply.getScene().getWindow();
        stage.close();
    }
}
