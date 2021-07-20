package server_app;

import org.apache.log4j.Logger;

import java.sql.*;

public class AuthService {
    private static Connection connection;
    private static Statement statement;
    private static final Logger LOGGER = Logger.getLogger(AuthService.class);

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");  //регистрация драйвера-менеджера
            connection = DriverManager.getConnection("jdbc:sqlite:dataBase.db"); //подключаем базу данных
            statement = connection.createStatement(); //создаем состояние
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static int addUser(String login, String pass, String nickname) { //добавление пользователя в базу
        LOGGER.info(String.format("Запрос добавления пользователя %s в базу данных", login));
        try {
            String query = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query); //запрос на изменение данных в SQL
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.setString(3, nickname);
            int result = ps.executeUpdate(); //проверяет сколько строк было изменено в базе данных, если ни одной то возвращает соответственно 0
            LOGGER.info(String.format(result == 0 ? "Не удалось добавить пользователя": "Пользователь успешно добавлен в базу данных"));
            return result;
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
        return 0;
    }


    public static String getNickNameByLoginAndPassword(String login, String password) {
        LOGGER.info(String.format("Осуществляется запрос в базе данных по логину пользователя %s", login));
        String query = String.format("select nickname, password from users where login='%s'", login);

        try {
            ResultSet rs = statement.executeQuery(query);
            int myHash = password.hashCode();

            if (rs.next()) {
                String nick = rs.getString(1);
                //изменяем тип поля PASSWORD на INTEGER в бд
                int dbHash = rs.getInt(2);
                if (myHash == dbHash) {
                    LOGGER.info(String.format("Пользователь с логином %s подтвержден", login));
                    return nick;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
        LOGGER.info(String.format("Пользователь с логином %s отсутствует в базе данных", login));
        return null;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
    }
}
