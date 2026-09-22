package bureau;

import bureau.repository.JdbcTableRepository;
import bureau.repository.TableRepository;
import bureau.service.TableService;
import bureau.ui.ConsoleInput;
import bureau.ui.ConsoleMenu;
import bureau.util.DatabaseManager;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            DatabaseManager databaseManager = new DatabaseManager();
            TableRepository tableRepository = new JdbcTableRepository(databaseManager);
            TableService tableService = new TableService(tableRepository);
            ConsoleInput input = new ConsoleInput(scanner, System.out);
            ConsoleMenu menu = new ConsoleMenu(input, System.out, tableService);
            menu.run();
        }
    }
}
