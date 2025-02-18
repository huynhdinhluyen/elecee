package com.example.electrical_preorder_system_backend.service.email;

import jakarta.mail.MessagingException;

public interface IEmailService {
    void sendEmail(String to, String subject, String body) throws MessagingException;

    String subjectRegister();

    String bodyRegister(String email, String fullName);

}
