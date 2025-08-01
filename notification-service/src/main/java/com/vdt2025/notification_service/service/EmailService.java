package com.vdt2025.notification_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    JavaMailSender mailSender;

    public void sendWelcomeEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Chào mừng đến với NTA VDT_2025!");
        message.setText("Xin chào " + username + ",\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản trên NTA VDT_2025. Chúng tôi rất vui mừng chào đón bạn đến với cộng đồng của chúng tôi.\n\n" +
                "Nếu bạn có bất kỳ câu hỏi nào, xin vui lòng liên hệ với chúng tôi qua email này.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ NTA VDT_2025");
        try {
            mailSender.send(message);
            log.info("Welcome email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    public void sendErrorEmailToAdmin(String errorMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("anhnta2004@gmail.com");
        message.setSubject("Lỗi trong dịch vụ thông báo NTA VDT_2025");
        message.setText("Đã xảy ra lỗi trong dịch vụ thông báo NTA VDT_2025:\n\n" +
                errorMessage + "\n\n" +
                "Vui lòng kiểm tra và xử lý kịp thời.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ NTA VDT_2025");
        try {
            mailSender.send(message);
            log.info("Error email sent to admin");
        } catch (Exception e) {
            log.error("Failed to send error email to admin: {}", e.getMessage());
            throw new RuntimeException("Failed to send error email to admin", e);
        }
    }
}
