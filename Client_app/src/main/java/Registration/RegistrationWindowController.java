package Registration;

import Main_Functional.MainWindowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

public class RegistrationWindowController {
    @FXML
    public TextField login;
    @FXML
    public PasswordField password;
    @FXML
    public PasswordField passwordRepeater;
    @FXML
    public TextField nickname;
    @FXML
    public Label resultLabel;
    @FXML
    public Button signupBtn;

    public static String DELIMETER = ";";

    public void signUp(ActionEvent actionEvent) {
        if ("Sign Up".equals(signupBtn.getText())) {
            Socket socket = null;
            DataOutputStream out = null;
            DataInputStream in = null;
            String result = null;
            ReadableByteChannel rbc = null;
            ByteBuffer byteBuffer = ByteBuffer.allocate(8 * 1024);

            try {
                socket = new Socket(MainWindowController.ADDRESS, MainWindowController.PORT);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());
                rbc = Channels.newChannel(in);
                if (socket != null && !password.getText().isEmpty() && password.getText().equals(passwordRepeater.getText())) {
                    out.write(("/signup" + DELIMETER + login.getText().toLowerCase() + DELIMETER
                            + password.getText() + DELIMETER + nickname.getText()).getBytes());
                    int readNumberBytes = rbc.read(byteBuffer);
                    String answer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes)).replace("\n","");
                    if ("/signup-no".equals(answer)){
                        resultLabel.setText("Registration denied");
                    }
                    if ("/signup-ok".equals(answer)){
                        resultLabel.setText("Registration successfully");
                    }
                } else {
                    resultLabel.setText("Password mismatch");
                }
            } catch (IOException e) {
                signupBtn.setText("Нет соединения с сервером");
            }

            resultLabel.setVisible(true);
            if (resultLabel.getText() != null && resultLabel.getText().contains("Registration successfully")) {
                signupBtn.setText("Exit");
            }
        } else {
            exitSignup();
        }
    }

    public void exitSignup() {
        Stage stage = (Stage) login.getScene().getWindow();
        stage.close();
    }
}
