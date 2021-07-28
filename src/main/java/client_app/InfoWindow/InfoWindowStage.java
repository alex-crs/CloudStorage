package client_app.InfoWindow;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class InfoWindowStage extends Stage {

    public static String info;

    public static String getInfo() {
        return info;
    }

    public InfoWindowStage(String message) {
        info = message;

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/InfoWindow.fxml"));
            setTitle("Warning");
            Scene scene = new Scene(root, 250, 100);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
