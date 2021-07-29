package client_app.SearchWindow;

import client_app.Resources.WorkPanel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;

import static client_app.Main_Functional.MainWindowController.*;
import static client_app.SearchWindow.SearchWindowStage.getSearchPathFromPanel;

public class SearchWindowController implements Initializable {
    @FXML
    Button searchBtn;

    @FXML
    Button cancelBtn;

    @FXML
    TextField searchObject;

    @FXML
    TextField searchPath;

    @FXML
    Button toLeftPanel;

    @FXML
    Button toRightPanel;

    @FXML
    ListView<String> searchResult;

    boolean isOnline;

    private ObservableList<String> fileList = FXCollections.emptyObservableList();
    private MultipleSelectionModel<String> markedElementsListener;

    WorkPanel firstPanel;
    WorkPanel secondPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchPath.setText(getSearchPathFromPanel());
        searchResult.setFocusTraversable(false);
        searchObject.requestFocus();
        fileList = FXCollections.emptyObservableList();
        this.markedElementsListener = searchResult.getSelectionModel();

    }

    public void mouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {

        }
        if (mouseEvent.getClickCount() == 2) {
            if (!firstPanel.isOnline()) {
                File file = new File(markedElementsListener.getSelectedItem());
                if (file.isFile()) {
                    try {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void searchObject() {
        firstPanel = ((SearchWindowStage) cancelBtn.getScene().getWindow()).firstPanel;
        secondPanel = ((SearchWindowStage) cancelBtn.getScene().getWindow()).secondPanel;
        searchResult.getItems().clear();
        String searchQuery = searchObject.getText();
        if (!searchQuery.isEmpty()) {
            if (!firstPanel.isOnline()) {
                searchLocalElementByName(firstPanel.getCurrentPath().toString(), searchQuery);
            }
            if (firstPanel.isOnline()) {
                isOnline = true;
                searchOnlineElementByName(firstPanel.getCurrentPath().toString(), searchQuery);
            }
        } else {
            searchResult.getItems().add("Введите запрос");
        }
    }

    private void searchLocalElementByName(String sourcePath, String searchQuery) {
        File file;
        File searchPath = new File(sourcePath);
        for (String element : searchPath.list()) {
            String path = sourcePath + element;
            file = new File(path);
            if ((element.toLowerCase()).contains(searchQuery.toLowerCase())) {
                searchResult.getItems().add(path);
            }
            if (file.isDirectory()) {
                searchLocalElementByName((sourcePath + element + File.separator), searchQuery);
            }
        }
    }

    private void searchOnlineElementByName(String sourcePath, String searchQuery) {
        for (String element : firstPanel.getNetworkManager().receiveFileList(sourcePath)) {
            String elementWithoutHead = element.replaceAll(".:", "");

            if ((elementWithoutHead.toLowerCase()).contains(searchQuery.toLowerCase())) {
                searchResult.getItems().add(sourcePath + File.separator + elementWithoutHead);
            }
            if (element.contains("d:")) {
                searchOnlineElementByName((sourcePath + File.separator + elementWithoutHead), searchQuery);
            }
        }
    }

    public void toRightPanel() {
        if (isOnline){
            getRightWorkPanel().setOnline(true);
        }
        getRightWorkPanel().setCurrentPath(getFoundPath());
        getRightWorkPanel().showDirectory();
        close();
    }

    public void toLeftPanel() {
        if (isOnline){
            getLeftWorkPanel().setOnline(true);
        }
        getLeftWorkPanel().setCurrentPath(getFoundPath());
        getLeftWorkPanel().showDirectory();
        close();
    }

    private String getFoundPath() {
        StringBuilder toPanel = new StringBuilder().append(markedElementsListener.getSelectedItem());
        String[] path = toPanel.toString().split(Matcher.quoteReplacement(File.separator));
        toPanel.delete(toPanel.length() - path[path.length - 1].length(), toPanel.length());
        return toPanel.toString();
    }

    public void close() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
