package bureau.fx.controller;

import bureau.exception.BusinessException;
import bureau.fx.util.FxTasks;
import bureau.model.Client;
import bureau.model.OrderStatus;
import bureau.model.TranslationOrder;
import bureau.service.TranslationOrderService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class OrderFormController {
    private final TranslationOrderService orderService;
    private final List<Client> clients;
    private final TranslationOrder original;
    private final Consumer<TranslationOrder> onSaved;

    @FXML private VBox fieldsBox;
    @FXML private ComboBox<Client> clientBox;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField sourceLanguageField;
    @FXML private TextField targetLanguageField;
    @FXML private TextField wordCountField;
    @FXML private TextField priceField;
    @FXML private DatePicker deadlinePicker;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    public OrderFormController(TranslationOrderService orderService, List<Client> clients,
                               TranslationOrder original, Consumer<TranslationOrder> onSaved) {
        this.orderService = orderService;
        this.clients = clients;
        this.original = original;
        this.onSaved = onSaved;
    }

    @FXML
    private void initialize() {
        clientBox.setItems(FXCollections.observableArrayList(clients));
        clientBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Client client) {
                return client == null ? "" : client.getName() + " (" + client.getEmail() + ")";
            }

            @Override
            public Client fromString(String text) {
                return null;
            }
        });
        deadlinePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.uuuu");

            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(format);
            }

            @Override
            public LocalDate fromString(String text) {
                return text == null || text.isBlank() ? null : LocalDate.parse(text, format);
            }
        });
        if (original == null) {
            deadlinePicker.setValue(LocalDate.now().plusDays(3));
            statusLabel.setText(OrderStatus.NEW.getTitle());
        } else {
            clientBox.setValue(clients.stream().filter(client -> client.getId() == original.getClientId())
                    .findFirst().orElse(null));
            titleField.setText(original.getTitle());
            descriptionField.setText(original.getDescription());
            sourceLanguageField.setText(original.getSourceLanguage());
            targetLanguageField.setText(original.getTargetLanguage());
            wordCountField.setText(Integer.toString(original.getWordCount()));
            priceField.setText(original.getPrice().toPlainString());
            deadlinePicker.setValue(original.getDeadline());
            statusLabel.setText(original.getStatus().getTitle());
        }
        if (clients.isEmpty()) {
            messageLabel.setText("Список клиентов пуст. Сначала добавьте клиента.");
        }
        fieldsBox.disableProperty().bind(saveButton.disableProperty());
        cancelButton.disableProperty().bind(saveButton.disableProperty());
    }

    @FXML
    private void save() {
        try {
            Client client = clientBox.getValue();
            if (client == null) {
                throw new BusinessException("Выберите клиента из списка.");
            }
            int wordCount;
            try {
                wordCount = Integer.parseInt(wordCountField.getText().trim());
            } catch (NumberFormatException e) {
                throw new BusinessException("Количество слов должно быть целым числом от 1 до 2147483647.");
            }
            BigDecimal price;
            try {
                price = new BigDecimal(priceField.getText().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                throw new BusinessException("Стоимость должна быть числом, например 1500,50.");
            }
            long id = original == null ? 0 : original.getId();
            TranslationOrder order = new TranslationOrder(id, client.getId(), titleField.getText(),
                    descriptionField.getText(), sourceLanguageField.getText(), targetLanguageField.getText(),
                    wordCount, price, original == null ? OrderStatus.NEW : original.getStatus(),
                    original == null ? null : original.getCreatedAt(), deadlinePicker.getValue());
            FxTasks.run(saveButton, messageLabel, "Сохранение заказа…", () -> {
                if (id == 0) {
                    return orderService.create(order);
                }
                orderService.update(order);
                return order;
            }, saved -> {
                close();
                onSaved.accept(saved);
            });
        } catch (BusinessException e) {
            messageLabel.setText("Проверьте данные формы.");
            FxTasks.showError(saveButton.getScene().getWindow(), e);
        }
    }

    public boolean isSaving() {
        return saveButton.isDisabled();
    }

    @FXML
    private void close() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}
