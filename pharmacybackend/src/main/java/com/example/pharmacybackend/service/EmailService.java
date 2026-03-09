package com.example.pharmacybackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTemporaryPasswordEmail(String toEmail, String name, String tempPassword) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Temporary Password - MediNavi");
        message.setReplyTo(fromEmail); // optional

        String body =
                "Dear " + (name == null || name.isBlank() ? "User" : name) + ",\n\n" +
                        "You requested a password reset.\n\n" +
                        "Your temporary password is:\n" +
                        tempPassword + "\n\n" +
                        "Please login using this temporary password and change it immediately in your profile.\n\n" +
                        "If you did not request this, please ignore this email or contact support.\n\n" +
                        "Regards,\n" +
                        "MediNavi Team";

        message.setText(body);

        mailSender.send(message);
    }
}