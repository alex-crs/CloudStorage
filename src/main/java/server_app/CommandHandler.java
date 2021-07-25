package server_app;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;

import static server_app.MainHandler.*;

public class CommandHandler {
    //    private static final String LS_COMMAND = "ls (path) - view all files from current directory;\r\n" +
//            "    ~ - move to root directory;\r\n" +
//            "   .. - move to previous directory;\r\n";
//    private static final String MKDIR_COMMAND = "mkdir (path) - view all files from current directory;\r\n";
//    private static final String TOUCH_COMMAND = "touch (filename) - create file;\r\n";
//    private static final String RM_COMMAND = "rm (filename/directory) - delete file/directory;\r\n";
//    private static final String COPY_COMMAND = "copy (source) (target) - copy file/directory;\r\n" +
//            "   - to copy directory\\files with spaces, use \"\" for path;\r\n" +
//            "   - example: copy \"dir 1\\name\" \"dir 2\"; \r\n";
//    private static final String CAT_COMMAND = "cat (filename) - read file;\r\n";
//    private static final String CNAME_COMMAND = "cname (name) - change nickname;\r\n";
//    private static final String SHUTDOWN_COMMAND = "shutdown - for close connection and server shutdown;\r\n";
//
//    public static void query(String msg, CSUser client, ChannelHandlerContext ctx) throws IOException {
//        try {
//            String command = msg
//                    .replace("\n", "")
//                    .replace("\r", "");
//            String[] tokens = command.split(" ", 2);
//            if ("yes".toLowerCase().equals(tokens[0]) || "y".toLowerCase().equals(tokens[0])) {
//                client.setQueryAnswer(true);
//                tokens = client.getQueryCache();
//            } else if ("no".toLowerCase().equals(tokens[0]) || "n".toLowerCase().equals(tokens[0])) {
//                tokens = client.getQueryCache();
//            }
//            switch (tokens[0]) {
//                case ("--help"):
//                    sendMessage(LS_COMMAND, ctx);
//                    sendMessage(MKDIR_COMMAND, ctx);
//                    sendMessage(TOUCH_COMMAND, ctx);
//                    sendMessage(RM_COMMAND, ctx);
//                    sendMessage(COPY_COMMAND, ctx);
//                    sendMessage(CAT_COMMAND, ctx);
//                    sendMessage(CNAME_COMMAND, ctx);
//                    sendMessage(SHUTDOWN_COMMAND, ctx);
//                    break;
//                case ("ls"):
//                    sendMessage(getFilesList(client).concat("\r\n"), ctx);
//                    break;
//                case ("shutdown"):
//                    Server.shutdownServer();
//                    break;
//                case ("cd"):
//                    if (tokens[1].equals("..")) {
//                        changeDirectory(tokens[1], ctx, 'u', client);
//                    } else if (tokens[1].equals("~")) {
//                        client.getCurrentPath().replace(0, client.getCurrentPath().length(), client.getRoot());
//                    } else {
//                        changeDirectory(tokens[1], ctx, 'd', client);
//                    }
//                    break;
//                case ("touch"):
//                    touchFile(tokens[1], ctx, client);
//                    break;
//                case ("mkdir"):
//                    makeDir(tokens[1], ctx, client);
//                    break;
//                case ("rm"):
//                    try {
//                        if (!Files.exists(Path.of(client.getCurrentPath() + File.separator + tokens[1]))) {
//                            throw new FileNotFoundException();
//                        }
//                        if (client.getQueryCache() == null) {
//                            client.setQueryCache(tokens);
//                            throw new DelAnswer();
//                        } else if (client.isQueryAnswer()) {
//                            client.setQueryCache(null);
//                            client.setQueryAnswer(false);
//                            removeFileOrDirectory(tokens[1], client);
//                        } else {
//                            client.setQueryCache(null);
//                            client.setQueryAnswer(false);
//                        }
//                    } catch (FileNotFoundException e) {
//                        sendMessage("Error: File or directory not found!\r\n", ctx);
//                    }
//                    break;
//                case ("copy"):
//                    if (client.getQueryCache() == null) {
//                        client.setQueryCache(tokens);
//                        throw new CopyAnswer();
//                    } else if (client.isQueryAnswer()) {
//                        client.setQueryCache(null);
//                        client.setQueryAnswer(false);
//                        copy(tokens[1], ctx, StandardCopyOption.REPLACE_EXISTING, client);
//                    } else {
//                        client.setQueryCache(null);
//                        client.setQueryAnswer(false);
//                        copy(tokens[1], ctx, StandardCopyOption.COPY_ATTRIBUTES, client);
//                    }
//                    break;
//                case ("cname"):
//                    client.setUserName(tokens[1]);
//                    sendMessage("Имя пользователя изменено на: " + client.getUserName() + "\r\n", ctx);
//                    break;
//                case ("cat"):
//                    readDoc(tokens[1], ctx, client);
//                    break;
//                default:
//                    if (!client.isFirstRun()) sendMessage(command + ": command not found\r\n", ctx);
//                    client.setQueryCache(null);
//                    client.setQueryAnswer(false);
//                    break;
//            }
//            client.setFirstRun(false);
//            sendMessage("\r\n" + client.getUserName() + "@"
//                    + ctx.channel().localAddress().toString().replace("/", "")
//                    + " current dir:" + client.getCurrentPath().toString() + "\r\n" + "$ ", ctx);
//        } catch (DelAnswer answer) {
//            sendMessage("Are you sure? (yes/no): ", ctx);
//        } catch (CopyAnswer answer) {
//            sendMessage("Overwrite exiting files? (yes/no): ", ctx);
//        }
//    }
//
//    private static void readDoc(String token, ChannelHandlerContext ctx, CSUser client) throws IOException {
//        try {
//            sendMessage(Files.readString((Path.of(client.getCurrentPath() + File.separator
//                    + clearEmptySymbolsAfterName(token)))) + "\r\n", ctx);
//        } catch (NoSuchFileException e) {
//            sendMessage("Error: File not found!\r\n", ctx);
//        } catch (AccessDeniedException e) {
//            sendMessage("Error: this is directory!\r\n", ctx);
//        } catch (MalformedInputException e) {
//            sendMessage("Error: file type not supported!\r\n", ctx);
//        }
//    }
//
//    private static void copy(String source, ChannelHandlerContext ctx, CopyOption copyOption, CSUser client) throws IOException {
//        try {
//            StringBuilder validSourcePath = new StringBuilder();
//            String[] paths = source.split(" ");
//            if (paths.length > 2) { //если в пути пробелы, если нет то можно копировать и так
//                paths = source.split("\" \""); //пробуем использовать кавычки
//                if (paths.length > 2 || paths.length == 1) {
//                    throw new FileNotFoundException(); //если без кавычек путь и с пробелами то ошибка
//                }
//            }
//            //получаем путь и сразу чистим от ненужных кавычек
//            validSourcePath.append(paths[0].replace("\"", ""));
//            //сегментируем полученный путь для дальнейшего извлечения имени файла
//            String[] dirStructure = validSourcePath.toString().split(Matcher.quoteReplacement(File.separator));
//            //забираем имя файла из пути
//            String filename = dirStructure[dirStructure.length - 1];
//            //получаем путь куда копировать и чистим от кавычек
//            String targetPath = paths[1].replace("\"", "");
//            //пути готовы начинаем копировать
//            Path fromPath = Path.of(client.getCurrentPath() + File.separator + validSourcePath.toString());
//            Path toPath = Path.of(client.getCurrentPath().toString() + File.separator
//                    + targetPath + File.separator + filename);
//            Files.walkFileTree(fromPath, new SimpleFileVisitor<Path>() {
//                @Override
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    Path targetPath = toPath.resolve(fromPath.relativize(dir));
//                    if (!Files.exists(targetPath)) {
//                        Files.createDirectory(targetPath);
//                    }
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (FileNotFoundException e) {
//            sendMessage("Error: File or directory not found!\r\n", ctx);
//        } catch (FileAlreadyExistsException e) {
//            sendMessage("Error: File already exist!\r\n", ctx);
//        }
//    }
//
//    private static void sendMessage(String message, ChannelHandlerContext ctx) {
//        ctx.writeAndFlush(message);
//    }
//
//    private static String getFilesList(CSUser client) {
//        String[] servers = new File(client.getCurrentPath().toString()).list();
//        return String.join("\r\n", servers);
//    }
    public static String getFilesList(CSUser client, String path) {
        String[] directoryElements = new File(client.getRoot() + path).list();
        for (int i = 0; i < directoryElements.length; i++) {
            File file = new File(client.getRoot() + path + directoryElements[i]);
            if (file.isDirectory()) {
                directoryElements[i] = "d:" + directoryElements[i];
            } else if (file.isFile()) {
                directoryElements[i] = "f:" + directoryElements[i];
            }
        }
        return String.join(DELIMETER, directoryElements);
    }

    public static void makeDir(CSUser user, String fileName, ChannelHandlerContext ctx) {
        try {
            Files.createDirectory(Path.of(user.getRoot() + fileName));
        } catch (IOException e) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-bad").getBytes()));
        }
        ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok").getBytes()));
    }

    public static void touchFile(CSUser user, String fileName, ChannelHandlerContext ctx) {
        try {
            Files.createFile(Path.of(user.getRoot() + fileName));
        } catch (IOException e) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-bad").getBytes()));
        }
        ctx.writeAndFlush(Unpooled.wrappedBuffer(("/status-ok").getBytes()));
    }

    public static void renameDir(CSUser user, String oldName, String newName, ChannelHandlerContext ctx) {
        File oldFileName = new File(user.getRoot() + oldName);
        File newFileName = new File(user.getRoot() + newName);
        oldFileName.renameTo(newFileName);
    }


//    public static void changeDirectory(String path, ChannelHandlerContext ctx, CSUser client) {
//        try {
//            if (!path.equals("..")) {
//                throw new FileNotFoundException();
//            }

//        client.setCurrentPath(path);
//            switch (direction) {
//                case ('d'):
//                    client.setCurrentPath(client.getCurrentPath().append(File.separator + path));
//                    String[] servers = new File(client.getCurrentPath().toString()).list();
//                    break;
//                case ('u'):
//                    String[] tokens = client.getCurrentPath().toString().split(Matcher.quoteReplacement(File.separator));
//                    client.setCurrentPath(client.getCurrentPath().delete(
//                            client.getCurrentPath().length() - tokens[tokens.length - 1].length() - 1,
//                            client.getCurrentPath().length()));
//                    break;
//            }
////        } catch (FileNotFoundException e) {
////            sendMessage("Error: File or directory not found!\r\n", ctx);
//        }
//}

    //    private static void touchFile(String name, ChannelHandlerContext ctx, CSUser client) throws IOException {
//        try {
//            Files.createFile(Path.of(client.getCurrentPath() + File.separator + clearEmptySymbolsAfterName(name)));
//        } catch (FileAlreadyExistsException e) {
//            sendMessage("Error: File already exist!\r\n", ctx);
//        }
//    }
//
//
//    //удаляет пробелы в конце пути (полезно при создании папки с пробелами на конце, во избежании ошибки)
//    private static String clearEmptySymbolsAfterName(String name) {
//        StringBuilder string = new StringBuilder().append(name);
//        while (true) {
//            if (!string.toString().endsWith(" ")) {
//                break;
//            }
//            string.deleteCharAt(string.length() - 1);
//        }
//        return string.toString();
//    }
//
//    private static void removeFileOrDirectory(String name, CSUser client) throws IOException {
//        Files.walkFileTree(Path.of(client.getCurrentPath() + File.separator + name), new SimpleFileVisitor<Path>() {
//            @Override
//            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                Files.delete(file);
//                return FileVisitResult.CONTINUE;
//            }
//
//            @Override
//            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                Files.delete(dir);
//                return FileVisitResult.CONTINUE;
//            }
//        });
//    }
//
//    private static boolean checkUniqueFileOrDirectory(String path, CSUser client) {
//        String checkDirectory = path;
//        String[] tokens = path.split(Matcher.quoteReplacement(File.separator));
//        if (tokens.length > 1) {
//            checkDirectory = tokens[tokens.length - 1];
//        }
//        String[] string = new File(client.getCurrentPath().toString() + File.separator
//                + path.replaceAll(checkDirectory, "")).list();
//        for (String l : string) {
//            if (l.equals(checkDirectory)) {
//                return true;
//            }
//        }
//        return false;
//    }

//    private static class DelAnswer extends Exception {
//
//    }
//
//    private static class CopyAnswer extends Exception {
//
//    }
}
