package com.supplysync.presentation.auth;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Toggle visibility between masked {@link PasswordField} and plain {@link TextField} (SRP: password UX only).
 */
public final class PasswordRevealSupport {
    private PasswordRevealSupport() {}

    public static void bindSingle(CheckBox showPassword, PasswordField masked, TextField plain) {
        plain.setVisible(false);
        plain.setManaged(false);

        ChangeListener<String> syncMaskedToPlain = (obs, o, n) -> {
            if (showPassword.isSelected()) {
                plain.setText(n);
            }
        };
        ChangeListener<String> syncPlainToMasked = (obs, o, n) -> {
            if (showPassword.isSelected()) {
                masked.setText(n);
            }
        };

        masked.textProperty().addListener(syncMaskedToPlain);
        plain.textProperty().addListener(syncPlainToMasked);

        showPassword.selectedProperty().addListener((obs, was, selected) -> {
            if (Boolean.TRUE.equals(selected)) {
                plain.setText(masked.getText());
                masked.setVisible(false);
                masked.setManaged(false);
                plain.setVisible(true);
                plain.setManaged(true);
            } else {
                masked.setText(plain.getText());
                plain.setVisible(false);
                plain.setManaged(false);
                masked.setVisible(true);
                masked.setManaged(true);
            }
        });
    }

    /**
     * One checkbox toggles two password/confirm pairs (registration / forgot-password).
     */
    public static void bindDual(
            CheckBox showPassword,
            PasswordField masked1,
            TextField plain1,
            PasswordField masked2,
            TextField plain2) {
        plain1.setVisible(false);
        plain1.setManaged(false);
        plain2.setVisible(false);
        plain2.setManaged(false);

        masked1.textProperty().addListener((o, a, t) -> {
            if (showPassword.isSelected()) {
                plain1.setText(t);
            }
        });
        plain1.textProperty().addListener((o, a, t) -> {
            if (showPassword.isSelected()) {
                masked1.setText(t);
            }
        });
        masked2.textProperty().addListener((o, a, t) -> {
            if (showPassword.isSelected()) {
                plain2.setText(t);
            }
        });
        plain2.textProperty().addListener((o, a, t) -> {
            if (showPassword.isSelected()) {
                masked2.setText(t);
            }
        });

        showPassword.selectedProperty().addListener((obs, was, selected) -> {
            if (Boolean.TRUE.equals(selected)) {
                plain1.setText(masked1.getText());
                plain2.setText(masked2.getText());
                setPairVisible(masked1, plain1, true);
                setPairVisible(masked2, plain2, true);
            } else {
                masked1.setText(plain1.getText());
                masked2.setText(plain2.getText());
                setPairVisible(masked1, plain1, false);
                setPairVisible(masked2, plain2, false);
            }
        });
    }

    private static void setPairVisible(PasswordField masked, TextField plain, boolean showPlain) {
        plain.setVisible(showPlain);
        plain.setManaged(showPlain);
        masked.setVisible(!showPlain);
        masked.setManaged(!showPlain);
    }

    public static String effectivePassword(CheckBox showPassword, PasswordField masked, TextField plain) {
        if (masked == null) {
            return "";
        }
        if (showPassword != null && showPassword.isSelected() && plain != null) {
            return plain.getText();
        }
        return masked.getText();
    }
}
