package bureau.repository;

import bureau.model.Client;

import java.util.List;

public interface ClientRepository {
    Client create(Client client);
    List<Client> findAll();
    Client findById(long id);
    boolean update(Client client);
    boolean delete(long id);
    boolean existsByEmail(String email, long excludedId);
    boolean hasOrders(long clientId);
}
