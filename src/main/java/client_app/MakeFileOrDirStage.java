package client_app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

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
