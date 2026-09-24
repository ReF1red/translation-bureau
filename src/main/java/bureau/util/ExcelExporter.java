package bureau.util;

import bureau.model.Client;
import bureau.model.TranslationOrder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {
    public Path export(List<Client> clients, List<TranslationOrder> orders, Path directory) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            writeClients(workbook, clients, headerStyle);
            writeOrders(workbook, orders, headerStyle);

            Files.createDirectories(directory);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path file = Files.createTempFile(directory, "translation-bureau-" + timestamp + "-", ".xlsx");
            try (OutputStream output = Files.newOutputStream(file)) {
                workbook.write(output);
            } catch (IOException e) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException cleanupError) {
                    e.addSuppressed(cleanupError);
                }
                throw e;
            }
            return file.toAbsolutePath();
        }
    }

    private void writeClients(Workbook workbook, List<Client> clients, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Клиенты");
        writeHeader(sheet, headerStyle, "ID", "Имя", "Email", "Телефон");
        for (Client client : clients) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            writeText(row, 0, Long.toString(client.getId()));
            writeText(row, 1, client.getName());
            writeText(row, 2, client.getEmail());
            writeText(row, 3, client.getPhone());
        }
        setWidths(sheet, 22, 32, 36, 22);
    }

    private void writeOrders(Workbook workbook, List<TranslationOrder> orders, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Заказы");
        writeHeader(sheet, headerStyle, "ID", "ID клиента", "Название", "Описание", "Исходный язык",
                "Язык перевода", "Количество слов", "Стоимость", "Статус", "Дата создания", "Срок");
        CellStyle priceStyle = createFormat(workbook, "#,##0.00");
        CellStyle dateStyle = createFormat(workbook, "dd.mm.yyyy");
        CellStyle dateTimeStyle = createFormat(workbook, "dd.mm.yyyy hh:mm:ss");
        for (TranslationOrder order : orders) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            writeText(row, 0, Long.toString(order.getId()));
            writeText(row, 1, Long.toString(order.getClientId()));
            writeText(row, 2, order.getTitle());
            writeText(row, 3, order.getDescription());
            writeText(row, 4, order.getSourceLanguage());
            writeText(row, 5, order.getTargetLanguage());
            row.createCell(6).setCellValue(order.getWordCount());
            row.createCell(7).setCellValue(order.getPrice().doubleValue());
            row.getCell(7).setCellStyle(priceStyle);
            writeText(row, 8, order.getStatus().getTitle());
            row.createCell(9).setCellValue(order.getCreatedAt());
            row.getCell(9).setCellStyle(dateTimeStyle);
            row.createCell(10).setCellValue(order.getDeadline());
            row.getCell(10).setCellStyle(dateStyle);
        }
        setWidths(sheet, 22, 22, 38, 60, 20, 20, 20, 20, 18, 24, 15);
    }

    private void writeHeader(Sheet sheet, CellStyle style, String... titles) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            row.createCell(i).setCellValue(titles[i]);
            row.getCell(i).setCellStyle(style);
        }
        sheet.createFreezePane(0, 1);
    }

    private void writeText(Row row, int column, String value) {
        if (value != null && value.length() > 32767) {
            throw new IllegalArgumentException("в одной ячейке может быть не больше 32767 символов.");
        }
        row.createCell(column).setCellValue(value == null ? "" : value);
    }

    private CellStyle createFormat(Workbook workbook, String format) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        return style;
    }

    private void setWidths(Sheet sheet, int... widths) {
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }
}
