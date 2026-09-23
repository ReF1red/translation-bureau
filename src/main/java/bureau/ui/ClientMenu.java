package bureau.ui;

import bureau.exception.BusinessException;
import bureau.exception.DatabaseException;
import bureau.model.Client;
import bureau.service.ClientService;

import java.io.PrintStream;
import java.util.List;

public class ClientMenu {
    private final ConsoleInput input;
    private final PrintStream output;
    private final ClientService clientService;

    public ClientMenu(ConsoleInput input, PrintStream output, ClientService clientService) {
        this.input = input;
        this.output = output;
        this.clientService = clientService;
    }

    public void run() {
        while (true) {
            output.println("\nКЛИЕНТЫ");
            output.println("1. Добавить клиента");
            output.println("2. Показать всех клиентов");
            output.println("3. Найти клиента по ID");
            output.println("4. Изменить клиента");
            output.println("5. Удалить клиента");
            output.println("0. Назад");

            try {
                switch (input.readNumber("Выберите действие: ")) {
                    case 1 -> create();
                    case 2 -> showAll();
                    case 3 -> showClient(clientService.getById(input.readId("ID клиента: ")));
                    case 4 -> update();
                    case 5 -> delete();
                    case 0 -> { return; }
                    default -> output.println("Ошибка: выберите пункт от 0 до 5.");
                }
            } catch (BusinessException | DatabaseException e) {
                output.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void create() {
        Client client = clientService.create(readClient(0));
        output.println("Клиент добавлен. ID: " + client.getId());
    }

    private void showAll() {
        List<Client> clients = clientService.getAll();
        if (clients.isEmpty()) {
            output.println("Клиентов пока нет.");
            return;
        }
        for (Client client : clients) {
            showClient(client);
        }
    }

    private void update() {
        long id = input.readId("ID клиента: ");
        showClient(clientService.getById(id));
        output.println("Введите новые данные полностью.");
        clientService.update(readClient(id));
        output.println("Клиент изменён.");
    }

    private void delete() {
        long id = input.readId("ID клиента: ");
        showClient(clientService.getById(id));
        if (!input.readText("Удалить клиента? (да/нет): ").equalsIgnoreCase("да")) {
            output.println("Удаление отменено.");
            return;
        }
        clientService.delete(id);
        output.println("Клиент удалён.");
    }

    private Client readClient(long id) {
        String name = input.readText("Имя или название организации: ");
        String email = input.readText("Email: ");
        String phone = input.readText("Телефон (можно оставить пустым): ");
        return new Client(id, name, email, phone);
    }

    private void showClient(Client client) {
        output.printf("ID: %d | %s | Email: %s | Телефон: %s%n", client.getId(),
                client.getName(), client.getEmail(), client.getPhone() == null ? "—" : client.getPhone());
    }
}
