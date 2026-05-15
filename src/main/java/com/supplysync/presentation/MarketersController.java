package com.supplysync.presentation;

import com.supplysync.models.Marketer;
import com.supplysync.models.User;
import com.supplysync.presentation.auth.PasswordRevealSupport;
import com.supplysync.presentation.auth.UserRegistrationValidator;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;

public class MarketersController extends BaseScreenController {
    @FXML private VBox marketersTable;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private Button editMarketerBtn;
    @FXML private Button addMarketerBtn;

    private User selectedUser;

    @FXML
    public void initialize() {
        if (orderFacade != null) {
            renderMarketers();
        }
    }

    @Override
    public void setOrderFacade(com.supplysync.facade.OrderFacade orderFacade) {
        super.setOrderFacade(orderFacade);
        renderMarketers();
    }

    @Override
    protected void applyLanguage() {
        super.applyLanguage();
        if (pageTitle != null) {
            pageTitle.setText(LanguageManager.isArabic() ? "المسوقون" : "Marketers");
        }
        if (pageSubtitle != null) {
            pageSubtitle.setText(LanguageManager.isArabic()
                    ? "إضافة وتعديل حسابات المسوقين (نفس شروط التسجيل)."
                    : "Add and manage marketer accounts (same rules as registration).");
        }
        if (editMarketerBtn != null) {
            editMarketerBtn.setText(LanguageManager.get("Manage"));
        }
        if (addMarketerBtn != null) {
            addMarketerBtn.setText(LanguageManager.get("Add Marketer"));
        }
    }

    private void renderMarketers() {
        if (marketersTable == null || orderFacade == null) {
            return;
        }

        if (marketersTable.getChildren().size() > 2) {
            javafx.scene.Node header = marketersTable.getChildren().get(0);
            javafx.scene.Node sep = marketersTable.getChildren().get(1);
            marketersTable.getChildren().clear();
            marketersTable.getChildren().addAll(header, sep);
        }

        selectedUser = null;
        List<User> users = orderFacade.getAllUsers();
        for (User user : users) {
            if ("MARKETER".equalsIgnoreCase(user.getRole())) {
                marketersTable.getChildren().add(createMarketerRow(user));
            }
        }
    }

    private HBox createMarketerRow(User user) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setSpacing(15);
        row.setOnMouseClicked(e -> selectUser(user, row));

        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().addAll("col-id", "body-cell");
        emailLabel.setPrefWidth(200);

        Label nameLabel = new Label(user.getName());
        nameLabel.getStyleClass().addAll("col-name", "body-cell");
        nameLabel.setPrefWidth(200);

        Label roleLabel = new Label(user.getRole());
        roleLabel.getStyleClass().addAll("col-category", "body-cell");
        roleLabel.setPrefWidth(100);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button manageBtn = new Button(LanguageManager.get("Manage"));
        manageBtn.getStyleClass().add("outline-btn");
        manageBtn.setOnAction(e -> {
            selectUser(user, row);
            openEditMarketerDialog(user);
        });

        row.getChildren().addAll(emailLabel, nameLabel, roleLabel, spacer, manageBtn);
        return row;
    }

    private void selectUser(User user, HBox row) {
        selectedUser = user;
        if (marketersTable != null) {
            for (javafx.scene.Node node : marketersTable.getChildren()) {
                if (node instanceof HBox) {
                    node.getStyleClass().remove("table-row-selected");
                }
            }
        }
        row.getStyleClass().add("table-row-selected");
    }

    @FXML
    private void handleAddMarketer() {
        if (!isAdmin()) {
            showAlert(Alert.AlertType.WARNING,
                    LanguageManager.isArabic() ? "غير مسموح" : "Access denied",
                    LanguageManager.isArabic() ? "المسؤول فقط يمكنه إضافة مسوق." : "Only administrators can add marketers.");
            return;
        }
        openAddMarketerDialog();
    }

    @FXML
    private void handleEditMarketer() {
        if (!isAdmin()) {
            showAlert(Alert.AlertType.WARNING,
                    LanguageManager.isArabic() ? "غير مسموح" : "Access denied",
                    LanguageManager.isArabic() ? "المسؤول فقط يمكنه التعديل." : "Only administrators can edit marketers.");
            return;
        }
        if (selectedUser == null) {
            showAlert(Alert.AlertType.INFORMATION,
                    LanguageManager.isArabic() ? "اختر مسوقاً" : "Select a marketer",
                    LanguageManager.isArabic() ? "اختر صفاً من الجدول أو اضغط Manage." : "Select a row or use Manage on a marketer.");
            return;
        }
        openEditMarketerDialog(selectedUser);
    }

    private void openAddMarketerDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.get("Add Marketer"));
        dialog.setHeaderText(LanguageManager.isArabic() ? "حساب مسوق جديد" : "New marketer account");

        ButtonType saveType = new ButtonType(LanguageManager.isArabic() ? "حفظ" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        MarketerFormFields fields = new MarketerFormFields(false);
        dialog.getDialogPane().setContent(fields.grid);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != saveType) {
                return;
            }
            List<String> errors = UserRegistrationValidator.validateNewMarketer(
                    fields.firstNameField.getText(),
                    fields.lastNameField.getText(),
                    fields.emailField.getText(),
                    fields.companyField.getText(),
                    fields.effectivePassword(),
                    fields.effectiveConfirmPassword(),
                    true
            );
            if (!errors.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", String.join("\n", errors));
                return;
            }
            String email = fields.emailField.getText().trim();
            if (orderFacade.emailTakenByOtherUser(email, null)) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        LanguageManager.isArabic() ? "البريد مستخدم بالفعل." : "Email already registered.");
                return;
            }

            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setEmail(email);
            newUser.setPassword(fields.effectivePassword());
            newUser.setName(fields.firstNameField.getText().trim() + " " + fields.lastNameField.getText().trim());
            newUser.setRole("MARKETER");

            try {
                orderFacade.register(newUser);
                orderFacade.addMarketer(new Marketer(newUser.getId(), newUser.getName()));
                renderMarketers();
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        LanguageManager.isArabic() ? "تم إضافة المسوق." : "Marketer added successfully.");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });
    }

    private void openEditMarketerDialog(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(LanguageManager.get("Manage"));
        dialog.setHeaderText(user.getName() + " (" + user.getEmail() + ")");

        ButtonType saveType = new ButtonType(LanguageManager.isArabic() ? "حفظ" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        MarketerFormFields fields = new MarketerFormFields(true);
        splitNameIntoFields(user.getName(), fields);
        fields.emailField.setText(user.getEmail());
        fields.companyField.setText(LanguageManager.isArabic() ? "شركة الجملة" : "Wholesale partner");

        dialog.getDialogPane().setContent(fields.grid);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != saveType) {
                return;
            }
            String newPassword = fields.effectivePassword();
            String confirm = fields.effectiveConfirmPassword();
            List<String> errors = UserRegistrationValidator.validateMarketerEdit(
                    fields.firstNameField.getText(),
                    fields.lastNameField.getText(),
                    fields.emailField.getText(),
                    newPassword,
                    confirm
            );
            if (!errors.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", String.join("\n", errors));
                return;
            }

            String email = fields.emailField.getText().trim();
            if (orderFacade.emailTakenByOtherUser(email, user.getId())) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        LanguageManager.isArabic() ? "البريد مستخدم بالفعل." : "Email already in use.");
                return;
            }

            user.setEmail(email);
            user.setName(fields.firstNameField.getText().trim() + " " + fields.lastNameField.getText().trim());
            if (newPassword != null && !newPassword.isEmpty()) {
                user.setPassword(newPassword);
            }
            orderFacade.saveUser(user);
            orderFacade.addMarketer(new Marketer(user.getId(), user.getName()));
            renderMarketers();
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    LanguageManager.isArabic() ? "تم تحديث بيانات المسوق." : "Marketer updated.");
        });
    }

    private static void splitNameIntoFields(String fullName, MarketerFormFields fields) {
        if (fullName == null || fullName.isBlank()) {
            return;
        }
        int space = fullName.trim().indexOf(' ');
        if (space < 0) {
            fields.firstNameField.setText(fullName.trim());
        } else {
            fields.firstNameField.setText(fullName.substring(0, space).trim());
            fields.lastNameField.setText(fullName.substring(space + 1).trim());
        }
    }

    private boolean isAdmin() {
        User u = orderFacade != null ? orderFacade.getCurrentUser() : null;
        return u != null && "ADMIN".equalsIgnoreCase(u.getRole());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static final class MarketerFormFields {
        final GridPane grid = new GridPane();
        final TextField firstNameField = new TextField();
        final TextField lastNameField = new TextField();
        final TextField emailField = new TextField();
        final TextField companyField = new TextField();
        final PasswordField passwordField = new PasswordField();
        final TextField passwordPlainField = new TextField();
        final PasswordField confirmPasswordField = new PasswordField();
        final TextField confirmPlainField = new TextField();
        final CheckBox showPasswordCheck = new CheckBox();

        MarketerFormFields(boolean editMode) {
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));

            PasswordRevealSupport.bindDual(showPasswordCheck, passwordField, passwordPlainField,
                    confirmPasswordField, confirmPlainField);
            showPasswordCheck.setText(LanguageManager.get("Show passwords"));

            int row = 0;
            grid.add(new Label(LanguageManager.isArabic() ? "الاسم الأول:" : "First name:"), 0, row);
            grid.add(firstNameField, 1, row++);
            grid.add(new Label(LanguageManager.isArabic() ? "اسم العائلة:" : "Last name:"), 0, row);
            grid.add(lastNameField, 1, row++);
            grid.add(new Label("Email:"), 0, row);
            grid.add(emailField, 1, row++);
            grid.add(new Label(LanguageManager.isArabic() ? "الشركة:" : "Company:"), 0, row);
            grid.add(companyField, 1, row++);

            String pwdLabel = editMode
                    ? (LanguageManager.isArabic() ? "كلمة مرور جديدة (اختياري):" : "New password (optional):")
                    : (LanguageManager.isArabic() ? "كلمة المرور:" : "Password:");
            grid.add(new Label(pwdLabel), 0, row);
            grid.add(passwordField, 1, row);
            grid.add(passwordPlainField, 1, row);
            row++;
            grid.add(new Label(LanguageManager.isArabic() ? "تأكيد كلمة المرور:" : "Confirm password:"), 0, row);
            grid.add(confirmPasswordField, 1, row);
            grid.add(confirmPlainField, 1, row);
            row++;
            grid.add(showPasswordCheck, 1, row);
        }

        String effectivePassword() {
            return PasswordRevealSupport.effectivePassword(showPasswordCheck, passwordField, passwordPlainField);
        }

        String effectiveConfirmPassword() {
            return PasswordRevealSupport.effectivePassword(showPasswordCheck, confirmPasswordField, confirmPlainField);
        }
    }
}
