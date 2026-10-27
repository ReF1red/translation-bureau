package bureau.fx.controller;

import bureau.fx.util.FxTasks;
import bureau.model.Client;
import bureau.service.ClientService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ClientFormController {
    private final ClientService clientService;
    private final Client original;
    private final Consumer<Client> onSaved;

    @FXML private VBox fieldsBox;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label messageLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    public ClientFormController(ClientService clientService, Client original, Consumer<Client> onSaved) {
        this.clientService = clientService;
        this.original = original;
        this.onSaved = onSaved;
    }

    @FXML
    private void initialize() {
        if (original != null) {
            nameField.setText(original.getName());
            emailField.setText(original.getEmail());
            phoneField.setText(original.getPhone());
        }
        fieldsBox.disableProperty().bind(saveButton.disableProperty());
        cancelButton.disableProperty().bind(saveButton.disableProperty());
    }

    @FXML
    private void save() {
        long id = original == null ? 0 : original.getId();
        Client client = new Client(id, nameField.getText(), emailField.getText(), phoneField.getText());
        FxTasks.run(saveButton, messageLabel, "Сохранение клиента…", () -> {
            if (id == 0) {
                return clientService.create(client);
            }
            clientService.update(client);
            return client;
        }, saved -> {
            close();
            onSaved.accept(saved);
        });
    }

    public boolean isSaving() {
        return saveButton.isDisabled();
    }

    @FXML
    private void close() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}
