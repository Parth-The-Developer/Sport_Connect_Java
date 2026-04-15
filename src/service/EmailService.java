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
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_SENDER_EMAIL_KEY = "SMTP_SENDER_EMAIL";
    private static final String SMTP_APP_PASSWORD_KEY = "SMTP_APP_PASSWORD";

    public boolean sendFriendRequestEmail(String toEmail, String toName, String fromName) {
        String subject = "New friend request on SportConnect";
        String body = "Hi " + safe(toName) + ",\n\n"
            + safe(fromName) + " sent you a friend request on SportConnect.\n"
            + "Login to view and respond.\n\n"
            + "- SportConnect";
        return sendEmail(toEmail, subject, body, "friend request notification");
    }

    public boolean sendRequestAcceptedEmail(String toEmail, String toName, String acceptedByName) {
        String subject = "Friend request accepted on SportConnect";
        String body = "Hi " + safe(toName) + ",\n\n"
            + safe(acceptedByName) + " accepted your friend request on SportConnect.\n"
            + "You can now chat in the app.\n\n"
            + "- SportConnect";
        return sendEmail(toEmail, subject, body, "acceptance notification");
    }

    private boolean sendEmail(String toEmail, String subject, String body, String emailType) {
        SmtpCredentials credentials = loadSmtpCredentials();
        if (credentials == null) {
            return false;
        }
        if (!isValidEmail(toEmail)) {
            System.out.println("[Email] Skipped: recipient email is empty or invalid.");
            return false;
        }

        Session session = createSession(credentials.getSenderEmail(), credentials.getAppPassword());

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(credentials.getSenderEmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("[Email] Sent " + emailType + " to " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.out.println("[Email] Send failed (" + emailType + "): " + e.getMessage());
            return false;
        }
    }

    private SmtpCredentials loadSmtpCredentials() {
        String smtpEmail = readEnv(SMTP_SENDER_EMAIL_KEY);
        String smtpPassword = readEnv(SMTP_APP_PASSWORD_KEY);
        if (isBlank(smtpEmail) || isBlank(smtpPassword)) {
            System.out.println("[Email] Skipped: set " + SMTP_SENDER_EMAIL_KEY + " and " + SMTP_APP_PASSWORD_KEY + ".");
            return null;
        }
        return new SmtpCredentials(smtpEmail, smtpPassword);
    }

    private Session createSession(String smtpEmail, String smtpPassword) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpEmail, smtpPassword);
            }
        });
    }

    private boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
            return true;
        } catch (MessagingException ex) {
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

    private String safe(String s) {
        return isBlank(s) ? "Player" : s;
    }

    private static class SmtpCredentials {
        private final String senderEmail;
        private final String appPassword;

        private SmtpCredentials(String senderEmail, String appPassword) {
            this.senderEmail = senderEmail;
            this.appPassword = appPassword;
        }

        private String getSenderEmail() {
            return senderEmail;
        }

        private String getAppPassword() {
            return appPassword;
        }
    }
}
