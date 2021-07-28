package client_app.InfoWindow;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;


import java.net.URL;
import java.util.ResourceBundle;

import static client_app.InfoWindow.InfoWindowStage.getInfo;

public class InfoWindowController implements Initializable {
    Stage stage;

    @FXML
    Button noBtn;

    @FXML
    Label message;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        message.setText(getInfo());
    }


    public void closeButton(){
        stage = (Stage) noBtn.getScene().getWindow();
        stage.close();
    }


}
