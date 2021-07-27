package client_app.SearchWindow;

import client_app.Resources.WorkPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SearchWindowStage extends Stage {
    WorkPanel firstPanel, secondPanel;
    public static String searchPathFromPanel;

    public static String getSearchPathFromPanel() {
        return searchPathFromPanel;
    }

    public SearchWindowStage(WorkPanel firstPanel, WorkPanel secondPanel) {
        this.firstPanel = firstPanel;
        this.secondPanel = secondPanel;
        searchPathFromPanel = firstPanel.getCurrentPath().toString();

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/SearchWindow.fxml"));
            setTitle("Search window");
            Scene scene = new Scene(root, 600, 400);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
