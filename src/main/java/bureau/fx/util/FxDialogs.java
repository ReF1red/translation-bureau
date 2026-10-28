package bureau.fx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.function.BooleanSupplier;

public class FxDialogs {
    public static void showForm(Window owner, String title, String resource,
                                Object controller, BooleanSupplier isSaving) {
        FXMLLoader loader = new FXMLLoader(FxDialogs.class.getResource(resource));
        loader.setControllerFactory(type -> controller);
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(owner.getScene().getStylesheets());
            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle(title);
            dialog.setResizable(false);
            dialog.setScene(scene);
            dialog.setOnCloseRequest(event -> {
                if (isSaving.getAsBoolean()) {
                    event.consume();
                }
            });
            dialog.show();
        } catch (IOException e) {
            FxTasks.showError(owner, e);
        }
    }

    public static boolean confirmDelete(Window owner, String message) {
        return confirm(owner, "Подтверждение удаления", message, "Удалить");
    }

    public static boolean confirmReplace(Window owner, String fileName) {
        return confirm(owner, "Сохранение файла", "Файл «" + fileName + "» уже существует. Заменить его?", "Заменить");
    }

    private static boolean confirm(Window owner, String title, String message, String actionText) {
        ButtonType action = new ButtonType(actionText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, message, action, cancel);
        confirmation.initOwner(owner);
        confirmation.setTitle(title);
        confirmation.setHeaderText(null);
        return confirmation.showAndWait().orElse(cancel) == action;
    }
}
