package bureau.service;

import bureau.exception.BusinessException;
import bureau.exception.EntityNotFoundException;
import bureau.model.Client;
import bureau.repository.ClientRepository;

import java.util.List;
import java.util.Locale;

public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client create(Client client) {
        if (client == null) {
            throw new BusinessException("Данные клиента не указаны.");
        }
        client.setId(0);
        validate(client);
        return clientRepository.create(client);
    }

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public Client getById(long id) {
        if (id <= 0) {
            throw new BusinessException("ID клиента должен быть положительным.");
        }
        Client client = clientRepository.findById(id);
        if (client == null) {
            throw new EntityNotFoundException("Клиент с ID " + id + " не найден.");
        }
        return client;
    }

    public void update(Client client) {
        if (client == null) {
            throw new BusinessException("Данные клиента не указаны.");
        }
        getById(client.getId());
        validate(client);
        if (!clientRepository.update(client)) {
            throw new EntityNotFoundException("Клиент с ID " + client.getId() + " не найден.");
        }
    }

    public void delete(long id) {
        getById(id);
        if (clientRepository.hasOrders(id)) {
            throw new BusinessException("Нельзя удалить клиента, у которого есть заказы.");
        }
        if (!clientRepository.delete(id)) {
            throw new EntityNotFoundException("Клиент с ID " + id + " не найден.");
        }
    }

    private void validate(Client client) {
        client.setName(requiredText(client.getName(), "Имя клиента", 150));
        String email = requiredText(client.getEmail(), "Email", 254).toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new BusinessException("Укажите email в формате name@example.com.");
        }
        client.setEmail(email);

        String phone = client.getPhone();
        if (phone != null) {
            phone = phone.trim();
            if (phone.length() > 30) {
                throw new BusinessException("Телефон не должен быть длиннее 30 символов.");
            }
            if (phone.isEmpty()) {
                phone = null;
            }
        }
        client.setPhone(phone);

        if (clientRepository.existsByEmail(email, client.getId())) {
            throw new BusinessException("Клиент с таким email уже существует.");
        }
    }

    private String requiredText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(field + ": поле не должно быть пустым.");
        }
        String text = value.trim();
        if (text.length() > maxLength) {
            throw new BusinessException(field + ": максимум " + maxLength + " символов.");
        }
        return text;
    }
}
