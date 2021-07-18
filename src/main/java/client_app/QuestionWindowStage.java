package client_app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;

import static client_app.MainWindowController.*;

public class QuestionWindowStage extends Stage {

    private static String message;
    static Action action;
    private static String markedFile;
    Path sourcePath, targetPath;
    StringBuilder currentPath;
    ListView<String> fileList;

    public static String getMessage() {
        switch (action) {
            case COPY:
                return markedFile + " уже существует." + "\n\r Желаете заменить?";
            case DELETE:
                return "Вы уверены что хотите удалить " + markedFile;
        }
        return null;
    }

    public QuestionWindowStage(Path sourcePath, Path targetPath, String markedFile,
                               StringBuilder currentPath, ListView<String> fileList, Action action) {
        Parent root = null;
        try {
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
            QuestionWindowStage.markedFile = markedFile;
            this.currentPath = currentPath;
            this.fileList = fileList;
            this.action = action;
            root = FXMLLoader.load(getClass().getResource("/fxml/QuestionWindow.fxml"));
            setTitle("Warning");
            Scene scene = new Scene(root, 400, 150);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
