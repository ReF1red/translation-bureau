package bureau.repository;

import bureau.exception.DatabaseException;
import bureau.model.OrderStatus;
import bureau.model.TranslationOrder;
import bureau.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcTranslationOrderRepository implements TranslationOrderRepository {
    private final DatabaseManager databaseManager;

    public JdbcTranslationOrderRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public TranslationOrder create(TranslationOrder order) {
        String sql = "INSERT INTO translation_orders (client_id, title, description, source_language, "
                + "target_language, word_count, price, status, deadline, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, order);
            statement.setTimestamp(10, Timestamp.valueOf(order.getCreatedAt()));
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Не получен ID заказа.");
                }
                order.setId(result.getLong("id"));
                return order;
            }
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public List<TranslationOrder> findAll() {
        String sql = "SELECT * FROM translation_orders ORDER BY id";
        List<TranslationOrder> orders = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                orders.add(readOrder(result));
            }
            return orders;
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public TranslationOrder findById(long id) {
        String sql = "SELECT * FROM translation_orders WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readOrder(result) : null;
            }
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public boolean update(TranslationOrder order) {
        String sql = "UPDATE translation_orders SET client_id = ?, title = ?, description = ?, "
                + "source_language = ?, target_language = ?, word_count = ?, price = ?, "
                + "status = ?, deadline = ? WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, order);
            statement.setLong(10, order.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM translation_orders WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private void fillStatement(PreparedStatement statement, TranslationOrder order) throws SQLException {
        statement.setLong(1, order.getClientId());
        statement.setString(2, order.getTitle());
        statement.setString(3, order.getDescription());
        statement.setString(4, order.getSourceLanguage());
        statement.setString(5, order.getTargetLanguage());
        statement.setInt(6, order.getWordCount());
        statement.setBigDecimal(7, order.getPrice());
        statement.setString(8, order.getStatus().name());
        statement.setDate(9, Date.valueOf(order.getDeadline()));
    }

    private TranslationOrder readOrder(ResultSet result) throws SQLException {
        return new TranslationOrder(result.getLong("id"), result.getLong("client_id"),
                result.getString("title"), result.getString("description"),
                result.getString("source_language"), result.getString("target_language"),
                result.getInt("word_count"), result.getBigDecimal("price"),
                OrderStatus.valueOf(result.getString("status")),
                result.getTimestamp("created_at").toLocalDateTime(),
                result.getDate("deadline").toLocalDate());
    }

    private DatabaseException databaseError(SQLException e) {
        if ("23503".equals(e.getSQLState())) {
            return new DatabaseException("Указанный клиент не существует.", e);
        }
        return new DatabaseException("Не удалось выполнить операцию с заказами в базе данных.", e);
    }
}
