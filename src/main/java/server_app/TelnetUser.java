package server_app;

import io.netty.channel.ChannelHandlerContext;

public class TelnetUser {
    private static String root;
    private static String userName;
    private static StringBuilder currentPath;
    private static boolean firstRun;
    private String[] queryCache;
    private boolean queryAnswer;

    public TelnetUser(ChannelHandlerContext ctx) {
        //Hello message
        System.out.println("client connected: " + ctx.channel());
        ctx.writeAndFlush("Hello user!\r\n");
        ctx.writeAndFlush("Welcome to my second Telnet server from NETTY.\r\nPlease enter your command. " +
                "for help enter --help\r\n\n");

        currentPath = new StringBuilder();
        root = "server_app";
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
