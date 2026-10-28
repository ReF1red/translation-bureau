package bureau.fx.controller;

import bureau.fx.util.FxDialogs;
import bureau.fx.util.FxTasks;
import bureau.model.Client;
import bureau.model.OrderStatus;
import bureau.model.TranslationOrder;
import bureau.service.ClientService;
import bureau.service.ExportService;
import bureau.service.TranslationOrderService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.math.BigDecimal;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class OrdersController {
    private final TranslationOrderService orderService;
    private final ClientService clientService;
    private final ExportService exportService;
    private final ObservableList<TranslationOrder> orders = FXCollections.observableArrayList();
    private final List<TranslationOrder> allOrders = new ArrayList<>();
    private final Map<Long, String> clientNames = new HashMap<>();
    private List<Client> clients = List.of();
    private boolean updatingFilters = true;

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
    @FXML private VBox filtersBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> clientFilter;
    @FXML private ComboBox<String> sortBox;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button statusButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Label messageLabel;

    public OrdersController(TranslationOrderService orderService, ClientService clientService,
                             ExportService exportService) {
        this.orderService = orderService;
        this.clientService = clientService;
        this.exportService = exportService;
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
        for (TableColumn<TranslationOrder, ?> column : ordersTable.getColumns()) {
            column.setSortable(false);
        }
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
        filtersBox.disableProperty().bind(refreshButton.disableProperty());
        exportButton.disableProperty().bind(refreshButton.disableProperty().or(Bindings.isEmpty(orders)));
        statusFilter.getItems().add("Все статусы");
        for (OrderStatus status : OrderStatus.values()) {
            statusFilter.getItems().add(status.getTitle());
        }
        statusFilter.getSelectionModel().selectFirst();
        clientFilter.getItems().add("Все клиенты");
        clientFilter.getSelectionModel().selectFirst();
        sortBox.getItems().setAll("Без сортировки", "Срок: по возрастанию", "Срок: по убыванию",
                "Стоимость: по возрастанию", "Стоимость: по убыванию");
        sortBox.getSelectionModel().selectFirst();
        searchField.textProperty().addListener((observable, previous, current) -> applyFilters());
        updatingFilters = false;
        applyFilters();
    }

    @FXML
    public void refresh() {
        FxTasks.load(refreshButton, messageLabel,
                () -> new OrderData(orderService.getAll(), clientService.getAll()), loaded -> {
                    Long selectedClientId = selectedClientId();
                    updatingFilters = true;
                    clients = loaded.clients;
                    clientNames.clear();
                    clientFilter.getItems().setAll("Все клиенты");
                    int selectedClientIndex = 0;
                    for (Client client : loaded.clients) {
                        clientNames.put(client.getId(), client.getName());
                        clientFilter.getItems().add(client.getName() + " (" + client.getEmail() + ")");
                        if (selectedClientId != null && client.getId() == selectedClientId) {
                            selectedClientIndex = clientFilter.getItems().size() - 1;
                        }
                    }
                    clientFilter.getSelectionModel().select(selectedClientIndex);
                    allOrders.clear();
                    allOrders.addAll(loaded.orders);
                    updatingFilters = false;
                    applyFilters();
                    ordersTable.refresh();
                });
    }

    private Long selectedClientId() {
        int index = clientFilter.getSelectionModel().getSelectedIndex();
        return index > 0 ? clients.get(index - 1).getId() : null;
    }

    @FXML
    private void applyFilters() {
        if (updatingFilters) {
            return;
        }
        TranslationOrder selected = ordersTable.getSelectionModel().getSelectedItem();
        int statusIndex = statusFilter.getSelectionModel().getSelectedIndex();
        OrderStatus status = statusIndex > 0 ? OrderStatus.values()[statusIndex - 1] : null;
        List<TranslationOrder> result = orderService.searchAndFilter(allOrders, searchField.getText(),
                status, selectedClientId());
        result = switch (sortBox.getSelectionModel().getSelectedIndex()) {
            case 1 -> orderService.sortByDeadline(result, true);
            case 2 -> orderService.sortByDeadline(result, false);
            case 3 -> orderService.sortByPrice(result, true);
            case 4 -> orderService.sortByPrice(result, false);
            default -> result;
        };
        orders.setAll(result);
        ordersTable.getSelectionModel().clearSelection();
        if (selected != null) {
            orders.stream().filter(order -> order.getId() == selected.getId()).findFirst()
                    .ifPresent(order -> ordersTable.getSelectionModel().select(order));
        }
        ordersTable.setPlaceholder(new Label(allOrders.isEmpty()
                ? "Заказов пока нет." : "Подходящих заказов нет."));
        messageLabel.setText("Показано заказов: " + orders.size() + " из " + allOrders.size());
    }

    @FXML
    private void resetFilters() {
        updatingFilters = true;
        searchField.clear();
        statusFilter.getSelectionModel().selectFirst();
        clientFilter.getSelectionModel().selectFirst();
        sortBox.getSelectionModel().selectFirst();
        updatingFilters = false;
        applyFilters();
    }

    @FXML
    private void exportOrders() {
        if (orders.isEmpty() || refreshButton.isDisabled()) {
            return;
        }
        Path file = chooseExportFile();
        if (file == null) {
            return;
        }
        List<TranslationOrder> visibleOrders = List.copyOf(orders);
        List<Client> loadedClients = List.copyOf(clients);
        FxTasks.run(refreshButton, messageLabel, "Сохранение Excel-файла…",
                () -> exportService.exportOrders(visibleOrders, loadedClients, file), saved ->
                        messageLabel.setText("Excel-файл сохранён: " + saved.getFileName()));
    }

    protected Path chooseExportFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Экспорт заказов в Excel");
        chooser.setInitialFileName("translation-orders.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        File selected = chooser.showSaveDialog(ordersTable.getScene().getWindow());
        if (selected == null) {
            return null;
        }
        Path file = selected.toPath();
        if (!selected.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            file = file.resolveSibling(file.getFileName() + ".xlsx");
            if (Files.exists(file) && !FxDialogs.confirmReplace(ordersTable.getScene().getWindow(),
                    file.getFileName().toString())) {
                return null;
            }
        }
        return file;
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
        for (int i = 0; i < allOrders.size(); i++) {
            if (allOrders.get(i).getId() == saved.getId()) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            allOrders.set(index, saved);
        } else {
            allOrders.add(saved);
        }
        applyFilters();
        if (orders.contains(saved)) {
            ordersTable.getSelectionModel().select(saved);
            ordersTable.scrollTo(saved);
            messageLabel.setText("Заказ сохранён. Показано заказов: " + orders.size() + " из " + allOrders.size());
        } else {
            ordersTable.getSelectionModel().clearSelection();
            messageLabel.setText("Заказ сохранён, но не соответствует текущим условиям поиска и фильтров.");
        }
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
            allOrders.removeIf(order -> order.getId() == id);
            applyFilters();
            messageLabel.setText("Заказ удалён. Показано заказов: " + orders.size() + " из " + allOrders.size());
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
