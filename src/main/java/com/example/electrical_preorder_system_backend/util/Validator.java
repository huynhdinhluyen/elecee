package com.example.electrical_preorder_system_backend.util;

import com.example.electrical_preorder_system_backend.dto.request.user.UpdateUserRequest;
import com.example.electrical_preorder_system_backend.dto.request.user.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.NotificationType;
import com.example.electrical_preorder_system_backend.enums.OrderStatus;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class Validator {
    static class ErrorMessageFactory {
        public static final Integer MAX_PAGE_SIZE = 100;
        public static final String INVALID_PAGE_MESSAGE = "Page number must be greater than or equal to 0";
        public static final String INVALID_SIZE_MESSAGE = "Page size must be greater than or equal to 0 and less than or equal to " + MAX_PAGE_SIZE;

        // Fullname requirements
        public static final String FULLNAME_REGEX = "^[\\p{L}\\s-]{5,50}$";
        public static final String INVALID_FULLNAME_MESSAGE = "Fullname must be between 5 and 50 characters and contain only letters";

        // Phone number requirements
        public static final String PHONE_NUMBER_REGEX = "^(\\+84|0)[0-9]{9,}$";
        public static final String INVALID_PHONE_NUMBER_MESSAGE = "Phone number must be between 10 numbers";

        // Address requirements
        public static final String ADDRESS_REGEX = "^[\\p{L}\\s-]{5,255}$";
        public static final String INVALID_ADDRESS_MESSAGE = "Address must be between 5 and 255 characters and contain only letters";

        // Password requirements
        public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";
        public static final String INVALID_PASSWORD_MESSAGE = "Password must be between 8 and 50 characters and contain at least one lowercase letter, one uppercase letter, one digit, and one special character";

        // Sort direction requirements
        public static final String SORT_DIRECTION_REGEX = "^(desc|asc)$";
        public static final String INVALID_SORT_DIRECTION_MESSAGE = "Sort direction must be 'asc' or 'desc'";

        // Invalid field message
        public static final String INVALID_FIELD_MESSAGE = "Invalid field: ";

    }

    public static boolean isValidField(Class<?> entityClass, String field) {
        List<String> entityFields = Arrays.stream(entityClass.getDeclaredFields())
                .map(Field::getName)
                .toList();
        return entityFields.contains(field);
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches(ErrorMessageFactory.PHONE_NUMBER_REGEX);
    }

    public static boolean isValidAddress(String address) {
        return address.trim().matches(ErrorMessageFactory.ADDRESS_REGEX);
    }

    public static boolean isValidFullname(String fullname) {
        return fullname.trim().matches(ErrorMessageFactory.FULLNAME_REGEX);
    }

    public static boolean isValidPassword(String password) {
        return password.matches(ErrorMessageFactory.PASSWORD_REGEX);
    }

    public static boolean isValidSortDirection(String sortDirection) {
        return sortDirection.trim().toLowerCase().matches(ErrorMessageFactory.SORT_DIRECTION_REGEX);
    }

    public static boolean isValidNotificationType(String type) {
        try {
            NotificationType.valueOf(type);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    public static boolean isValidOrderStatus(String status){
        try {
            OrderStatus.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String verifyUserUpdate(User user, UpdateUserRequest request){
        switch (user.getStatus()){
            case INACTIVE:
                return UserStatus.INACTIVE.getErrorMessage();
            case BANNED:
                return UserStatus.BANNED.getErrorMessage();
            default:
                break;
        }
        if (!isValidFullname(request.getFullname())){
            return ErrorMessageFactory.INVALID_FULLNAME_MESSAGE;
        }
        if (!isValidPhoneNumber(request.getPhoneNumber())){
            return ErrorMessageFactory.INVALID_PHONE_NUMBER_MESSAGE;
        }
        if (!isValidAddress(request.getAddress())){
            return ErrorMessageFactory.INVALID_ADDRESS_MESSAGE;
        }
        return null;
    }

    public static boolean isValidUserRole(String role){
        try {
            UserRole.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidUserStatus(String status){
        try {
            UserStatus.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String verifyUserSignUp(UserSignUpRequest request){
        if (!isValidFullname(request.getFullname())){
            return ErrorMessageFactory.INVALID_FULLNAME_MESSAGE;
        }
        if (!isValidPhoneNumber(request.getPhoneNumber())){
            return ErrorMessageFactory.INVALID_PHONE_NUMBER_MESSAGE;
        }
        if (!isValidAddress(request.getAddress())){
            return ErrorMessageFactory.INVALID_ADDRESS_MESSAGE;
        }
        if (!isValidPassword(request.getPassword())){
            return ErrorMessageFactory.INVALID_PASSWORD_MESSAGE;
        }
        return null;
    }

    public static boolean isValidPage(int page){
        return page >= 0;
    }

    public static boolean isValidSize(int size){
        return size >= 0 && size <= ErrorMessageFactory.MAX_PAGE_SIZE;
    }

    public static String verifyUserFilter(int page, int size, String role, String status,
                                          String sortField, String sortDirection, Class<?> entityClass){
        if (!isValidField(entityClass, sortField)){
            return ErrorMessageFactory.INVALID_FIELD_MESSAGE + sortField;
        }
        if (!isValidSortDirection(sortDirection)){
            return ErrorMessageFactory.INVALID_SORT_DIRECTION_MESSAGE;
        }
        if (!isValidUserStatus(status)){
            return "Invalid status: " + status;
        }
        if (!isValidUserRole(role)){
            return "Invalid role: " + role;
        }
        if (!isValidPage(page)){
            return ErrorMessageFactory.INVALID_PAGE_MESSAGE;
        }
        if (!isValidSize(size)){
            return ErrorMessageFactory.INVALID_SIZE_MESSAGE;
        }
        return null;
    }

    public static String verifyOrderFilter(int page, int size, String status, String sortField, String sortDirection, Class<?> entityClass){
        if (!isValidField(entityClass, sortField)){
            return ErrorMessageFactory.INVALID_FIELD_MESSAGE + sortField;
        }
        if (!isValidSortDirection(sortDirection)){
            return ErrorMessageFactory.INVALID_SORT_DIRECTION_MESSAGE;
        }
        if (!isValidOrderStatus(status)){
            return "Invalid status: " + status;
        }
        if (!isValidPage(page)){
            return ErrorMessageFactory.INVALID_PAGE_MESSAGE;
        }
        if (!isValidSize(size)){
            return ErrorMessageFactory.INVALID_SIZE_MESSAGE;
        }
        return null;
    }

    public static String verifyPaymentFilter(int page, int size, String sortField, String sortDirection, Class<?> entityClass){
        if (!isValidField(entityClass, sortField)){
            return ErrorMessageFactory.INVALID_FIELD_MESSAGE + sortField;
        }
        if (!isValidSortDirection(sortDirection)){
            return ErrorMessageFactory.INVALID_SORT_DIRECTION_MESSAGE;
        }
        if (!isValidPage(page)){
            return ErrorMessageFactory.INVALID_PAGE_MESSAGE;
        }
        if (!isValidSize(size)){
            return ErrorMessageFactory.INVALID_SIZE_MESSAGE;
        }

        return null;
    }
}
