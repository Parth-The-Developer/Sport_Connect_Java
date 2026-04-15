package service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    // 1. UPDATE THESE CREDENTIALS
    static final String senderEmail = "tp025060@gmail.com"; // Your Gmail address
    static final String appPassword = "qxie ndwa czuo ygsp"; // Your 16-character Google App Password

    public boolean sendFriendRequestEmail(String toEmail, String toName, String fromName) {
        String smtpEmail = firstNonBlank(readEnv("SMTP_SENDER_EMAIL"), senderEmail);
        String smtpPassword = firstNonBlank(readEnv("SMTP_APP_PASSWORD"), appPassword);

        if (isBlank(smtpEmail) || isBlank(smtpPassword)) {
            System.out.println("[Email] Skipped: configure SMTP_SENDER_EMAIL and SMTP_APP_PASSWORD.");
            return false;
        }
        if (isBlank(toEmail)) {
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpEmail, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("New friend request on SportConnect");
            message.setText("Hi " + safe(toName) + ",\n\n"
                + safe(fromName) + " sent you a friend request on SportConnect.\n"
                + "Login to view and respond.\n\n"
                + "- SportConnect");

            Transport.send(message);
            System.out.println("[Email] Notification sent to " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.out.println("[Email] Send failed: " + e.getMessage());
            return false;
        }
    }

    public boolean sendRequestAcceptedEmail(String toEmail, String toName, String acceptedByName) {
        String smtpEmail = firstNonBlank(readEnv("SMTP_SENDER_EMAIL"), senderEmail);
        String smtpPassword = firstNonBlank(readEnv("SMTP_APP_PASSWORD"), appPassword);

        if (isBlank(smtpEmail) || isBlank(smtpPassword)) {
            System.out.println("[Email] Skipped: configure SMTP_SENDER_EMAIL and SMTP_APP_PASSWORD.");
            return false;
        }
        if (isBlank(toEmail)) {
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpEmail, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Friend request accepted on SportConnect");
            message.setText("Hi " + safe(toName) + ",\n\n"
                + safe(acceptedByName) + " accepted your friend request on SportConnect.\n"
                + "You can now chat in the app.\n\n"
                + "- SportConnect");

            Transport.send(message);
            System.out.println("[Email] Acceptance notification sent to " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.out.println("[Email] Send failed: " + e.getMessage());
            return false;
        }
    }

    private String readEnv(String key) {
        String value = System.getenv(key);
        return value != null ? value.trim() : null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : second;
    }

    private String safe(String s) {
        return isBlank(s) ? "Player" : s;
    }
}
