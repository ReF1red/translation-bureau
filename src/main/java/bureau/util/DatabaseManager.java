package bureau.util;

import bureau.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    public Connection getConnection() {
        Properties settings = new Properties();

        try (InputStream input = getClass().getResourceAsStream("/database.properties")) {
            if (input == null) {
                throw new DatabaseException("Не найден файл database.properties. Скопируйте пример настроек и заполните его.");
            }
            settings.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException | IllegalArgumentException e) {
            throw new DatabaseException("Не удалось прочитать настройки базы данных.", e);
        }

        String url = settings.getProperty("db.url");
        String user = settings.getProperty("db.user");
        String password = settings.getProperty("db.password");

        if (url == null || url.isBlank() || user == null || user.isBlank() || password == null) {
            throw new DatabaseException("В database.properties нужны db.url, db.user и db.password.");
        }

        Properties connectionSettings = new Properties();
        connectionSettings.setProperty("user", user.trim());
        connectionSettings.setProperty("password", password);
        connectionSettings.setProperty("connectTimeout", "5");
        connectionSettings.setProperty("socketTimeout", "10");

        try {
            return DriverManager.getConnection(url.trim(), connectionSettings);
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось подключиться к PostgreSQL. Проверьте запуск сервера, имя базы, пользователя и пароль.", e);
        }
    }
}
