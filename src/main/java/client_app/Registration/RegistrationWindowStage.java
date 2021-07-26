package client_app.Registration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public class RegistrationWindowStage extends Stage {
    DataOutputStream out;
    ReadableByteChannel rbc;

    public RegistrationWindowStage(DataOutputStream out, ReadableByteChannel rbc) {
        this.out = out;
        this.rbc = rbc;

        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/RegistrationWindow.fxml"));
            setTitle("Registration window");
            Scene scene = new Scene(root, 400, 150);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
