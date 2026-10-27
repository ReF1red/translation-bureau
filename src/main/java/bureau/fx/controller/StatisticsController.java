package bureau.fx.controller;

import bureau.fx.util.FxTasks;
import bureau.service.StatisticsService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class StatisticsController {
    private final StatisticsService statisticsService;

    @FXML private Label clientsCount;
    @FXML private Label ordersCount;
    @FXML private Label newCount;
    @FXML private Label inProgressCount;
    @FXML private Label completedCount;
    @FXML private Label cancelledCount;
    @FXML private Button refreshButton;
    @FXML private Label messageLabel;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @FXML
    public void refresh() {
        FxTasks.load(refreshButton, messageLabel, statisticsService::getStatistics, statistics -> {
            clientsCount.setText(statistics.get("Всего клиентов").toString());
            ordersCount.setText(statistics.get("Всего заказов").toString());
            newCount.setText(statistics.get("Новых").toString());
            inProgressCount.setText(statistics.get("В работе").toString());
            completedCount.setText(statistics.get("Завершённых").toString());
            cancelledCount.setText(statistics.get("Отменённых").toString());
            messageLabel.setText("Данные обновлены.");
        });
    }
}
