package com.example.electrical_preorder_system_backend.service.email;

import com.example.electrical_preorder_system_backend.config.jwt.JwtUtils;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailService implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    @Autowired
    private JavaMailSender emailSender;
    @Value("${spring.mail.username}")
    private String systemMailAddress;
    @Value("${client.domain}")
    private String clientUrl;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();

        message.setFrom("hello@elecee.vercel.app");
        message.setRecipients(Message.RecipientType.TO, to);
        message.setSubject(subject);
        message.setText(body, "utf-8", "html");

        emailSender.send(message);
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("hello@elecee.vercel.app");
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//        emailSender.send(message);

    }

    @Override
    public String subjectRegister() {
        return "Elecee - Verify your email";
    }

    @Override
    public String bodyRegister(String email, String fullName) {
        String verificationUrl = " <a href=\"" + clientUrl + "verify?token=" + jwtUtils.generateVerificationToken(email) + " \" class=\"button\">Verify Email Address</a>\n";
        log.info("Verification URL: {}", verificationUrl);
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"utf-8\">\n"
                + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <style>\n"
                + "    body {\n"
                + "      font-family: Arial, sans-serif;\n"
                + "      margin: 0;\n"
                + "      padding: 0;\n"
                + "      background-color: #f4f4f4;\n"
                + "    }\n"
                + "    .container {\n"
                + "      max-width: 600px;\n"
                + "      margin: 0 auto;\n"
                + "      background-color: #ffffff;\n"
                + "      padding: 20px;\n"
                + "    }\n"
                + "    .header {\n"
                + "      text-align: center;\n"
                + "      padding: 20px 0;\n"
                + "      border-bottom: 1px solid #eee;\n"
                + "    }\n"
                + "    .logo {\n"
                + "      max-width: 150px;\n"
                + "      height: auto;\n"
                + "    }\n"
                + "    .content {\n"
                + "      padding: 30px 0;\n"
                + "      color: #333333;\n"
                + "      line-height: 1.6;\n"
                + "    }\n"
                + "    .button {\n"
                + "      display: inline-block;\n"
                + "      padding: 12px 30px;\n"
                + "      background-color: #007bff;\n"
                + "      color: #fff;\n"
                + "      text-decoration: none;\n"
                + "      border-radius: 5px;\n"
                + "      margin: 20px 0;\n"
                + "    }\n"
                + "    .footer {\n"
                + "      text-align: center;\n"
                + "      padding: 20px;\n"
                + "      color: #666666;\n"
                + "      font-size: 12px;\n"
                + "      border-top: 1px solid #eee;\n"
                + "    }\n"
                + "    .timestamp {\n"
                + "      color: #999999;\n"
                + "      font-size: 12px;\n"
                + "      margin-bottom: 20px;\n"
                + "    }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div class=\"container\">\n"
                + "    <div class=\"header\">\n"
                + "      <img src=\"https://elecee.vercel.app/assets/Elecee_logo-BgfdPs0B.jpg\" alt=\"Company Logo\" class=\"logo\">\n"
                + "    </div>\n"
                + "    <div class=\"content\">\n"
                + "      <div class=\"timestamp\">\n"
                + "        Sent on: " + LocalDate.now() + "\n"
                + "      </div>\n"
                + "      <p>Hello " + fullName + ",</p>\n"
                + "      <p>Thank you for registering with Elecee. To ensure the security of your account, please verify your email address by clicking the button below:</p>\n"
                + "      <div style=\"text-align: center;\">\n"
                + "        <a href=\"" + clientUrl + "verify?token=" + jwtUtils.generateVerificationToken(email) + " \" class=\"button\">Verify Email Address</a>\n"
                + "      </div>\n"
                + "      <p>If you did not create an account, please ignore this email.</p>\n"
                + "      <p>Best regards,<br>The Elecee Team</p>\n"
                + "    </div>\n"
                + "    <div class=\"footer\">\n"
                + "      <p>Elecee | https://elecee.vercel.app /</p>\n"
                + "      <p>This is an automated message, please do not reply.</p>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>";
    }


}
