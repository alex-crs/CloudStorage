package client_app;

import org.apache.log4j.Logger;
import server_app.MainHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static client_app.RegistrationWindowController.DELIMETER;


public class NetworkManager {
    private static final Logger LOGGER = Logger.getLogger(NetworkManager.class);
    DataOutputStream out;
    DataInputStream in;
    ReadableByteChannel rbc;
    ByteBuffer byteBuffer;

    public NetworkManager(DataOutputStream out, DataInputStream in, ReadableByteChannel rbc, ByteBuffer byteBuffer) {
        this.out = out;
        this.in = in;
        this.rbc = rbc;
        this.byteBuffer = byteBuffer;
    }

    public String[] receiveFileList(StringBuilder path) {
        try {
            out.write(("/ls" + DELIMETER + path + File.separator).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryStringListener();
    }

    public String[] queryStringListener() {
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

    public void outFromDirectory() {
        try {
            out.write(("/cd" + DELIMETER + "..").getBytes());
            out.flush();
            String[] serverAnswer = queryStringListener();
            if ("/status-ok".equals(serverAnswer[0])) {
                LOGGER.info(String.format("Out from directory: operation successfully"));
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Out from directory error"));
        }
    }

    public void makeDir(){

    }

    public void touchFile(){

    }

}
