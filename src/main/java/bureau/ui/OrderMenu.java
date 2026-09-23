package bureau.ui;

import bureau.exception.BusinessException;
import bureau.exception.DatabaseException;
import bureau.model.OrderStatus;
import bureau.model.TranslationOrder;
import bureau.service.TranslationOrderService;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderMenu {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final ConsoleInput input;
    private final PrintStream output;
    private final TranslationOrderService orderService;

    public OrderMenu(ConsoleInput input, PrintStream output, TranslationOrderService orderService) {
        this.input = input;
        this.output = output;
        this.orderService = orderService;
    }

    public void run() {
        while (true) {
            output.println("\nЗАКАЗЫ НА ПЕРЕВОД");
            output.println("1. Добавить заказ");
            output.println("2. Показать все заказы");
            output.println("3. Найти заказ по ID");
            output.println("4. Изменить данные заказа");
            output.println("5. Удалить заказ");
            output.println("6. Изменить статус заказа");
            output.println("0. Назад");

            try {
                switch (input.readNumber("Выберите действие: ")) {
                    case 1 -> create();
                    case 2 -> showAll();
                    case 3 -> showOrder(orderService.getById(input.readId("ID заказа: ")));
                    case 4 -> update();
                    case 5 -> delete();
                    case 6 -> changeStatus();
                    case 0 -> { return; }
                    default -> output.println("Ошибка: выберите пункт от 0 до 6.");
                }
            } catch (BusinessException | DatabaseException e) {
                output.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public void runSearch() {
        while (true) {
            output.println("\nПОИСК ЗАКАЗОВ");
            output.println("1. По названию");
            output.println("2. По описанию");
            output.println("0. Назад");

            try {
                switch (input.readNumber("Выберите действие: ")) {
                    case 1 -> showResults(orderService.searchByTitle(input.readText("Часть названия: ")));
                    case 2 -> showResults(orderService.searchByDescription(input.readText("Часть описания: ")));
                    case 0 -> { return; }
                    default -> output.println("Ошибка: выберите пункт от 0 до 2.");
                }
            } catch (BusinessException | DatabaseException e) {
                output.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public void runFilters() {
        while (true) {
            output.println("\nФИЛЬТРАЦИЯ И СОРТИРОВКА");
            output.println("1. По статусу");
            output.println("2. По клиенту");
            output.println("3. По статусу и клиенту");
            output.println("4. Сортировать все заказы");
            output.println("0. Назад");

            try {
                switch (input.readNumber("Выберите действие: ")) {
                    case 1 -> showResults(orderService.filter(readStatus("Статус заказа: "), null));
                    case 2 -> showResults(orderService.filter(null, input.readId("ID клиента: ")));
                    case 3 -> {
                        OrderStatus status = readStatus("Статус заказа: ");
                        long clientId = input.readId("ID клиента: ");
                        showResults(orderService.filter(status, clientId));
                    }
                    case 4 -> showResults(orderService.getAll());
                    case 0 -> { return; }
                    default -> output.println("Ошибка: выберите пункт от 0 до 4.");
                }
            } catch (BusinessException | DatabaseException e) {
                output.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void showResults(List<TranslationOrder> orders) {
        if (orders.isEmpty()) {
            output.println("Заказы не найдены.");
            return;
        }

        output.println("Найдено заказов: " + orders.size());
        output.println("Сортировка:");
        output.println("1. По сроку — сначала ближайшие");
        output.println("2. По сроку — сначала поздние");
        output.println("3. По стоимости — сначала дешёвые");
        output.println("4. По стоимости — сначала дорогие");
        output.println("0. Без сортировки");

        while (true) {
            int choice = input.readNumber("Выберите порядок: ");
            switch (choice) {
                case 0 -> showOrders(orders);
                case 1 -> showOrders(orderService.sortByDeadline(orders, true));
                case 2 -> showOrders(orderService.sortByDeadline(orders, false));
                case 3 -> showOrders(orderService.sortByPrice(orders, true));
                case 4 -> showOrders(orderService.sortByPrice(orders, false));
                default -> {
                    output.println("Ошибка: выберите пункт от 0 до 4.");
                    continue;
                }
            }
            return;
        }
    }

    private void create() {
        TranslationOrder order = orderService.create(readOrder(0, OrderStatus.NEW, null));
        output.println("Заказ добавлен со статусом «Новый». ID: " + order.getId());
    }

    private void showAll() {
        List<TranslationOrder> orders = orderService.getAll();
        if (orders.isEmpty()) {
            output.println("Заказов пока нет.");
            return;
        }
        showOrders(orders);
    }

    private void showOrders(List<TranslationOrder> orders) {
        for (TranslationOrder order : orders) {
            output.printf("ID: %d | %s | Клиент: %d | %s → %s | %s | %s руб. | Срок: %s%n",
                    order.getId(), order.getTitle(), order.getClientId(), order.getSourceLanguage(),
                    order.getTargetLanguage(), order.getStatus().getTitle(), order.getPrice().toPlainString(),
                    order.getDeadline().format(DATE_FORMAT));
        }
    }

    private void update() {
        TranslationOrder previous = orderService.getById(input.readId("ID заказа: "));
        showOrder(previous);
        if (previous.getStatus() == OrderStatus.COMPLETED || previous.getStatus() == OrderStatus.CANCELLED) {
            output.println("Ошибка: завершённый или отменённый заказ нельзя изменять.");
            return;
        }
        output.println("Введите новые данные полностью. Статус меняется отдельным пунктом меню.");
        orderService.update(readOrder(previous.getId(), previous.getStatus(), previous.getCreatedAt()));
        output.println("Заказ изменён.");
    }

    private void delete() {
        long id = input.readId("ID заказа: ");
        showOrder(orderService.getById(id));
        if (!input.readText("Удалить заказ? (да/нет): ").equalsIgnoreCase("да")) {
            output.println("Удаление отменено.");
            return;
        }
        orderService.delete(id);
        output.println("Заказ удалён.");
    }

    private void changeStatus() {
        long id = input.readId("ID заказа: ");
        showOrder(orderService.getById(id));
        orderService.changeStatus(id, readStatus("Новый статус: "));
        output.println("Статус изменён.");
    }

    private OrderStatus readStatus(String prompt) {
        OrderStatus[] statuses = OrderStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            output.println((i + 1) + ". " + statuses[i].getTitle());
        }
        while (true) {
            int choice = input.readNumber(prompt);
            if (choice >= 1 && choice <= statuses.length) {
                return statuses[choice - 1];
            }
            output.println("Ошибка: выберите статус от 1 до " + statuses.length + ".");
        }
    }

    private TranslationOrder readOrder(long id, OrderStatus status, LocalDateTime createdAt) {
        long clientId = input.readId("ID клиента: ");
        String title = input.readText("Название заказа: ");
        String description = input.readText("Описание (можно оставить пустым): ");
        String sourceLanguage = input.readText("Исходный язык: ");
        String targetLanguage = input.readText("Язык перевода: ");
        int wordCount = input.readNumber("Количество слов: ");
        BigDecimal price = input.readPrice("Стоимость в рублях: ");
        LocalDate deadline = input.readDate("Срок (ДД.ММ.ГГГГ): ");
        return new TranslationOrder(id, clientId, title, description, sourceLanguage, targetLanguage,
                wordCount, price, status, createdAt, deadline);
    }

    private void showOrder(TranslationOrder order) {
        output.println("ID заказа: " + order.getId());
        output.println("ID клиента: " + order.getClientId());
        output.println("Название: " + order.getTitle());
        output.println("Описание: " + (order.getDescription() == null ? "—" : order.getDescription()));
        output.println("Перевод: " + order.getSourceLanguage() + " → " + order.getTargetLanguage());
        output.println("Количество слов: " + order.getWordCount());
        output.println("Стоимость: " + order.getPrice().toPlainString() + " руб.");
        output.println("Статус: " + order.getStatus().getTitle());
        output.println("Создан: " + order.getCreatedAt().format(TIME_FORMAT));
        output.println("Срок: " + order.getDeadline().format(DATE_FORMAT));
    }
}
