package lesson01;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
	private final Socket socket;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		try (
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream())
		) {
			System.out.printf("Client %s connected\n", socket.getInetAddress());
			while (true) {
				String command = in.readUTF();
				if ("upload".equals(command)) {
					try {
						File file = new File("server"  + File.separator + in.readUTF());
						if (!file.exists()) {
							 file.createNewFile();
						}
						FileOutputStream fos = new FileOutputStream(file);

						long size = in.readLong();

						byte[] buffer = new byte[8 * 1024];

						for (int i = 0; i < (size + (buffer.length-1)) / (buffer.length); i++) {
							int read = in.read(buffer);
							fos.write(buffer, 0, read);
						}
						fos.close();
						out.writeUTF("OK");
					} catch (Exception e) {
						out.writeUTF("FATAL ERROR");
					}
				}

				if ("download".equals(command)) {
					try {
						File file = new File("server" + File.separator + in.readUTF());

						long fileLength = file.length();
						FileInputStream fis = new FileInputStream(file);

						out.writeLong(fileLength);

						int read = 0;
						byte[] buffer = new byte[8 * 1024];
						while ((read = fis.read(buffer)) != -1) {
							out.write(buffer, 0, read);
						}
						fis.close();
						out.flush();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						out.writeLong(0);
					} catch (IOException e) {
						e.printStackTrace();
					}

					// TODO: 14.06.2021
				}
				if ("exit".equals(command)) {
					System.out.printf("Client %s disconnected correctly\n", socket.getInetAddress());
					break;
				}

				System.out.println(command);
				//out.writeUTF(command); - виновник
				/*вот поэтому и отправлялся "upload"
				сначала мы отправили команду на загрузку, сервер её принял и отправил нам "OK", но после отправки статуса вдогонку он отправил upload,
				но прочитали мы его только после отправки второго файла. Держу пари, что отправив 3ий файл мы также получим OK,
				а потом также upload*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
