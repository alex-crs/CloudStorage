package client_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartClient extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(150);
        primaryStage.setTitle("Pixel File Explorer");
        FXMLLoader mainWindow = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent main = mainWindow.load();
        primaryStage.setScene(new Scene(main, 600, 500));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
