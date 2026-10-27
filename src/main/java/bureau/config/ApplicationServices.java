package bureau.config;

import bureau.repository.ClientRepository;
import bureau.repository.JdbcClientRepository;
import bureau.repository.JdbcTableRepository;
import bureau.repository.JdbcTranslationOrderRepository;
import bureau.repository.TableRepository;
import bureau.repository.TranslationOrderRepository;
import bureau.service.ClientService;
import bureau.service.ExportService;
import bureau.service.StatisticsService;
import bureau.service.TableService;
import bureau.service.TranslationOrderService;
import bureau.util.DatabaseManager;
import bureau.util.ExcelExporter;

public class ApplicationServices {
    private final ClientService clientService;
    private final TranslationOrderService orderService;
    private final StatisticsService statisticsService;
    private final ExportService exportService;
    private final TableService tableService;

    public ApplicationServices() {
        DatabaseManager databaseManager = new DatabaseManager();
        ClientRepository clientRepository = new JdbcClientRepository(databaseManager);
        TranslationOrderRepository orderRepository = new JdbcTranslationOrderRepository(databaseManager);
        TableRepository tableRepository = new JdbcTableRepository(databaseManager);

        clientService = new ClientService(clientRepository);
        orderService = new TranslationOrderService(orderRepository, clientService);
        statisticsService = new StatisticsService(clientRepository, orderRepository);
        exportService = new ExportService(clientRepository, orderRepository, new ExcelExporter());
        tableService = new TableService(tableRepository);
    }

    public ClientService getClientService() {
        return clientService;
    }

    public TranslationOrderService getOrderService() {
        return orderService;
    }

    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    public ExportService getExportService() {
        return exportService;
    }

    public TableService getTableService() {
        return tableService;
    }
}
