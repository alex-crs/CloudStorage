package lesson02;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;

public class TelnetServer {
    private static final String LS_COMMAND = "ls (path) - view all files from current directory;\r\n" +
            "    ~ - move to root directory;\r\n" +
            "   .. - move to previous directory;\r\n";
    private static final String MKDIR_COMMAND = "mkdir (path) - view all files from current directory;\r\n";
    private static final String TOUCH_COMMAND = "touch (filename) - create file;\r\n";
    private static final String RM_COMMAND = "rm (filename/directory) - delete file/directory;\r\n";
    private static final String CP_COMMAND = "cp (source) (target) - copy file/directory;\r\n";
    private static final String CAT_COMMAND = "cat (filename) - read file;\r\n";
    private static final String CNAME_COMMAND = "cname (name) - change nickname;\r\n";
    private static final String SHUTDOWN_COMMAND = "shutdown - for close connection and server shutdown;\r\n";

    private final ByteBuffer buffer = ByteBuffer.allocate(512);
    private String root;
    private String userName = "root";
    private StringBuilder currentPath = new StringBuilder();
    private boolean firstRun = true;
    private String[] queryCache;
    private boolean queryAnswer = false;

    private Map<SocketAddress, String> clients = new HashMap<>();

    public TelnetServer() throws Exception {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(5679));
        server.configureBlocking(false);
        Selector selector = Selector.open();

        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started");
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                } else if (key.isReadable()) {
                    handleRead(key, selector);
                }
                iterator.remove();
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        System.out.println("Client connected. IP:" + channel.getRemoteAddress());
        channel.register(selector, SelectionKey.OP_READ, "skjghksdhg");
        channel.write(ByteBuffer.wrap("Hello user!\r\n".getBytes(StandardCharsets.UTF_8)));
        channel.write(ByteBuffer.wrap(("Welcome to my first Telnet server.\r\nPlease enter your command. " +
                "for help enter --help\r\n\n").getBytes(StandardCharsets.UTF_8)));
        root = "server";
        currentPath.delete(0, currentPath.length());
        currentPath.append(root);
        firstRun = true;
    }


    private void handleRead(SelectionKey key, Selector selector) throws IOException, DelAnswer {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAddress client = channel.getRemoteAddress();
        int readBytes = channel.read(buffer);

        if (readBytes < 0) {
            channel.close();
            return;
        } else if (readBytes == 0) {
            return;
        }

        buffer.flip();
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining()) {
            sb.append((char) buffer.get());
        }
        buffer.clear();

        // TODO: 21.06.2021
        // touch (filename) - создание файла (выполнено)
        // mkdir (dirname) - создание директории (выполнено)
        // cd (path | ~ | ..) - изменение текущего положения (выполнено)
        // rm (filename / dirname) - удаление файла / директории (выполнено)
        // copy (src) (target) - копирование файлов / директории (выполнено)
        // cat (filename) - вывод содержимого текстового файла (выполнено)
        // changenick (nickname) - изменение имени пользователя (выполнено)

        // добавить имя клиента
        try {
            if (key.isValid()) {
                String command = sb.toString()
                        .replace("\n", "")
                        .replace("\r", "");
                String[] tokens = command.split(" ", 2);
                if ("yes".toLowerCase().equals(tokens[0]) || "y".toLowerCase().equals(tokens[0])) {
                    queryAnswer = true;
                    tokens = queryCache;
                } else if ("no".toLowerCase().equals(tokens[0]) || "n".toLowerCase().equals(tokens[0])) {
                    tokens = queryCache;
                }
                switch (tokens[0]) {
                    case ("--help"):
                        sendMessage(LS_COMMAND, selector, client);
                        sendMessage(MKDIR_COMMAND, selector, client);
                        sendMessage(TOUCH_COMMAND, selector, client);
                        sendMessage(RM_COMMAND, selector, client);
                        sendMessage(CP_COMMAND, selector, client);
                        sendMessage(CAT_COMMAND, selector, client);
                        sendMessage(CNAME_COMMAND, selector, client);
                        sendMessage(SHUTDOWN_COMMAND, selector, client);
                        break;
                    case ("ls"):
                        sendMessage(getFilesList().concat("\r\n"), selector, client);
                        break;
                    case ("shutdown"):
                        channel.close();
                        selector.close();
                        break;
                    case ("cd"):
                        if (tokens[1].equals("..")) {
                            changeDirectory(tokens[1], 'u', selector, client);
                        } else if (tokens[1].equals("~")) {
                            currentPath.replace(0, currentPath.length(), root);
                        } else {
                            changeDirectory(tokens[1], 'd', selector, client);
                        }
                        break;
                    case ("touch"):
                        touchFile(tokens[1], selector, client);
                        break;
                    case ("mkdir"):
                        makeDir(tokens[1], selector, client);
                        break;
                    case ("rm"):
                        try {
                            if (!Files.exists(Path.of(currentPath + File.separator + tokens[1]))) {
                                throw new FileNotFoundException();
                            }
                            if (queryCache == null) {
                                queryCache = tokens;
                                throw new DelAnswer();
                            } else if (queryAnswer) {
                                queryCache = null;
                                queryAnswer = false;
                                removeFileOrDirectory(tokens[1]);
                            } else {
                                queryCache = null;
                                queryAnswer = false;
                            }
                        } catch (FileNotFoundException e) {
                            sendMessage("Error: File or directory not found!\r\n", selector, client);
                        }
                        break;
                    case ("cp"):
                        if (queryCache == null) {
                            queryCache = tokens;
                            throw new CopyAnswer();
                        } else if (queryAnswer) {
                            queryCache = null;
                            queryAnswer = false;
                            copy(tokens[1], selector, client, StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            queryCache = null;
                            queryAnswer = false;
                            copy(tokens[1], selector, client, StandardCopyOption.COPY_ATTRIBUTES);
                        }
                        break;
                    case ("cname"):
                        userName = tokens[1];
                        sendMessage("Имя пользователя изменено на: " + userName + "\r\n", selector, client);
                        break;
                    case ("cat"):
                        readDoc(tokens[1], selector, client);
                        break;
                    default:
                        if (!firstRun) sendMessage(command + ": command not found\r\n", selector, client);
                        queryCache = null;
                        queryAnswer = false;
                        break;
                }
                firstRun = false;
            }
            sendMessage(("\r\n" + userName + "@" + (channel.getLocalAddress()
                    .toString().replace("/", ""))
                    + " current dir:" + currentPath.toString() + "\r\n" + "$ "), selector, client);
        } catch (DelAnswer answer) {
            sendMessage("Are you sure? (yes/no): ", selector, client);
        } catch (CopyAnswer answer) {
            sendMessage("Overwrite exiting files? (yes/no): ", selector, client);
        }
    }

    private void readDoc(String token, Selector selector, SocketAddress client) throws IOException {
        try {
            sendMessage(Files.readString((Path.of(currentPath + File.separator + clearEmptySymbolsAfterName(token)))) + "\r\n", selector, client);
        } catch (NoSuchFileException e) {
            sendMessage("Error: File not found!\r\n", selector, client);
        } catch (AccessDeniedException e) {
            sendMessage("Error: this is directory!\r\n", selector, client);
        } catch (MalformedInputException e) {
            sendMessage("Error: file type not supported!\r\n", selector, client);
        }
    }

    private void copy(String source, Selector selector, SocketAddress client, CopyOption copyOption) throws IOException {
        try {  //стащил с просторов интернета... и адаптировал под себя
        StringBuilder validSourcePath = new StringBuilder(); //определяем путь копируемого файла (даже если путь с пробелами)
        String[] paths = source.split(" ");
        if (paths.length > 2) {
            paths = source.split("\" \"");
            if (paths.length > 2 || paths.length == 1) {
                throw new FileNotFoundException();
            }
        }
        validSourcePath.append(paths[0].replace("\"", ""));

        //сегментируем полученный путь
        String[] dirStructure = validSourcePath.toString().split(Matcher.quoteReplacement(File.separator));
        //забираем имя файла из пути
        String filename = dirStructure[dirStructure.length - 1];
        //получаем путь куда копировать
        String targetPath = paths[1].replace("\"", "");

        Path fromPath = Path.of(currentPath + File.separator + validSourcePath.toString());
        Path toPath = Path.of(currentPath.toString() + File.separator + targetPath + File.separator + filename);
            Files.walkFileTree(fromPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = toPath.resolve(fromPath.relativize(dir));
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (FileNotFoundException e) {
            sendMessage("Error: File or directory not found!\r\n", selector, client);
        } catch (FileAlreadyExistsException e) {
            sendMessage("Error: File already exist!\r\n", selector, client);
        }
    }

    private void sendMessage(String message, Selector selector, SocketAddress client) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                if (((SocketChannel) key.channel()).getRemoteAddress().equals(client)) {
                    ((SocketChannel) key.channel()).write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                }
            }
        }
    }

    private String getFilesList() {
        String[] servers = new File(currentPath.toString()).list();
        return String.join("\r\n", servers);
    }

    private void changeDirectory(String path, char direction, Selector selector, SocketAddress client) throws IOException {
        try {
            if (!path.equals("..") && !checkUniqueFileOrDirectory(path)) {
                throw new FileNotFoundException();
            }
            switch (direction) {
                case ('d'):
                    currentPath.append(File.separator + path);
                    String[] servers = new File(currentPath.toString()).list();
                    break;
                case ('u'):
                    String[] tokens = currentPath.toString().split(Matcher.quoteReplacement(File.separator));
                    currentPath.delete(
                            currentPath.length() - tokens[tokens.length - 1].length() - 1,
                            currentPath.length());
                    break;
            }
        } catch (FileNotFoundException e) {
            sendMessage("Error: File or directory not found!\r\n", selector, client);
        }
    }

    private void touchFile(String name, Selector selector, SocketAddress client) throws IOException {
        try {
            Files.createFile(Path.of(currentPath + File.separator + clearEmptySymbolsAfterName(name)));
        } catch (FileAlreadyExistsException e) {
            sendMessage("Error: File already exist!\r\n", selector, client);
        }
    }

    private void makeDir(String name, Selector selector, SocketAddress client) throws IOException {
        try {
            Files.createDirectory(Path.of(currentPath + File.separator + clearEmptySymbolsAfterName(name)));
        } catch (FileAlreadyExistsException e) {
            sendMessage("Error: Directory already exist!\r\n", selector, client);
        }
    }

    //удаляет пробелы в конце пути
    private String clearEmptySymbolsAfterName(String name) {
        StringBuilder string = new StringBuilder().append(name);
        while (true) {
            if (!string.toString().endsWith(" ")) {
                break;
            }
            string.deleteCharAt(string.length() - 1);
        }
        return string.toString();
    }

    private void removeFileOrDirectory(String name) throws IOException {
        Files.walkFileTree(Path.of(currentPath + File.separator + name), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private boolean checkUniqueFileOrDirectory(String path) {
        String checkDirectory = path;
        String[] tokens = path.split(Matcher.quoteReplacement(File.separator));
        if (tokens.length > 1) {
            checkDirectory = tokens[tokens.length - 1];
        }
        String[] string = new File(currentPath.toString() + File.separator + path.replaceAll(checkDirectory, "")).list();
        for (String l : string) {
            if (l.equals(checkDirectory)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        try {
            new TelnetServer();
        } catch (Exception e) {
            System.out.println("Server is shutdown");
        }
    }

    private class DelAnswer extends Exception {

    }

    private class CopyAnswer extends Exception {

    }
}
