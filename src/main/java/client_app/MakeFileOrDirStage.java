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
    StringBuilder firstPath;
    StringBuilder secondPath;
    ListView<String> firstList;
    ListView<String> secondList;
    DataOutputStream out;
    ReadableByteChannel rbc;
    boolean isFirstPathOnline;
    boolean isSecondPathOnline;

    public MakeFileOrDirStage(StringBuilder firstPath, ListView<String> firstList, boolean isFirstPathOnline,
                              StringBuilder secondPath, ListView<String> secondList, boolean isSecondPathOnline,
                              DataOutputStream out, ReadableByteChannel rbc) {
        this.firstPath = firstPath;
        this.secondPath = secondPath;
        this.firstList = firstList;
        this.secondList = secondList;
        this.out = out;
        this.isFirstPathOnline = isFirstPathOnline;
        this.isSecondPathOnline = isSecondPathOnline;
        this.rbc = rbc;
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
