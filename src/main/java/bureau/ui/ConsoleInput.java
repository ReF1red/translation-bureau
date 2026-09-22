package bureau.ui;

import java.io.PrintStream;
import java.util.OptionalInt;
import java.util.Scanner;

public class ConsoleInput {
    private final Scanner scanner;
    private final PrintStream output;

    public ConsoleInput(Scanner scanner, PrintStream output) {
        this.scanner = scanner;
        this.output = output;
    }

    public OptionalInt readInt(String prompt) {
        while (true) {
            output.print(prompt);
            output.flush();

            if (!scanner.hasNextLine()) {
                return OptionalInt.empty();
            }

            String value = scanner.nextLine().trim();
            try {
                return OptionalInt.of(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                output.println("Ошибка: введите целое число.");
            }
        }
    }
}
