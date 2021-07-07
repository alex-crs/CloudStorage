package client_app;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOutboundBuffer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class ClientTest extends JFrame {
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;


    public ClientTest() throws IOException {
        socket = new Socket("localhost", 5679);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());


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
                uploadFile(cmd[1]);
            } else if ("download".equals(cmd[0])) {
                downloadFile(cmd[1]);
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

    private void downloadFile(String fileName) {
        File file = new File("client" + File.separator + fileName);
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.write(("d" + "  " + fileName + "  " + file.length()).getBytes());
            if (!file.exists()) {
                file.createNewFile();
            }
            while (true) {
                String answer = bufferedReader.readLine().replace("\n", "");
                if ("ok".equals(answer)) {
                    break;
                } else if ("nex".equals(answer)) {
                    System.out.println("File not found!"); //отработать этот модуль
                    throw new FileNotFoundException();
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(in);
            ByteBuffer byteBuffer = ByteBuffer.allocate(8 * 1024);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            FileChannel fileChannel = randomAccessFile.getChannel();
            while (rbc.read(byteBuffer) != -1) {
                byteBuffer.flip();
                fileChannel.position(file.length());
                fileChannel.write(byteBuffer);
                byteBuffer.compact();
            }
            System.out.println("done");
            byteBuffer.clear();
            rbc.close();
            fileChannel.close();
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void uploadFile(String fileName) {  //готов для работы
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            File file = new File("client" + File.separator + fileName);
            out.write(("u" + "  " + fileName + "  "
                    + file.length()).getBytes());

            while (true) {
                String answer = bufferedReader.readLine().replace("\n", "");
                if ("ok".equals(answer)) {
                    break;
                } else if ("ex".equals(answer)) {
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
            out.flush();
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

    public static void main(String[] args) throws IOException {
        new ClientTest();
    }
}
