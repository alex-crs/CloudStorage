package client_app;

import org.apache.log4j.Logger;
import server_app.MainHandler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import static client_app.MainWindowController.updateAllFilesLists;
import static client_app.RegistrationWindowController.DELIMETER;


public class NetworkManager {
    private static final Logger LOGGER = Logger.getLogger(NetworkManager.class);
    DataOutputStream out;
    DataInputStream in;
    ReadableByteChannel rbc;
    ByteBuffer byteBuffer;
    ExecutorService threadManager;



    public NetworkManager(DataOutputStream out, DataInputStream in, ReadableByteChannel rbc,
                          ByteBuffer byteBuffer, ExecutorService threadManager) {
        this.out = out;
        this.in = in;
        this.rbc = rbc;
        this.byteBuffer = byteBuffer;
        this.threadManager = threadManager;
    }

    public synchronized String[] receiveFileList(StringBuilder path) {
        try {
            out.write(("/ls" + DELIMETER + path + File.separator).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryStringListener();
    }

    public synchronized String[] receiveFileList(String path) {
        try {
            out.write(("/ls" + DELIMETER + path + File.separator).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryStringListener();
    }

    public synchronized String[] queryStringListener() {
        int readNumberBytes = 0;
        try {
            readNumberBytes = rbc.read(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(String.format("Error receiving a response from the server"));
        }
        String[] queryAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes)).split(DELIMETER);
        byteBuffer.clear();
        return queryAnswer;
    }

    public int enterToDirectory(String path) {
        try {
            out.write(("/cd" + DELIMETER + path).getBytes());
            out.flush();
            String[] serverAnswer = queryStringListener();
            if ("/status-ok".equals(serverAnswer[0])) {
                LOGGER.info(String.format("Enter to directory: operation successfully"));
                return 1; //возвращает 1 если ответ положительный
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Enter to directory error"));
        }
        return -1; //возвращает -1 если ответ отрицательный
    }

    public int makeDir(String name) {
        try {
            out.write(("/mkdir" + DELIMETER + name).getBytes());
            out.flush();
            String[] serverAnswer = queryStringListener();
            if ("/status-ok".equals(serverAnswer[0])) {
                LOGGER.info(String.format("Create dir: operation successfully"));
                return 1; //возвращает 1 если ответ положительный
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int touchFile(String name) {
        try {
            out.write(("/touch" + DELIMETER + name).getBytes());
            out.flush();
            String[] serverAnswer = queryStringListener();
            if ("/status-ok".equals(serverAnswer[0])) {
                LOGGER.info(String.format("Create file: operation successfully"));
                return 1; //возвращает 1 если ответ положительный
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int renameObject(String oldName, String newName) {
        try {
            out.write(("/rename" + DELIMETER + oldName + DELIMETER + newName).getBytes());
            out.flush();
            String[] serverAnswer = queryStringListener();
            if ("/status-ok".equals(serverAnswer[0])) {
                LOGGER.info(String.format("Rename file: operation successfully"));
                return 1; //возвращает 1 если ответ положительный
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int deleteObject(String name) {
        try {
            out.write(("/delete" + DELIMETER + name).getBytes());
            out.flush();
            String[] serverAnswer = queryStringListener();
            if ("/status-ok".equals(serverAnswer[0])) {
                LOGGER.info(String.format("Delete file: operation successfully"));
                return 1; //возвращает 1 если ответ положительный
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int copyObject(String source, String target) {
        try {
            out.write(("/copy" + DELIMETER + source + DELIMETER + target).getBytes());
            out.flush();
            String[] serverAnswer = queryStringListener();
            if ("/status-ok".equals(serverAnswer[0])) {
                LOGGER.info(String.format("Copy file: operation successfully"));
                return 1; //возвращает 1 если ответ положительный
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }



}
