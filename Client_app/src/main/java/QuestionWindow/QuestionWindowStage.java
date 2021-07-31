package QuestionWindow;

import entitys.Action;
import entitys.WorkPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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
            case COPY_REMOTE:
                return "Скопировать удаленные объекты?";
            case UPLOAD:
                return "Загрузить файлы на сервер?";
            case DOWNLOAD:
                return "Загрузить объект(ы) с сервера?";
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
