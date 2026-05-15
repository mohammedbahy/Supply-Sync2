package com.supplysync.presentation;

import com.supplysync.models.Product;
import com.supplysync.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.util.List;

public class ProductsManagementController extends BaseScreenController {
    @FXML
    private VBox productsTable;
    @FXML
    private Label productsTitle;
    @FXML
    private Label productsSubtitle;
    @FXML
    private Button addProductBtn;

    public void initialize() {
        if (orderFacade != null) {
            renderProducts();
        }
    }

    @Override
    public void setOrderFacade(com.supplysync.facade.OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
        renderProducts();
    }

    @Override
    protected void applyLanguage() {
        super.applyLanguage();
        if (productsTitle != null) {
            productsTitle.setText(LanguageManager.get("Products Management"));
        }
        if (productsSubtitle != null) {
            productsSubtitle.setText(LanguageManager.get("Manage your wholesale inventory, stock levels, and unit pricing."));
        }
        if (addProductBtn != null) {
            addProductBtn.setText(LanguageManager.get("Add Product"));
        }
    }

    private boolean isAdmin() {
        User u = orderFacade != null ? orderFacade.getCurrentUser() : null;
        return u != null && "ADMIN".equalsIgnoreCase(u.getRole());
    }

    private void renderProducts() {
        if (productsTable == null || orderFacade == null) {
            return;
        }

        if (productsTable.getChildren().size() > 2) {
            javafx.scene.Node header = productsTable.getChildren().get(0);
            javafx.scene.Node sep = productsTable.getChildren().get(1);
            productsTable.getChildren().clear();
            productsTable.getChildren().addAll(header, sep);
        }

        List<Product> products = orderFacade.getCatalog();
        for (Product product : products) {
            productsTable.getChildren().add(createProductRow(product));
        }
    }

    private HBox createProductRow(Product p) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");

        Label idLabel = new Label(p.getId());
        idLabel.getStyleClass().addAll("col-id", "body-cell");

        Label nameLabel = new Label(p.getName());
        nameLabel.getStyleClass().addAll("col-name", "body-cell");

        Label catLabel = new Label(LanguageManager.get(p.getCategory()));
        catLabel.getStyleClass().addAll("col-category", "body-cell");

        Label qtyLabel = new Label(p.getQuantity() + " units");
        qtyLabel.getStyleClass().addAll("col-qty", "body-cell");

        Label priceLabel = new Label("$" + String.format("%.2f", p.getPrice()));
        priceLabel.getStyleClass().addAll("col-price", "body-cell");

        String status = getStockStatus(p.getQuantity());
        Label statusLabel = new Label(LanguageManager.get(status));
        statusLabel.getStyleClass().add(status.equals("IN STOCK") ? "tag-green" : (status.equals("LOW STOCK") ? "tag-blue" : "tag-red"));

        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);

        Button editBtn = new Button("✎");
        editBtn.getStyleClass().add("icon-btn");
        editBtn.setMinWidth(36);
        editBtn.setOnAction(e -> {
            if (!isAdmin()) {
                showAlert(Alert.AlertType.WARNING, "Access denied", "Only administrators can edit products.");
                return;
            }
            openEditProductDialog(p);
        });

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("icon-btn");
        deleteBtn.setOnAction(e -> {
            if (!isAdmin()) {
                showAlert(Alert.AlertType.WARNING, "Access denied", "Only administrators can delete products.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete product");
            confirm.setHeaderText(null);
            confirm.setContentText("Delete " + p.getName() + " (" + p.getId() + ")?");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    orderFacade.deleteProduct(p.getId());
                    renderProducts();
                }
            });
        });

        HBox actions = new HBox(6, editBtn, deleteBtn);
        actions.getStyleClass().add("col-actions");

        row.getChildren().addAll(idLabel, nameLabel, catLabel, qtyLabel, priceLabel, statusLabel, gap, actions);
        return row;
    }

    @FXML
    private void handleAddProduct() {
        if (!isAdmin()) {
            showAlert(Alert.AlertType.WARNING, "Access denied", "Only administrators can add products.");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.get("Add Product"));
        dialog.setHeaderText(LanguageManager.isArabic() ? "أدخل بيانات المنتج" : "Enter product details");

        ButtonType saveButtonType = new ButtonType(LanguageManager.isArabic() ? "حفظ" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField qtyField = new TextField();
        TextField priceField = new TextField();

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        CategoryPickerHelper.addCategoryRow(grid, 2, orderFacade.getCatalog(), null);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(qtyField, 1, 3);
        grid.add(new Label("Price:"), 0, 4);
        grid.add(priceField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != saveButtonType) {
                return null;
            }
            String id = idField.getText() != null ? idField.getText().trim() : "";
            if (id.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation", "Product ID is required.");
                return null;
            }
            if (orderFacade.getCatalog().stream().anyMatch(x -> x.getId().equals(id))) {
                showAlert(Alert.AlertType.ERROR, "Error", "ID already exists.");
                return null;
            }
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                String name = nameField.getText() != null ? nameField.getText().trim() : "";
                String cat = CategoryPickerHelper.resolveCategory(CategoryPickerHelper.findCategoryBox(grid));
                if (name.isEmpty() || cat.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation",
                            LanguageManager.isArabic() ? "الاسم والقسم مطلوبان." : "Name and category are required.");
                    return null;
                }
                return new Product(id, name, qty, cat, price, "");
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Validation", "Quantity and price must be valid numbers.");
                return null;
            }
        });

        dialog.showAndWait().ifPresent(product -> {
            orderFacade.saveProduct(product);
            renderProducts();
            showAlert(Alert.AlertType.INFORMATION, "Success", LanguageManager.isArabic() ? "تم حفظ المنتج." : "Product saved to the database.");
        });
    }

    private void openEditProductDialog(Product selected) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.get("Edit Product"));
        dialog.setHeaderText(selected.getId() + " — " + selected.getName());

        ButtonType saveButtonType = new ButtonType(LanguageManager.isArabic() ? "حفظ" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField idDisplay = new TextField(selected.getId());
        idDisplay.setEditable(false);

        TextField nameField = new TextField(selected.getName());
        TextField qtyField = new TextField(String.valueOf(selected.getQuantity()));
        TextField priceField = new TextField(String.valueOf(selected.getPrice()));

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idDisplay, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        CategoryPickerHelper.addCategoryRow(grid, 2, orderFacade.getCatalog(), selected.getCategory());
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(qtyField, 1, 3);
        grid.add(new Label("Price:"), 0, 4);
        grid.add(priceField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != saveButtonType) {
                return null;
            }
            try {
                String cat = CategoryPickerHelper.resolveCategory(CategoryPickerHelper.findCategoryBox(grid));
                Product copy = new Product(
                        selected.getId(),
                        nameField.getText().trim(),
                        Integer.parseInt(qtyField.getText().trim()),
                        cat,
                        Double.parseDouble(priceField.getText().trim()),
                        selected.getImagePath() != null ? selected.getImagePath() : "");
                if (copy.getName().isEmpty() || copy.getCategory().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation",
                            LanguageManager.isArabic() ? "الاسم والقسم مطلوبان." : "Name and category are required.");
                    return null;
                }
                return copy;
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Validation", "Quantity and price must be valid numbers.");
                return null;
            }
        });

        dialog.showAndWait().ifPresent(orderFacade::saveProduct);
        renderProducts();
    }

    private String getStockStatus(int qty) {
        if (qty == 0) {
            return "OUT OF STOCK";
        }
        if (qty < 100) {
            return "LOW STOCK";
        }
        return "IN STOCK";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
