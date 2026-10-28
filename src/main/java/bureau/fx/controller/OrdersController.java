package bureau.fx.controller;

import bureau.fx.util.FxDialogs;
import bureau.fx.util.FxTasks;
import bureau.model.Client;
import bureau.model.OrderStatus;
import bureau.model.TranslationOrder;
import bureau.service.ClientService;
import bureau.service.TranslationOrderService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
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
import java.util.Optional;

public class OrdersController {
    private final TranslationOrderService orderService;
    private final ClientService clientService;
    private final ObservableList<TranslationOrder> orders = FXCollections.observableArrayList();
    private final Map<Long, String> clientNames = new HashMap<>();
    private List<Client> clients = List.of();

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
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button statusButton;
    @FXML private Button deleteButton;
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
        addButton.disableProperty().bind(refreshButton.disableProperty());
        editButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            TranslationOrder selected = ordersTable.getSelectionModel().getSelectedItem();
            return refreshButton.isDisabled() || selected == null
                    || orderService.getAllowedNextStatuses(selected.getStatus()).isEmpty();
        }, ordersTable.getSelectionModel().selectedItemProperty(), refreshButton.disableProperty()));
        editButton.setTooltip(new Tooltip("Завершённые и отменённые заказы нельзя изменять."));
        statusButton.disableProperty().bind(editButton.disableProperty());
        deleteButton.disableProperty().bind(ordersTable.getSelectionModel().selectedItemProperty().isNull()
                .or(refreshButton.disableProperty()));
        ordersTable.disableProperty().bind(refreshButton.disableProperty());
    }

    @FXML
    public void refresh() {
        FxTasks.load(refreshButton, messageLabel,
                () -> new OrderData(orderService.getAll(), clientService.getAll()), loaded -> {
                    clients = loaded.clients;
                    clientNames.clear();
                    for (Client client : loaded.clients) {
                        clientNames.put(client.getId(), client.getName());
                    }
                    orders.setAll(loaded.orders);
                    ordersTable.sort();
                    ordersTable.refresh();
                    messageLabel.setText("Заказов: " + orders.size());
                });
    }

    @FXML
    private void add() {
        showForm(null);
    }

    @FXML
    private void edit() {
        TranslationOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showForm(selected);
        }
    }

    private void showForm(TranslationOrder order) {
        OrderFormController controller = new OrderFormController(orderService, clients, order, this::orderSaved);
        FxDialogs.showForm(ordersTable.getScene().getWindow(),
                order == null ? "Новый заказ" : "Редактирование заказа",
                "/bureau/fx/order-form.fxml", controller, controller::isSaving);
    }

    private void orderSaved(TranslationOrder saved) {
        int index = -1;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId() == saved.getId()) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            orders.set(index, saved);
        } else {
            orders.add(saved);
        }
        ordersTable.sort();
        ordersTable.getSelectionModel().select(saved);
        ordersTable.scrollTo(saved);
        descriptionArea.setText(saved.getDescription() == null ? "" : saved.getDescription());
        messageLabel.setText("Заказ сохранён. Заказов: " + orders.size());
    }

    @FXML
    private void changeStatus() {
        TranslationOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        List<OrderStatus> allowed = orderService.getAllowedNextStatuses(selected.getStatus());
        if (allowed.isEmpty()) {
            return;
        }
        List<String> titles = allowed.stream().map(OrderStatus::getTitle).toList();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(titles.getFirst(), titles);
        dialog.initOwner(ordersTable.getScene().getWindow());
        dialog.setTitle("Изменение статуса");
        dialog.setHeaderText("Заказ «" + selected.getTitle() + "»");
        dialog.setContentText("Новый статус:");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Сохранить");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Отмена");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        OrderStatus status = allowed.stream().filter(value -> value.getTitle().equals(result.get()))
                .findFirst().orElseThrow();
        FxTasks.run(refreshButton, messageLabel, "Изменение статуса…",
                () -> orderService.changeStatus(selected.getId(), status), this::orderSaved);
    }

    @FXML
    private void delete() {
        TranslationOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null || !FxDialogs.confirmDelete(ordersTable.getScene().getWindow(),
                "Удалить заказ «" + selected.getTitle() + "»?")) {
            return;
        }
        FxTasks.run(refreshButton, messageLabel, "Удаление заказа…", () -> {
            orderService.delete(selected.getId());
            return selected.getId();
        }, id -> {
            orders.removeIf(order -> order.getId() == id);
            messageLabel.setText("Заказ удалён. Заказов: " + orders.size());
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
