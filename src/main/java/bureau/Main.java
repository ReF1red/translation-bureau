package bureau;

import bureau.ui.ConsoleInput;
import bureau.ui.ConsoleMenu;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            ConsoleInput input = new ConsoleInput(scanner, System.out);
            ConsoleMenu menu = new ConsoleMenu(input, System.out);
            menu.run();
        }
    }
}
