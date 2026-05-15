package com.supplysync.presentation;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.supplysync.facade.ApplicationContext;

public final class ScreenNavigator {
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 760;
    private static ApplicationContext applicationContext;

    private ScreenNavigator() {
    }

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static void open(Event event, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(ScreenNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof BaseScreenController) {
            ((BaseScreenController) controller).setApplicationContext(applicationContext);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = stage.getScene() != null ? stage.getScene().getWidth() : DEFAULT_WIDTH;
        double height = stage.getScene() != null ? stage.getScene().getHeight() : DEFAULT_HEIGHT;
        stage.setTitle(title);
        stage.setScene(new Scene(root, width, height));
        stage.setResizable(true);
        stage.show();
    }
}
