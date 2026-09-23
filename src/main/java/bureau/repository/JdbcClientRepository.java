package bureau.repository;

import bureau.exception.DatabaseException;
import bureau.model.Client;
import bureau.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcClientRepository implements ClientRepository {
    private final DatabaseManager databaseManager;

    public JdbcClientRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Client create(Client client) {
        String sql = "INSERT INTO clients (name, email, phone) VALUES (?, ?, ?) RETURNING id";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.getName());
            statement.setString(2, client.getEmail());
            statement.setString(3, client.getPhone());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Не получен ID клиента.");
                }
                client.setId(result.getLong("id"));
                return client;
            }
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT id, name, email, phone FROM clients ORDER BY id";
        List<Client> clients = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                clients.add(readClient(result));
            }
            return clients;
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public Client findById(long id) {
        String sql = "SELECT id, name, email, phone FROM clients WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readClient(result) : null;
            }
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public boolean update(Client client) {
        String sql = "UPDATE clients SET name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.getName());
            statement.setString(2, client.getEmail());
            statement.setString(3, client.getPhone());
            statement.setLong(4, client.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public boolean existsByEmail(String email, long excludedId) {
        String sql = "SELECT 1 FROM clients WHERE email = ? AND id <> ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setLong(2, excludedId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    @Override
    public boolean hasOrders(long clientId) {
        String sql = "SELECT 1 FROM translation_orders WHERE client_id = ? LIMIT 1";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, clientId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private Client readClient(ResultSet result) throws SQLException {
        return new Client(result.getLong("id"), result.getString("name"),
                result.getString("email"), result.getString("phone"));
    }

    private DatabaseException databaseError(SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new DatabaseException("Клиент с таким email уже существует.", e);
        }
        if ("23503".equals(e.getSQLState())) {
            return new DatabaseException("Нельзя удалить клиента, у которого есть заказы.", e);
        }
        return new DatabaseException("Не удалось выполнить операцию с клиентами в базе данных.", e);
    }
}
