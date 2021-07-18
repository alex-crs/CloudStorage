package client_app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;

import static client_app.Action.OVERWRITE;


public class ClientTest extends JFrame {
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final ReadableByteChannel rbc;
    ByteBuffer byteBuffer = ByteBuffer.allocate(8 * 1024);

    public ClientTest() throws IOException {
        socket = new Socket("localhost", 5679);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        rbc = Channels.newChannel(in);


        setSize(300, 300);
        JPanel panel = new JPanel(new GridLayout(2, 1));

        JButton btnSend = new JButton("SEND");
        JTextField textField = new JTextField();
        System.out.println(socket.getLocalPort());
        btnSend.addActionListener(a -> {
            // upload 1.txt
            // download img.png
            String[] cmd = textField.getText().split(" ");
            if ("upload".equals(cmd[0])) {
                uploadToServer(cmd[1]);
            } else if ("download".equals(cmd[0])) {
                downloadFromServer(cmd[1]);
            }
        });

        panel.add(textField);
        panel.add(btnSend);

        add(panel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                sendMessage("exit");
            }
        });
        setVisible(true);
    }

    private void downloadFromServer(String fileName) { //готов для работы
        File file = new File("client" + File.separator + fileName);
        long downloadFileLength = 0;


        try {
            out.write(("d  " + fileName).getBytes());
            String[] serverAnswer = queryFileInfo();
            if (!file.exists()) {
                file.createNewFile();
            }
            while (true) {
                if ("ok".equals(serverAnswer[0])) {
                    downloadFileLength = Long.parseLong(serverAnswer[1].replace("\n", ""));
                    break;
                } else if ("nex".equals(serverAnswer[0])) {
                    System.out.println("File not found!"); //отработать этот модуль
                    throw new FileNotFoundException();
                }
            }
            out.write(" ".getBytes());
            byteBuffer.clear();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            FileChannel fileChannel = randomAccessFile.getChannel();
            while ((rbc.read(byteBuffer)) > 0) {
                byteBuffer.flip();
                fileChannel.position(file.length());
                fileChannel.write(byteBuffer);
                byteBuffer.compact();
                if (file.length() == downloadFileLength) {
                    break;
                }
            }
            byteBuffer.clear();
            fileChannel.close();
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void uploadToServer(String fileName) { //готов для работы
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            File file = new File("client" + File.separator + fileName);
            out.write(("u  " + fileName + "  "
                    + file.length() + "  " + OVERWRITE).getBytes());

            String[] serverAnswer = queryFileInfo();
            while (true) {
                if ("ok".equals(serverAnswer[0].replace("\n", ""))) {
                    break;
                } else if ("nex".equals(serverAnswer[0])) {
                    System.out.println("Файл уже существует заменить?"); //отработать этот модуль
                    throw new FileAlreadyExistsException(fileName);
                }
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            int read = 0;
            byte[] buffer = new byte[8 * 1024];
            while ((read = randomAccessFile.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            byteBuffer.clear();
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] queryFileInfo() throws IOException {
        int readNumberBytes = rbc.read(byteBuffer);
        return new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes)).split("  ");
    }

    public static void main(String[] args) throws IOException {
        new ClientTest();
    }
}
