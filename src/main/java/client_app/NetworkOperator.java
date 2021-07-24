package client_app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

import static client_app.RegistrationWindowController.DELIMETER;

public class NetworkOperator {
    DataOutputStream out;
    DataInputStream in;
    ReadableByteChannel rbc;
    ByteBuffer byteBuffer;

    public NetworkOperator(DataOutputStream out, DataInputStream in, ReadableByteChannel rbc, ByteBuffer byteBuffer) {
        this.out = out;
        this.in = in;
        this.rbc = rbc;
        this.byteBuffer = byteBuffer;
    }

    public String[] receiveFileList(StringBuilder path) {
        try {
            out.write(("/ls").getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryStringListener(rbc, byteBuffer);
    }

    public static String[] queryStringListener(ReadableByteChannel readableByteChannel, ByteBuffer byteBuffer) {
        int readNumberBytes = 0;
        try {
            readNumberBytes = readableByteChannel.read(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] queryAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes)).split(DELIMETER);
        byteBuffer.clear();
        return queryAnswer;
    }

    public void enterToDirectory(String path){
        try {
            out.write(("/cd" + DELIMETER + path).getBytes());
            out.flush();
            rbc.read(byteBuffer);
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void outFromDirectory() {
        try {
            out.write(("/cd" + DELIMETER + "..").getBytes());
            out.flush();
            rbc.read(byteBuffer);
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
