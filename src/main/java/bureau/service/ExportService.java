package bureau.service;

import bureau.exception.ExportException;
import bureau.model.Client;
import bureau.model.TranslationOrder;
import bureau.repository.ClientRepository;
import bureau.repository.TranslationOrderRepository;
import bureau.util.ExcelExporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExportService {
    private final ClientRepository clientRepository;
    private final TranslationOrderRepository orderRepository;
    private final ExcelExporter exporter;

    public ExportService(ClientRepository clientRepository, TranslationOrderRepository orderRepository,
                         ExcelExporter exporter) {
        this.clientRepository = clientRepository;
        this.orderRepository = orderRepository;
        this.exporter = exporter;
    }

    public Path export(Path directory) {
        List<Client> clients = clientRepository.findAll();
        List<TranslationOrder> orders = orderRepository.findAll();
        try {
            return exporter.export(clients, orders, directory);
        } catch (IOException | SecurityException e) {
            throw new ExportException("Не удалось сохранить Excel-файл. Проверьте доступ к папке "
                    + directory.toAbsolutePath() + ".", e);
        } catch (IllegalArgumentException e) {
            throw new ExportException("Данные не помещаются в Excel: " + e.getMessage(), e);
        }
    }

    public Path exportOrders(List<TranslationOrder> orders, List<Client> clients, Path file) {
        Set<Long> clientIds = orders.stream().map(TranslationOrder::getClientId).collect(Collectors.toSet());
        List<Client> selectedClients = clients.stream().filter(client -> clientIds.contains(client.getId())).toList();
        try {
            return exporter.exportToFile(selectedClients, orders, file);
        } catch (IOException | SecurityException e) {
            throw new ExportException("Не удалось сохранить Excel-файл. Проверьте доступ к папке "
                    + "и убедитесь, что файл не открыт в другой программе.", e);
        } catch (IllegalArgumentException e) {
            throw new ExportException("Данные не помещаются в Excel: " + e.getMessage(), e);
        }
    }
}
