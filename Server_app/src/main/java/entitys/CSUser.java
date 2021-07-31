package entitys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static Main_Functional.CommandManager.availableSpaceCalc;


//данный класс хранит настройки пользователя и управляет корневыми путями,
public class CSUser {
    private String root;
    private String userName;
    private StringBuilder currentPath;
    private long userQuota = 400000000L;
    private long availableSpace;

    public CSUser(String user) {
        currentPath = new StringBuilder();
        root = "userData" + File.separator + user;
        this.availableSpace = availableSpaceCalc(root + File.separator);
        setCurrentPath(root);
        userName = user;
        Path path = Path.of(root);
        if (!path.toFile().exists()) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void calcAvailableSpace(){
        this.availableSpace = availableSpaceCalc(root + File.separator);
    }

    public long getUserQuota() {
        return userQuota;
    }

    public long getAvailableSpace() {
        return availableSpace;
    }

    public void setAvailableSpace(long availableSpace) {
        this.availableSpace = availableSpace;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCurrentPath(String newPath) {
        currentPath.delete(0, currentPath.length());
        currentPath.append(newPath);
    }

    public String getRoot() {
        return root;
    }

    public String getUserName() {
        return userName;
    }

    public StringBuilder getCurrentPath() {
        return currentPath;
    }
}

