package bureau.fx.util;

import bureau.exception.BusinessException;
import bureau.exception.DatabaseException;
import bureau.exception.ExportException;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Window;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class FxTasks {
    public static <T> void load(Button refreshButton, Label messageLabel,
                               Callable<T> work, Consumer<T> onSuccess) {
        run(refreshButton, messageLabel, "Загрузка данных…", work, onSuccess);
    }

    public static <T> void run(Button actionButton, Label messageLabel, String progressText,
                              Callable<T> work, Consumer<T> onSuccess) {
        if (actionButton.isDisabled()) {
            return;
        }
        actionButton.setDisable(true);
        messageLabel.setText(progressText);

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(event -> {
            actionButton.setDisable(false);
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            actionButton.setDisable(false);
            messageLabel.setText("Не удалось выполнить операцию.");
            showError(actionButton.getScene().getWindow(), task.getException());
        });

        Thread thread = new Thread(task, "bureau-data");
        thread.setDaemon(true);
        thread.start();
    }

    public static void showError(Window owner, Throwable error) {
        System.getLogger(FxTasks.class.getName()).log(System.Logger.Level.WARNING,
                "Ошибка выполнения операции", error);
        String message = error instanceof DatabaseException || error instanceof BusinessException
                || error instanceof ExportException
                ? error.getMessage() : "Не удалось выполнить операцию. Повторите попытку.";
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
