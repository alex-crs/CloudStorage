package client_app;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class ClientTest extends JFrame {
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private BufferedReader bufferedReader;

    public ClientTest() throws IOException {
        socket = new Socket("localhost", 5679);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
        //out.write(("u " + "\'" + fileName + "\"").getBytes());


//        try {
//            out.writeUTF("download");
//            out.writeUTF(filename);
//            long size = in.readLong();
//            if (size == 0) {
//                System.out.println("File not found");
//                throw new FileNotFoundException();
//            }
//            File file = new File("client" + File.separator + filename);
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//
//            FileOutputStream fos = new FileOutputStream(file);
//
//            byte[] buffer = new byte[8 * 1024];
//
//            for (int i = 0; i < (size + (buffer.length - 1)) / (buffer.length); i++) {
//                int read = in.read(buffer);
//                fos.write(buffer, 0, read);
//            }
//            fos.close();
//            //проверяем соответствие размера файла переданому с сервера
//            if (file.length() == size) {
//                System.out.println("download status: OK");
//            } else {
//                System.out.println("ERROR! File spoiled!");
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    private void uploadFile(String fileName) {  //готов для работы
        try {
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
