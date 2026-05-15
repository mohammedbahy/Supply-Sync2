package com.supplysync.presentation;

import com.supplysync.models.Product;
import com.supplysync.presentation.auth.UserRegistrationValidator;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

final class CategoryPickerHelper {
    private static final String OTHER_EN = UserRegistrationValidator.OTHER_CATEGORY_LABEL;
    private static final String OTHER_AR = "أخرى";

    private CategoryPickerHelper() {
    }

    static VBox buildCategoryRow(List<Product> catalog, String initialCategory) {
        List<String> categories = catalog.stream()
                .map(Product::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(ArrayList::new));

        String otherLabel = LanguageManager.isArabic() ? OTHER_AR : OTHER_EN;
        if (!categories.contains(otherLabel)) {
            categories.add(otherLabel);
        }

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(categories);
        categoryCombo.setPrefWidth(220);

        TextField otherCategoryField = new TextField();
        otherCategoryField.setPromptText(LanguageManager.isArabic() ? "اسم القسم الجديد" : "New category name");
        otherCategoryField.setVisible(false);
        otherCategoryField.setManaged(false);

        String normalizedInitial = initialCategory != null ? initialCategory.trim() : "";
        if (!normalizedInitial.isEmpty()) {
            if (categories.contains(normalizedInitial)) {
                categoryCombo.setValue(normalizedInitial);
            } else {
                categoryCombo.setValue(otherLabel);
                otherCategoryField.setText(normalizedInitial);
                otherCategoryField.setVisible(true);
                otherCategoryField.setManaged(true);
            }
        } else if (!categories.isEmpty()) {
            categoryCombo.setValue(categories.get(0));
        }

        categoryCombo.valueProperty().addListener((obs, old, val) -> {
            boolean other = otherLabel.equals(val);
            otherCategoryField.setVisible(other);
            otherCategoryField.setManaged(other);
        });

        VBox box = new VBox(6, categoryCombo, otherCategoryField);
        box.setUserData(new CategoryInputs(categoryCombo, otherCategoryField, otherLabel));
        return box;
    }

    static void addCategoryRow(GridPane grid, int row, List<Product> catalog, String initialCategory) {
        VBox picker = buildCategoryRow(catalog, initialCategory);
        grid.add(new Label(LanguageManager.get("Category") + ":"), 0, row);
        grid.add(picker, 1, row);
    }

    static String resolveCategory(VBox categoryBox) {
        if (categoryBox == null || !(categoryBox.getUserData() instanceof CategoryInputs)) {
            return "";
        }
        CategoryInputs inputs = (CategoryInputs) categoryBox.getUserData();
        String selected = inputs.combo.getValue();
        if (inputs.otherLabel.equals(selected)) {
            return inputs.otherField.getText() != null ? inputs.otherField.getText().trim() : "";
        }
        return selected != null ? selected.trim() : "";
    }

    static VBox findCategoryBox(GridPane grid) {
        for (javafx.scene.Node node : grid.getChildren()) {
            if (node instanceof VBox && node.getUserData() instanceof CategoryInputs) {
                return (VBox) node;
            }
        }
        return null;
    }

    private static final class CategoryInputs {
        final ComboBox<String> combo;
        final TextField otherField;
        final String otherLabel;

        CategoryInputs(ComboBox<String> combo, TextField otherField, String otherLabel) {
            this.combo = combo;
            this.otherField = otherField;
            this.otherLabel = otherLabel;
        }
    }
}
