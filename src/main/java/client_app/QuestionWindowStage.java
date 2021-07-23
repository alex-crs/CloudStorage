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
    static String element;

    public static String getElement() {
        return element;
    }

    public static String getMessage() {
        switch (action) {
            case COPY:
                return element + " уже существует." + "\n\r Желаете заменить?";
            case DELETE:
                return "Вы уверены что хотите удалить " + element;
        }
        return null;
    }

    public QuestionWindowStage(WorkPanel sourcePanel, WorkPanel targetPanel, String element, Action action) {
        QuestionWindowStage.action = action;
        QuestionWindowStage.element = element;
        this.sourcePanel = sourcePanel;
        this.targetPanel = targetPanel;
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/QuestionWindow.fxml"));
            setTitle("Warning");
            Scene scene = new Scene(root, 400, 150);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
