package ObjectMakers;

import entitys.Action;
import entitys.WorkPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MakeFileOrDirStage extends Stage {
    WorkPanel sourcePanel;
    Action action;

    public MakeFileOrDirStage(WorkPanel sourcePanel, Action action) {
        this.sourcePanel = sourcePanel;
        this.action = action;
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/MakeFileOrDirWindow.fxml"));
            setTitle("Create file or directory");
            Scene scene = new Scene(root, 480, 25);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
