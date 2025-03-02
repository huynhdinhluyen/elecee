package com.example.electrical_preorder_system_backend.util;

import com.example.electrical_preorder_system_backend.enums.NotificationType;

public class Validator {

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("^(\\+84|0)[0-9]{9,}$");
    }

    public static boolean isValidAddress(String address) {
        return !address.isEmpty() && address.length() <= 255;
    }

    public static boolean isValidFullname(String fullname) {
        return fullname.length() >= 3 && fullname.length() <= 50;
    }

    public static boolean isValidPassword(String password) {
        return password.length() >= 6 && password.length() <= 50;
    }

    public static boolean isValidNotificationType(String type) {
        try {
            NotificationType.valueOf(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
