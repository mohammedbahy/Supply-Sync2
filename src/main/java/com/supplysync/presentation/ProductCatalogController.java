package com.supplysync.presentation;

import com.supplysync.models.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class ProductCatalogController extends BaseScreenController {
    @FXML
    private FlowPane productFlowPane;
    @FXML
    private Label userNameLabel;

    private String currentCategory = "All Categories";

    public void initialize() {
        renderProducts();
    }

    @Override
    public void setApplicationContext(com.supplysync.facade.ApplicationContext app) {
        super.setApplicationContext(app);
        if (auth() != null && auth().getCurrentUser() != null) {
            userNameLabel.setText(auth().getCurrentUser().getName());
        }
        renderProducts();
    }

    @FXML
    private void showHelp() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageManager.get("Support"));
        alert.setHeaderText(LanguageManager.get("Product Catalog Instructions"));
        alert.setContentText(LanguageManager.isArabic() ?
            "1. تصفح المنتجات حسب الفئة.\n2. اضغط على 'إضافة للطلب' لاختيار المنتج." :
            "1. Browse products by category.\n2. Click 'ADD TO ORDER' to select a product.");
        alert.showAndWait();
    }

    @FXML
    private void filterByCategory(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        currentCategory = btn.getText();
        renderProducts();
    }

    private void renderProducts() {
        if (productFlowPane == null || catalog() == null) return;

        productFlowPane.getChildren().clear();
        List<Product> products = catalog().getCatalog();

        for (Product product : products) {
            String translatedCat = LanguageManager.get(product.getCategory());
            boolean matchesCategory = currentCategory.equals("All Categories") || 
                                     currentCategory.equals("كل الفئات") || 
                                     product.getCategory().equals(currentCategory) ||
                                     translatedCat.equals(currentCategory);
            
            if (matchesCategory) {
                VBox card = createProductCard(product);
                productFlowPane.getChildren().add(card);
            }
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");

        String status = getStockStatus(product.getQuantity());
        Label stockLabel = new Label(LanguageManager.get(status));
        stockLabel.getStyleClass().add(status.equals("IN STOCK") ? "stock-green" : (status.equals("LOW STOCK") ? "stock-yellow" : "stock-gray"));

        Label metaLabel = new Label(LanguageManager.get(product.getCategory()) + "   ID: " + product.getId());
        metaLabel.getStyleClass().add("meta");

        Label titleLabel = new Label(product.getName());
        titleLabel.getStyleClass().add("product-title");

        HBox priceBox = new HBox();
        Label priceLabel = new Label("$" + String.format("%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("price");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        int warehouse = product.getQuantity();
        int remaining = catalog().availableUnitsToAddFromCatalog(product);
        Label unitsLabel = new Label(remaining + "/" + warehouse + " " + LanguageManager.get("Units"));
        unitsLabel.getStyleClass().add("units");
        priceBox.getChildren().addAll(priceLabel, spacer, unitsLabel);

        HBox actions = new HBox(6);
        // "View Details" button removed as requested in 212.txt

        javafx.scene.control.Spinner<Integer> qtySpinner = new javafx.scene.control.Spinner<>(0, Math.max(0, remaining), Math.min(1, Math.max(0, remaining)));
        qtySpinner.setPrefWidth(70);
        qtySpinner.getStyleClass().add("qty-spinner");
        qtySpinner.valueProperty().addListener((obs, o, n) -> {
            int max = catalog().availableUnitsToAddFromCatalog(product);
            if (n != null && n > max) {
                qtySpinner.getValueFactory().setValue(max);
            }
        });

        Button addBtn = new Button(remaining > 0 ? LanguageManager.get("ADD TO ORDER") : LanguageManager.get("OUT OF STOCK"));
        addBtn.getStyleClass().add("mini-btn");
        if (remaining > 0) {
            addBtn.getStyleClass().add("primary-mini");
            addBtn.setOnAction(e -> {
                int requestedQty = qtySpinner.getValue();
                int maxAdd = catalog().availableUnitsToAddFromCatalog(product);
                if (requestedQty <= 0) {
                    return;
                }
                if (requestedQty > maxAdd) {
                    javafx.scene.control.Alert warn = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                    warn.setHeaderText(null);
                    warn.setTitle(LanguageManager.get("Validation Error"));
                    warn.setContentText(LanguageManager.get("Stock limit"));
                    warn.showAndWait();
                    return;
                }
                for (int i = 0; i < requestedQty; i++) {
                    if (!catalog().addToCart(product)) {
                        break;
                    }
                }

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle(LanguageManager.get("Success"));
                alert.setHeaderText(null);
                alert.setContentText(LanguageManager.isArabic() ?
                        "تمت إضافة " + requestedQty + " من " + product.getName() + " إلى طلبك." :
                        "Added " + requestedQty + " of " + product.getName() + " to your order.");
                alert.showAndWait();

                renderProducts();
            });
        } else {
            addBtn.setDisable(true);
            qtySpinner.setDisable(true);
        }

        actions.getChildren().addAll(qtySpinner, addBtn);

        card.getChildren().addAll(stockLabel, metaLabel, titleLabel, priceBox, actions);
        return card;
    }

    private String getStockStatus(int qty) {
        if (qty == 0) return "OUT OF STOCK";
        if (qty < 100) return "LOW STOCK";
        return "IN STOCK";
    }
}
