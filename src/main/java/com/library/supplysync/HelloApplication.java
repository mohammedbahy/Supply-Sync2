package com.library.supplysync;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.supplysync.facade.OrderFacade;
import com.supplysync.patterns.creational.factory.ServiceFactory;
import com.supplysync.presentation.BaseScreenController;
import com.supplysync.presentation.ScreenNavigator;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        OrderFacade orderFacade = new OrderFacade(
                ServiceFactory.createOrderService(),
                ServiceFactory.createInventoryService(),
                ServiceFactory.createDeliveryService(),
                ServiceFactory.createNotificationService(),
                ServiceFactory.createAuthService(),
                ServiceFactory.getStorage()
        );
        ScreenNavigator.setOrderFacade(orderFacade);

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/supplysync/presentation/login-view.fxml"));
        Parent root = fxmlLoader.load();
        
        Object controller = fxmlLoader.getController();
        if (controller instanceof BaseScreenController) {
            ((BaseScreenController) controller).setOrderFacade(orderFacade);
        }

        Scene scene = new Scene(root, 1200, 760);
        stage.setTitle("SupplySync Login");
        stage.setResizable(true);
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}