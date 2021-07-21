package server_app;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

import java.sql.*;

import static server_app.MainHandler.DELIMETER;
import static server_app.MainHandler.setCsUser;

public class AuthService {
    private static Connection connection;
    private static Statement statement;
    private static final Logger LOGGER = Logger.getLogger(AuthService.class);

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:dataBase.db");
            statement = connection.createStatement();
            LOGGER.info("DataBase connection successfully. Connection settings: " + statement.getConnection());
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
            String query = "INSERT INTO Users (login, password, name) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query); //запрос на изменение данных в SQL
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.setString(3, nickname);
            int result = ps.executeUpdate(); //проверяет сколько строк было изменено в базе данных, если ни одной то возвращает соответственно 0
            LOGGER.info(String.format(result == 0 ? "Не удалось добавить пользователя" : "Пользователь успешно добавлен в базу данных"));
            return result;
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
        return 0;
    }


    public static String getNickNameByLoginAndPassword(String login, String password) {
        LOGGER.info(String.format("Осуществляется запрос в базе данных по логину пользователя %s", login));
        String query = String.format("select name, password from Users where login='%s'", login);
        LOGGER.info(String.format("Попытка авторизации на сервере", login));
        try {
            ResultSet rs = statement.executeQuery(query);
            int myHash = password.hashCode();

            if (rs.next()) {
                String nick = rs.getString(1);
                int dbHash = rs.getInt(2);
                if (myHash == dbHash) {
                    LOGGER.info(String.format("Auth ok", login));
                    return nick;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
        LOGGER.info(String.format("Пользователь с логином %s отсутствует в базе данных", login));
        return null;
    }

    public static void tryToAuth(ChannelHandlerContext ctx, String[] header) {
        if (!header[1].isEmpty()) {
            LOGGER.info(ctx.channel().localAddress() + " user authorization with nick " + header[2]);
            String nick = getNickNameByLoginAndPassword(header[1], header[2]);
            if (nick != null) {
                setCsUser(header[1]);
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/auth-ok" + DELIMETER + nick + "\n").getBytes()));
                LOGGER.info(String.format("%s авторизован на сервере", header[1]));
            } else {
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/auth-no" + "\n").getBytes()));
            }
        }
    }

    public static void tryToReg(ChannelHandlerContext ctx, String[] header){
        if (!header[1].isEmpty()) {
            int result = addUser(header[1], header[2], header[3]);
            if (result > 0) {
                LOGGER.info(String.format("%s успешно зарегистрировался в системе", header[1]));
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/signup-ok" + "\n").getBytes()));
            } else {
                LOGGER.info(String.format("%s в регистрации отказано", header[1]));
                ctx.writeAndFlush(Unpooled.wrappedBuffer(("/signup-no" + "\n").getBytes()));
            }
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("Произошла ошибка:", e);
        }
    }
}
