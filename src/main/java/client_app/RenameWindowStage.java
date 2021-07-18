package client_app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class RenameWindowStage extends Stage {
    private static String fileName;
    StringBuilder renamePath;
    StringBuilder secondPath;
    ListView<String> renameList;
    ListView<String> secondList;

    public static String getFileName() {
        return fileName;
    }

    public RenameWindowStage(String fileName, StringBuilder renamePath, ListView<String> renameList,
                             StringBuilder secondPath, ListView<String> secondList) {
        RenameWindowStage.fileName = fileName;
        this.renamePath = renamePath;
        this.secondPath = secondPath;
        this.renameList = renameList;
        this.secondList = secondList;
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
