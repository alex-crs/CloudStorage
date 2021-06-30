package lesson03.handlers;

public class TelnetUser {
    private static String root;
    private static String userName;
    private static StringBuilder currentPath;
    private static boolean firstRun;
    private String[] queryCache;
    private boolean queryAnswer;

    public TelnetUser() {
        currentPath = new StringBuilder();
        root = "server";
        userName = "root";
        currentPath.delete(0, currentPath.length());
        currentPath.append(root);
        firstRun = true;
        queryAnswer=false;
    }

    public String[] getQueryCache() {
        return queryCache;
    }

    public void setQueryCache(String[] queryCache) {
        this.queryCache = queryCache;
    }

    public boolean isQueryAnswer() {
        return queryAnswer;
    }

    public void setQueryAnswer(boolean queryAnswer) {
        this.queryAnswer = queryAnswer;
    }

    public void setUserName(String userName) {
        TelnetUser.userName = userName;
    }

    public void setCurrentPath(StringBuilder currentPath) {
        TelnetUser.currentPath = currentPath;
    }

    public void setFirstRun(boolean firstRun) {
        TelnetUser.firstRun = firstRun;
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

    public boolean isFirstRun() {
        return firstRun;
    }
}
