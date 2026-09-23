package bureau.ui;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.Scanner;

public class ConsoleInput {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
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

    public String readText(String prompt) {
        output.print(prompt);
        output.flush();
        if (!scanner.hasNextLine()) {
            throw new NoSuchElementException("Ввод завершён.");
        }
        return scanner.nextLine().trim();
    }

    public int readNumber(String prompt) {
        OptionalInt value = readInt(prompt);
        if (value.isEmpty()) {
            throw new NoSuchElementException("Ввод завершён.");
        }
        return value.getAsInt();
    }

    public long readId(String prompt) {
        while (true) {
            try {
                long id = Long.parseLong(readText(prompt));
                if (id > 0) {
                    return id;
                }
                output.println("Ошибка: ID должен быть положительным.");
            } catch (NumberFormatException e) {
                output.println("Ошибка: ID должен быть целым числом допустимого размера.");
            }
        }
    }

    public BigDecimal readPrice(String prompt) {
        while (true) {
            try {
                return new BigDecimal(readText(prompt).replace(',', '.'));
            } catch (NumberFormatException e) {
                output.println("Ошибка: введите стоимость числом, например 1500.50.");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readText(prompt), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                output.println("Ошибка: введите существующую дату в формате ДД.ММ.ГГГГ.");
            }
        }
    }
}
