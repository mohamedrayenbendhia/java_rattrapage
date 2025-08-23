package utils;

import java.util.regex.Pattern;

/**
 * Utility class for data validation
 */
public class ValidationUtils {

    // Regex for email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Regex for phone number validation (exactly 8 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9]{8}$"
    );

    // Regex for name validation: letters and spaces only (no digits), supports unicode letters
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[\\p{L} ]+$"
    );

    /**
     * Validates if a name is valid (between 3 and 20 characters)
     * @param name The name to validate
     * @return true if the name is valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < 3 || trimmedName.length() > 20) {
            return false;
        }
        // Must contain letters (unicode) and spaces only; no digits allowed
        return NAME_PATTERN.matcher(trimmedName).matches();
    }

    /**
     * Validates if an email is valid
     * @param email The email to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates if a phone number is valid (exactly 8 digits)
     * @param phone The phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates if a password is valid (at least 8 characters)
     * @param password The password to validate
     * @return true if the password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    /**
     * Validates if passwords match
     * @param password The first password
     * @param repeatPassword The repeat password
     * @return true if passwords match, false otherwise
     */
    public static boolean doPasswordsMatch(String password, String repeatPassword) {
        return password != null && password.equals(repeatPassword);
    }

    /**
     * Gets validation error message for name
     * @param name The name to validate
     * @return Error message or null if valid
     */
    public static String getNameValidationError(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Name is required";
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < 3) {
            return "Name must be at least 3 characters long";
        }
        if (trimmedName.length() > 20) {
            return "Name must not exceed 20 characters";
        }
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            return "Name must contain letters only";
        }
        return null;
    }

    /**
     * Gets validation error message for email
     * @param email The email to validate
     * @return Error message or null if valid
     */
    public static String getEmailValidationError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }
        return null;
    }

    /**
     * Gets validation error message for phone number
     * @param phone The phone number to validate
     * @return Error message or null if valid
     */
    public static String getPhoneValidationError(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Phone number is required";
        }
        if (!isValidPhoneNumber(phone)) {
            return "Phone number must be exactly 8 digits";
        }
        return null;
    }

    /**
     * Gets validation error message for password
     * @param password The password to validate
     * @return Error message or null if valid
     */
    public static String getPasswordValidationError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        return null;
    }

    /**
     * Gets validation error message for repeat password
     * @param password The original password
     * @param repeatPassword The repeat password
     * @return Error message or null if valid
     */
    public static String getRepeatPasswordValidationError(String password, String repeatPassword) {
        if (repeatPassword == null || repeatPassword.isEmpty()) {
            return "Please confirm your password";
        }
        if (!doPasswordsMatch(password, repeatPassword)) {
            return "Passwords do not match";
        }
        return null;
    }

    /**
     * Checks if a string is null or empty
     * @param str The string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Cleans a string by removing leading and trailing spaces
     * @param str The string to clean
     * @return The cleaned string
     */
    public static String cleanString(String str) {
        return str != null ? str.trim() : null;
    }
}
