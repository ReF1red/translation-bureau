package bureau;

import bureau.config.ApplicationServices;
import bureau.ui.ClientMenu;
import bureau.ui.ConsoleInput;
import bureau.ui.ConsoleMenu;
import bureau.ui.OrderMenu;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.NoSuchElementException;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            ApplicationServices services = new ApplicationServices();
            ConsoleInput input = new ConsoleInput(scanner, System.out);
            ClientMenu clientMenu = new ClientMenu(input, System.out, services.getClientService());
            OrderMenu orderMenu = new OrderMenu(input, System.out, services.getOrderService());
            ConsoleMenu menu = new ConsoleMenu(input, System.out, services.getTableService(), clientMenu, orderMenu,
                    services.getStatisticsService(), services.getExportService());
            menu.run();
        } catch (NoSuchElementException e) {
            System.out.println("\nВвод завершён. До свидания!");
        }
    }
}
