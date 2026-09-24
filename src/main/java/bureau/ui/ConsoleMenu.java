package bureau.ui;

import bureau.exception.DatabaseException;
import bureau.exception.ExportException;
import bureau.service.ExportService;
import bureau.service.StatisticsService;
import bureau.service.TableService;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class ConsoleMenu {
    private final ConsoleInput input;
    private final PrintStream output;
    private final TableService tableService;
    private final ClientMenu clientMenu;
    private final OrderMenu orderMenu;
    private final StatisticsService statisticsService;
    private final ExportService exportService;

    public ConsoleMenu(ConsoleInput input, PrintStream output, TableService tableService,
                       ClientMenu clientMenu, OrderMenu orderMenu,
                       StatisticsService statisticsService, ExportService exportService) {
        this.input = input;
        this.output = output;
        this.tableService = tableService;
        this.clientMenu = clientMenu;
        this.orderMenu = orderMenu;
        this.statisticsService = statisticsService;
        this.exportService = exportService;
    }

    public void run() {
        while (true) {
            printMainMenu();
            OptionalInt choice = input.readInt("Выберите действие: ");

            if (choice.isEmpty()) {
                output.println("\nВвод завершён. До свидания!");
                return;
            }

            switch (choice.getAsInt()) {
                case 1 -> clientMenu.run();
                case 2 -> orderMenu.run();
                case 3 -> orderMenu.runSearch();
                case 4 -> orderMenu.runFilters();
                case 5 -> showStatistics();
                case 6 -> exportData();
                case 7 -> showTables();
                case 0 -> {
                    output.println("До свидания!");
                    return;
                }
                default -> output.println("Ошибка: выберите пункт меню от 0 до 7.");
            }
        }
    }

    private void printMainMenu() {
        output.println();
        output.println("========================================");
        output.println("             БЮРО ПЕРЕВОДОВ");
        output.println("========================================");
        output.println("1. Клиенты");
        output.println("2. Заказы на перевод");
        output.println("3. Поиск заказов");
        output.println("4. Фильтрация и сортировка заказов");
        output.println("5. Статистика");
        output.println("6. Экспорт данных в Excel");
        output.println("7. Вывести таблицы базы данных");
        output.println("0. Выход");
    }

    private void showTables() {
        try {
            List<String> tables = tableService.getTableNames();
            if (tables.isEmpty()) {
                output.println("В базе пока нет таблиц.");
                return;
            }

            output.println("Таблицы базы данных:");
            for (String table : tables) {
                output.println(table);
            }
        } catch (DatabaseException e) {
            output.println("Ошибка: " + e.getMessage());
        }
    }

    private void showStatistics() {
        try {
            Map<String, Long> statistics = statisticsService.getStatistics();
            output.println("Статистика бюро переводов:");
            for (Map.Entry<String, Long> entry : statistics.entrySet()) {
                output.println(entry.getKey() + ": " + entry.getValue());
            }
        } catch (DatabaseException e) {
            output.println("Ошибка: " + e.getMessage());
        }
    }

    private void exportData() {
        try {
            Path file = exportService.export(Path.of("exports"));
            output.println("Данные сохранены: " + file);
        } catch (DatabaseException | ExportException e) {
            output.println("Ошибка: " + e.getMessage());
        }
    }
}
