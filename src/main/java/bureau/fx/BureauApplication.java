package bureau.fx;

import bureau.config.ApplicationServices;
import bureau.fx.controller.ClientsController;
import bureau.fx.controller.MainController;
import bureau.fx.controller.OrdersController;
import bureau.fx.controller.StatisticsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BureauApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        ApplicationServices services = new ApplicationServices();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/bureau/fx/main.fxml"));
        loader.setControllerFactory(type -> {
            if (type == MainController.class) {
                return new MainController();
            }
            if (type == ClientsController.class) {
                return new ClientsController(services.getClientService());
            }
            if (type == OrdersController.class) {
                return new OrdersController(services.getOrderService(), services.getClientService());
            }
            if (type == StatisticsController.class) {
                return new StatisticsController(services.getStatisticsService());
            }
            throw new IllegalArgumentException("Неизвестный контроллер: " + type.getName());
        });

        Parent root = loader.load();
        Scene scene = new Scene(root, 1180, 720);
        scene.getStylesheets().add(getClass().getResource("/bureau/fx/app.css").toExternalForm());
        stage.setTitle("Бюро переводов");
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }
}
