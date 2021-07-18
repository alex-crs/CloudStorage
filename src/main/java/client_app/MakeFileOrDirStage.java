package client_app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class MakeFileOrDirStage extends Stage {
    StringBuilder firstPath;
    StringBuilder secondPath;
    ListView<String> firstList;
    ListView<String> secondList;

    public MakeFileOrDirStage(StringBuilder firstPath, ListView<String> firstList,
                              StringBuilder secondPath, ListView<String> secondList) {
        this.firstPath = firstPath;
        this.secondPath = secondPath;
        this.firstList = firstList;
        this.secondList = secondList;
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
