package bureau.fx.controller;

import bureau.fx.util.FxTasks;
import bureau.model.Client;
import bureau.service.ClientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/bureau/fx/client-form.fxml"));
        loader.setControllerFactory(type -> controller);
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(clientsTable.getScene().getStylesheets());
            Stage dialog = new Stage();
            dialog.initOwner(clientsTable.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle(client == null ? "Новый клиент" : "Редактирование клиента");
            dialog.setResizable(false);
            dialog.setScene(scene);
            dialog.setOnCloseRequest(event -> {
                if (controller.isSaving()) {
                    event.consume();
                }
            });
            dialog.show();
        } catch (IOException e) {
            FxTasks.showError(clientsTable.getScene().getWindow(), e);
        }
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
        ButtonType delete = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить клиента «" + selected.getName() + "»?", delete, cancel);
        confirmation.initOwner(clientsTable.getScene().getWindow());
        confirmation.setTitle("Удаление клиента");
        confirmation.setHeaderText(null);
        if (confirmation.showAndWait().orElse(cancel) != delete) {
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
