package com.supplysync.presentation.auth;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared registration rules (registration page + admin add marketer).
 */
public final class UserRegistrationValidator {
    public static final String OTHER_CATEGORY_LABEL = "Other";

    private UserRegistrationValidator() {
    }

    public static List<String> validateNewMarketer(
            String firstName,
            String lastName,
            String email,
            String company,
            String password,
            String confirmPassword,
            boolean termsAgreed
    ) {
        List<String> errors = new ArrayList<>();
        if (isBlank(firstName) || isBlank(lastName) || isBlank(email)
                || isBlank(company) || isBlank(password) || isBlank(confirmPassword)) {
            errors.add("All fields are required.");
            return errors;
        }
        validateNameAndEmail(firstName, lastName, email, errors);
        validatePassword(password, confirmPassword, errors);
        if (!termsAgreed) {
            errors.add("You must agree to the Terms and Privacy Policy.");
        }
        return errors;
    }

    public static List<String> validateMarketerEdit(
            String firstName,
            String lastName,
            String email,
            String newPassword,
            String confirmPassword
    ) {
        List<String> errors = new ArrayList<>();
        if (isBlank(firstName) || isBlank(lastName) || isBlank(email)) {
            errors.add("First name, last name, and email are required.");
            return errors;
        }
        validateNameAndEmail(firstName, lastName, email, errors);
        if (!isBlank(newPassword) || !isBlank(confirmPassword)) {
            validatePassword(
                    newPassword != null ? newPassword : "",
                    confirmPassword != null ? confirmPassword : "",
                    errors
            );
        }
        return errors;
    }

    private static void validateNameAndEmail(String firstName, String lastName, String email, List<String> errors) {
        if (firstName.trim().length() < 3) {
            errors.add("First name must be at least 3 characters.");
        }
        if (lastName.trim().length() < 3) {
            errors.add("Last name must be at least 3 characters.");
        }
        String lower = email.trim().toLowerCase();
        if (!lower.contains("@gmail") && !lower.contains("@icloud")) {
            errors.add("Email must be a @gmail or @icloud address.");
        }
    }

    private static void validatePassword(String password, String confirmPassword, List<String> errors) {
        if (password.length() < 6) {
            errors.add("Password must be at least 6 characters.");
        }
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");
        if (!hasLetter || !hasDigit || !hasSymbol) {
            errors.add("Password must contain at least one letter, one number, and one symbol.");
        }
        if (!password.equals(confirmPassword)) {
            errors.add("Passwords do not match.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
