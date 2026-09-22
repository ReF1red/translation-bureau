package bureau.repository;

import bureau.exception.DatabaseException;
import bureau.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTableRepository implements TableRepository {
    private final DatabaseManager databaseManager;

    public JdbcTableRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public List<String> findTableNames() {
        String sql = "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = ? AND table_type = ? ORDER BY table_name";
        List<String> tables = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "public");
            statement.setString(2, "BASE TABLE");

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    tables.add(result.getString("table_name"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить список таблиц базы данных.", e);
        }

        return tables;
    }
}
