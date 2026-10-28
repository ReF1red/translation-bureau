package bureau.fx.controller;

import bureau.fx.util.FxDialogs;
import bureau.fx.util.FxTasks;
import bureau.model.Client;
import bureau.service.ClientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClientsController {
    private final ClientService clientService;
    private final ObservableList<Client> clients = FXCollections.observableArrayList();

    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, String> nameColumn;
    @FXML private TableColumn<Client, String> emailColumn;
    @FXML private TableColumn<Client, String> phoneColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label messageLabel;

    public ClientsController(ClientService clientService) {
        this.clientService = clientService;
    }

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        clientsTable.setItems(clients);
        clientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        clientsTable.setPlaceholder(new Label("Клиентов пока нет."));
        addButton.disableProperty().bind(refreshButton.disableProperty());
        editButton.disableProperty().bind(clientsTable.getSelectionModel().selectedItemProperty().isNull()
                .or(refreshButton.disableProperty()));
        deleteButton.disableProperty().bind(editButton.disableProperty());
        clientsTable.disableProperty().bind(refreshButton.disableProperty());
    }

    @FXML
    public void refresh() {
        FxTasks.load(refreshButton, messageLabel, clientService::getAll, loaded -> {
            clients.setAll(loaded);
            clientsTable.sort();
            messageLabel.setText("Клиентов: " + clients.size());
        });
    }

    @FXML
    private void add() {
        showForm(null);
    }

    @FXML
    private void edit() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showForm(selected);
        }
    }

    private void showForm(Client client) {
        ClientFormController controller = new ClientFormController(clientService, client, this::clientSaved);
        FxDialogs.showForm(clientsTable.getScene().getWindow(),
                client == null ? "Новый клиент" : "Редактирование клиента",
                "/bureau/fx/client-form.fxml", controller, controller::isSaving);
    }

    private void clientSaved(Client saved) {
        int index = -1;
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId() == saved.getId()) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            clients.set(index, saved);
        } else {
            clients.add(saved);
        }
        clientsTable.sort();
        clientsTable.getSelectionModel().select(saved);
        clientsTable.scrollTo(saved);
        messageLabel.setText("Клиент сохранён. Клиентов: " + clients.size());
    }

    @FXML
    private void delete() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (!FxDialogs.confirmDelete(clientsTable.getScene().getWindow(),
                "Удалить клиента «" + selected.getName() + "»?")) {
            return;
        }

        FxTasks.run(refreshButton, messageLabel, "Удаление клиента…", () -> {
            clientService.delete(selected.getId());
            return selected.getId();
        }, id -> {
            clients.removeIf(client -> client.getId() == id);
            messageLabel.setText("Клиент удалён. Клиентов: " + clients.size());
        });
    }
}
