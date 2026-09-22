package bureau.ui;

import java.io.PrintStream;
import java.util.OptionalInt;

public class ConsoleMenu {
    private final ConsoleInput input;
    private final PrintStream output;

    public ConsoleMenu(ConsoleInput input, PrintStream output) {
        this.input = input;
        this.output = output;
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
                case 1 -> showUnavailableSection("Клиенты");
                case 2 -> showUnavailableSection("Заказы на перевод");
                case 3 -> showUnavailableSection("Поиск заказов");
                case 4 -> showUnavailableSection("Фильтрация и сортировка заказов");
                case 5 -> showUnavailableSection("Статистика");
                case 6 -> showUnavailableSection("Экспорт данных в Excel");
                case 7 -> showUnavailableSection("Таблицы базы данных");
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

    private void showUnavailableSection(String name) {
        output.println("Раздел «" + name + "» пока не реализован.");
    }
}
