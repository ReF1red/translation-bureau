package bureau;

import bureau.repository.ClientRepository;
import bureau.repository.JdbcClientRepository;
import bureau.repository.JdbcTableRepository;
import bureau.repository.JdbcTranslationOrderRepository;
import bureau.repository.TableRepository;
import bureau.repository.TranslationOrderRepository;
import bureau.service.ClientService;
import bureau.service.TableService;
import bureau.service.TranslationOrderService;
import bureau.ui.ClientMenu;
import bureau.ui.ConsoleInput;
import bureau.ui.ConsoleMenu;
import bureau.ui.OrderMenu;
import bureau.util.DatabaseManager;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.NoSuchElementException;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            DatabaseManager databaseManager = new DatabaseManager();
            TableRepository tableRepository = new JdbcTableRepository(databaseManager);
            TableService tableService = new TableService(tableRepository);
            ClientRepository clientRepository = new JdbcClientRepository(databaseManager);
            TranslationOrderRepository orderRepository = new JdbcTranslationOrderRepository(databaseManager);
            ClientService clientService = new ClientService(clientRepository);
            TranslationOrderService orderService = new TranslationOrderService(orderRepository, clientService);
            ConsoleInput input = new ConsoleInput(scanner, System.out);
            ClientMenu clientMenu = new ClientMenu(input, System.out, clientService);
            OrderMenu orderMenu = new OrderMenu(input, System.out, orderService);
            ConsoleMenu menu = new ConsoleMenu(input, System.out, tableService, clientMenu, orderMenu);
            menu.run();
        } catch (NoSuchElementException e) {
            System.out.println("\nВвод завершён. До свидания!");
        }
    }
}
