package bureau.fx.controller;

import bureau.fx.util.FxTasks;
import bureau.model.Client;
import bureau.model.OrderStatus;
import bureau.model.TranslationOrder;
import bureau.service.ClientService;
import bureau.service.TranslationOrderService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrdersController {
    private final TranslationOrderService orderService;
    private final ClientService clientService;
    private final ObservableList<TranslationOrder> orders = FXCollections.observableArrayList();
    private final Map<Long, String> clientNames = new HashMap<>();

    @FXML private TableView<TranslationOrder> ordersTable;
    @FXML private TableColumn<TranslationOrder, String> titleColumn;
    @FXML private TableColumn<TranslationOrder, String> clientColumn;
    @FXML private TableColumn<TranslationOrder, String> sourceLanguageColumn;
    @FXML private TableColumn<TranslationOrder, String> targetLanguageColumn;
    @FXML private TableColumn<TranslationOrder, Integer> wordCountColumn;
    @FXML private TableColumn<TranslationOrder, BigDecimal> priceColumn;
    @FXML private TableColumn<TranslationOrder, OrderStatus> statusColumn;
    @FXML private TableColumn<TranslationOrder, LocalDateTime> createdAtColumn;
    @FXML private TableColumn<TranslationOrder, LocalDate> deadlineColumn;
    @FXML private TextArea descriptionArea;
    @FXML private Button refreshButton;
    @FXML private Label messageLabel;

    public OrdersController(TranslationOrderService orderService, ClientService clientService) {
        this.orderService = orderService;
        this.clientService = clientService;
    }

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        clientColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                clientNames.getOrDefault(cell.getValue().getClientId(), "Клиент не найден")));
        sourceLanguageColumn.setCellValueFactory(new PropertyValueFactory<>("sourceLanguage"));
        targetLanguageColumn.setCellValueFactory(new PropertyValueFactory<>("targetLanguage"));
        wordCountColumn.setCellValueFactory(new PropertyValueFactory<>("wordCount"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        priceColumn.setCellFactory(column -> new TableCell<>() {
            private final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("ru-RU"));

            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : format.format(value));
            }
        });
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(OrderStatus value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.getTitle());
            }
        });
        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.format(format));
            }
        });
        deadlineColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            @Override
            protected void updateItem(LocalDate value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.format(format));
            }
        });
        ordersTable.setItems(orders);
        ordersTable.setPlaceholder(new Label("Заказов пока нет."));
        ordersTable.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) ->
                descriptionArea.setText(selected == null || selected.getDescription() == null
                        ? "" : selected.getDescription()));
    }

    @FXML
    public void refresh() {
        FxTasks.load(refreshButton, messageLabel,
                () -> new OrderData(orderService.getAll(), clientService.getAll()), loaded -> {
                    clientNames.clear();
                    for (Client client : loaded.clients) {
                        clientNames.put(client.getId(), client.getName());
                    }
                    orders.setAll(loaded.orders);
                    ordersTable.refresh();
                    messageLabel.setText("Заказов: " + orders.size());
                });
    }

    private static class OrderData {
        private final List<TranslationOrder> orders;
        private final List<Client> clients;

        private OrderData(List<TranslationOrder> orders, List<Client> clients) {
            this.orders = orders;
            this.clients = clients;
        }
    }
}
