package client_app.ObjectEditors;

import client_app.Resources.Action;
import client_app.Resources.WorkPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RenameWindowStage extends Stage {
    public static Action action;
    private static String fileName;
    WorkPanel sourcePanel;
    WorkPanel secondPanel;

    public static String getFileName() {
        return fileName;
    }

    public RenameWindowStage(WorkPanel sourcePanel, WorkPanel secondPanel, Action action) {
        this.sourcePanel = sourcePanel;
        this.secondPanel = secondPanel;
        fileName = sourcePanel.getMarkedFileList().get(0);
        RenameWindowStage.action = action;
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/RenameWindow.fxml"));
            setTitle("Rename");
            Scene scene = new Scene(root, 410, 25);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
