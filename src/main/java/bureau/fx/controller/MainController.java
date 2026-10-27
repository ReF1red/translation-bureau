package bureau.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

public class MainController {
    @FXML private TabPane tabs;
    @FXML private OrdersController ordersViewController;
    @FXML private ClientsController clientsViewController;
    @FXML private StatisticsController statisticsViewController;

    @FXML
    private void initialize() {
        tabs.getSelectionModel().selectedIndexProperty().addListener((observable, previous, selected) ->
                refreshTab(selected.intValue()));
        refreshTab(0);
    }

    private void refreshTab(int index) {
        switch (index) {
            case 0 -> ordersViewController.refresh();
            case 1 -> clientsViewController.refresh();
            case 2 -> statisticsViewController.refresh();
        }
    }
}
