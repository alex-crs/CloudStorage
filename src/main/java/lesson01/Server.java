package lesson01;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	// TODO: 14.06.2021
	// организовать корректный вывод статуса
	// Ответ: выполнено, комментарий прописан рядом с виновником
	// подумать почему так реализован цикл в ClientHandler
	/* Ответ: в данном цикле нам необходимо посчитать количество необходимых итераций для получения всего файла целиком,
	размер файла может быть равен размеру буфера и в таком случае если не использовать ((размер файла + буфер-1)/буфер) мы получим
	на одну итерацию больше и тогда выпадет исключение EOFException. Если мы разделим размер файла на размер буфера, то не дочитаем поток до конца
	и по итогу файл будет собран некорректно, в потоке останутся данные, а файл будет поврежден.
	Я думаю можно еще попробовать выполнить операцию сбора файла через остаток от деления (размера файла / буфер),
	но кода будет больше. Ваш вариант компактнее =)*/
	public Server() {
		ExecutorService service = Executors.newFixedThreadPool(4);
		try (ServerSocket server = new ServerSocket(5678)){
			System.out.println("Server started");
			while (true) {
				service.execute(new ClientHandler(server.accept()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server();
	}
}
