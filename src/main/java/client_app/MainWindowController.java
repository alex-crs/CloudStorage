package client_app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;

import static client_app.Action.*;
import static client_app.FileOperations.*;

public class MainWindowController implements Initializable {
    @FXML
    ListView<String> leftList;

    @FXML
    ListView<String> rightList;

    StringBuilder rightPath = new StringBuilder();
    StringBuilder leftPath = new StringBuilder();
    ObservableList<String> leftFiles = FXCollections.emptyObservableList();
    ObservableList<String> rightFiles = FXCollections.emptyObservableList();
    MultipleSelectionModel<String> leftMarkedFiles;
    MultipleSelectionModel<String> rightMarkedFiles;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        leftPath.append("c:\\");
        rightPath.append("c:\\");
        showDirectory(rightPath, rightList);
        showDirectory(leftPath, leftList);
        rightMarkedFiles = rightList.getSelectionModel();
        rightMarkedFiles.setSelectionMode(SelectionMode.MULTIPLE);
        leftMarkedFiles = leftList.getSelectionModel();
        leftMarkedFiles.setSelectionMode(SelectionMode.MULTIPLE);
    }

    //--------------------------------------------------------------------------------
    /*управляют активными окнами. В случае выделения одного или нескольких файлов
     * добавляют их в лист для дальнейших операций. При этом если файлы выбираются в одном окне
     * в другом список очищается, таким образом мы работаем только с одним активным окном*/
    public void leftEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            leftFiles = leftMarkedFiles.getSelectedItems();
            rightMarkedFiles.clearSelection();
        }
        if (mouseEvent.getClickCount() == 2) {
            String currentElement = leftList.getSelectionModel().getSelectedItem();
            eventAction(currentElement, leftPath, leftList);
        }
    }

    public void rightEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            rightFiles = rightMarkedFiles.getSelectedItems();
            leftMarkedFiles.clearSelection();
        }
        if (mouseEvent.getClickCount() == 2) {
            String currentElement = rightList.getSelectionModel().getSelectedItem();
            eventAction(currentElement, rightPath, rightList);
        }
    }
    //--------------------------------------------------------------------------------


    /*Данный метод строит путь как вперед так и назад, если дважды кликнуть по кнопке BACK, то данный метод
     * возвращает в предыдущую директорию, если было передано имя директории, то метод строит путь дальше.
     * Также в данном методе производится проверка на то, является ли файлом переданное имя, если да, то ПОКА
     * ничего не происходит (в дальнейшем добавлю открытие в системных программах)*/
    private void eventAction(String element, StringBuilder currentPath, ListView<String> renewableFileList) {
        String[] tokens = currentPath.toString().split(Matcher.quoteReplacement(File.separator));
        if ("BACK".equals(element)) {
            currentPath.delete((currentPath.length() - tokens[tokens.length - 1].length() - 1), currentPath.length());
            showDirectory(currentPath, renewableFileList);
        } else {
            if (new File(currentPath.toString() + File.separator + element).isDirectory()) {
                currentPath.append(element + File.separator);
                showDirectory(currentPath, renewableFileList);
            }
        }
    }

    /*Проверяем текущий выделенный файл в окне (если например выделен файл в правом окне, то копируем в левый
     * если выделен в левом окне, то копируем в правое окно
     * Если левое или правое окно подключены к облачному хранилищу, то вместо команды copy(), запускается метод upload*/
    public void copyAction() throws IOException {
        if (leftFiles.size() > 0) { //если выделенные файлы слева
            prepareAndCopy(leftPath, rightPath, leftFiles, rightList);
        }
        if (rightFiles.size() > 0) { //если выделенные файлы справа
            prepareAndCopy(rightPath, leftPath, rightFiles, leftList);
        }
    }

    /*позволяет построить пути для копирования и проверить существует ли файл, если файл существует,
     * появится окно с запросом о замене файла*/
    public void prepareAndCopy(StringBuilder sourcePath, StringBuilder targetPath,
                               ObservableList<String> files, ListView<String> renewableFileList) throws IOException {
        for (String element : files) {
            Path source = Path.of(sourcePath + File.separator + element);
            Path target = Path.of(targetPath + File.separator + element);
            if (target.toFile().exists()) {
                QuestionWindowStage qws = new QuestionWindowStage(source, target, element,
                        targetPath, renewableFileList, COPY);
                qws.setResizable(false);
                qws.show();
            } else {
                copy(source, target, targetPath, renewableFileList);
            }
        }
    }

    public void deleteAction() throws IOException {
        if (leftFiles.size() > 0) { //если выделенные файлы слева
            prepareAndDelete(leftPath, leftFiles, leftList);
        }
        if (rightFiles.size() > 0) { //если выделенные файлы справа
            prepareAndDelete(rightPath, rightFiles, rightList);
        }
    }

    private void prepareAndDelete(StringBuilder path, ObservableList<String> files,
                                  ListView<String> renewableFileList) throws IOException {
        Iterator<String> iterator = files.iterator();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            delete(path, fileName, renewableFileList);
        }
        updateAllFilesLists();
    }

    private void updateAllFilesLists() {
        showDirectory(leftPath, leftList);
        showDirectory(rightPath, rightList);
    }
}
