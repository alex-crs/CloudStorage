package client_app.SearchWindow;

import client_app.Resources.WorkPanel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

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

    Thread searchThread;

    WorkPanel firstPanel;
    WorkPanel secondPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchPath.setText(getSearchPathFromPanel());
    }

    public void searchObject(){
        firstPanel = ((SearchWindowStage) cancelBtn.getScene().getWindow()).firstPanel;
        secondPanel = ((SearchWindowStage) cancelBtn.getScene().getWindow()).secondPanel;
        String searchQuery = searchObject.getText();
//        searchElementByName(firstPanel.getCurrentPath().toString(), searchQuery);

    }

//    private void searchElementByName(String sourcePath, String searchQuery){
//        File file;
////       for (String element:firstPanel.getCurrentDirectoryList()){
//////            file = new File(sourcePath + File.separator + )
////           if (element.equals(searchQuery)){
////               searchResult.getItems().add(element);
////           }
////           if ()
//       }
//    }

    public void toRightPanel(){

    }

    public void toLeftPanel(){

    }

    public void close(){

    }
}
