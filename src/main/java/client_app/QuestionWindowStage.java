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

    static Action action;
    WorkPanel sourcePanel, targetPanel;

    public static String getMessage() {
        switch (action) {
            case COPY:
                return "Перезаписать без подтверждения?";
            case DELETE:
            case DELETE_REMOTE:
                return "Уверены, что хотите удалить файл(ы)?";
            case MOVE:
                return "Хотите переместить файлы?";
        }
        return null;
    }

    public QuestionWindowStage(WorkPanel sourcePanel, WorkPanel targetPanel, Action action) {
        QuestionWindowStage.action = action;
        this.sourcePanel = sourcePanel;
        this.targetPanel = targetPanel;
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/QuestionWindow.fxml"));
            setTitle("Warning");
            Scene scene = new Scene(root, 250, 100);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
