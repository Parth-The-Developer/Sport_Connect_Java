package service;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


// export SENDER_EMAIL="tp025060@gmail.com"
// export APP_PASSWORD="qxie ndwa czuo ygsp"

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_SSL_PORT = 465;
    private static final String SMTP_SENDER_EMAIL_KEY = "SMTP_SENDER_EMAIL";
    private static final String SMTP_APP_PASSWORD_KEY = "SMTP_APP_PASSWORD";
    /** Short aliases (e.g. {@code export SENDER_EMAIL=...} / {@code export APP_PASSWORD=...}). */
    private static final String ALT_SENDER_EMAIL_KEY = "SENDER_EMAIL";
    private static final String ALT_APP_PASSWORD_KEY = "APP_PASSWORD";

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
    // Dhruv - added method to send payment confirmation email after successful payment

    public boolean sendPaymentConfirmationEmail(String toEmail, String toName,
                                                String paymentID, double amount,
                                                String method, String sessionID) {
        String subject = "Payment Confirmation - SportConnect";
        String body = "Hi " + safe(toName) + ",\n\n"
            + "Your payment was successful. Here are your details:\n\n"
            + "  Payment ID : " + paymentID + "\n"
            + "  Amount     : $" + amount + "\n"
            + "  Method     : " + method + "\n"
            + "  Session    : " + sessionID + "\n\n"
            + "Thank you for using SportConnect!\n\n"
            + "- SportConnect";
        return sendEmail(toEmail, subject, body, "payment confirmation");
    }
    // completed method to send payment confirmation email after successful payment. Dhurv
    

    private boolean sendEmail(String toEmail, String subject, String body, String emailType) {
        SmtpCredentials credentials = loadSmtpCredentials();
        if (credentials == null) {
            return false;
        }
        if (!isValidEmail(toEmail)) {
            System.out.println("[Email] Skipped: recipient email is empty or invalid.");
            return false;
        }
        try {
            sendViaGmailSmtp(
                credentials.getSenderEmail(),
                credentials.getAppPassword(),
                toEmail,
                subject,
                body
            );
            System.out.println("[Email] Sent " + emailType + " to " + toEmail);
            return true;
        } catch (Exception e) {
            System.out.println("[Email] Send failed (" + emailType + "): " + e.getMessage());
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        return !isBlank(email) && email.contains("@") && email.contains(".");
    }

    /**
     * Reads credentials from the OS environment first, then JVM system properties
     * (so you can run: {@code java -DSMTP_SENDER_EMAIL=you@gmail.com -DSMTP_APP_PASSWORD=xxxx Main}).
     */
    private String readCredential(String key) {
        String value = System.getenv(key);
        if (!isBlank(value)) return value.trim();
        value = System.getProperty(key);
        return isBlank(value) ? null : value.trim();
    }

    /** Tries keys in order (env, then system property for each key). */
    private String readFirstCredential(String... keys) {
        for (String key : keys) {
            String v = readCredential(key);
            if (!isBlank(v)) return v;
        }
        return null;
    }

    /** Gmail app passwords are 16 characters; spaces are display-only. */
    private String normalizeAppPassword(String raw) {
        if (raw == null) return null;
        return raw.replaceAll("\\s+", "");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String safe(String s) {
        return isBlank(s) ? "Player" : s;
    }

    private SmtpCredentials loadSmtpCredentials() {
        String smtpEmail = readFirstCredential(SMTP_SENDER_EMAIL_KEY, ALT_SENDER_EMAIL_KEY);
        String smtpPassword = normalizeAppPassword(
            readFirstCredential(SMTP_APP_PASSWORD_KEY, ALT_APP_PASSWORD_KEY));
        if (isBlank(smtpEmail) || isBlank(smtpPassword)) {
            System.out.println("[Email] Skipped: set "
                + SMTP_SENDER_EMAIL_KEY + " / " + ALT_SENDER_EMAIL_KEY + " and "
                + SMTP_APP_PASSWORD_KEY + " / " + ALT_APP_PASSWORD_KEY
                + " (Gmail: App Password, not your normal login password).");
            return null;
        }
        return new SmtpCredentials(smtpEmail, smtpPassword);
    }

    private void sendViaGmailSmtp(String fromEmail, String appPassword,
                                  String toEmail, String subject, String body) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(SMTP_HOST, SMTP_SSL_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            expectCode(readLine(in), "220");
            sendLine(out, "EHLO localhost");
            readMultiline(in);

            sendLine(out, "AUTH LOGIN");
            expectCode(readLine(in), "334");
            sendLine(out, base64(fromEmail));
            expectCode(readLine(in), "334");
            sendLine(out, base64(appPassword));
            expectCode(readLine(in), "235");

            sendLine(out, "MAIL FROM:<" + fromEmail + ">");
            expectCode(readLine(in), "250");
            sendLine(out, "RCPT TO:<" + toEmail + ">");
            expectCode(readLine(in), "250");
            sendLine(out, "DATA");
            expectCode(readLine(in), "354");

            sendLine(out, "From: " + fromEmail);
            sendLine(out, "To: " + toEmail);
            sendLine(out, "Subject: " + sanitizeHeader(subject));
            sendLine(out, "");
            sendLine(out, body);
            sendLine(out, ".");
            expectCode(readLine(in), "250");

            sendLine(out, "QUIT");
            readLine(in);
        }
    }

    private String base64(String v) {
        return Base64.getEncoder().encodeToString(v.getBytes(StandardCharsets.UTF_8));
    }

    private String sanitizeHeader(String v) {
        return v.replace("\r", " ").replace("\n", " ");
    }

    private void sendLine(BufferedWriter out, String line) throws IOException {
        out.write(line);
        out.write("\r\n");
        out.flush();
    }

    private String readLine(BufferedReader in) throws IOException {
        String line = in.readLine();
        if (line == null) throw new IOException("SMTP connection closed unexpectedly");
        return line;
    }

    private String readMultiline(BufferedReader in) throws IOException {
        String line = readLine(in);
        while (line.length() >= 4 && line.charAt(3) == '-') {
            line = readLine(in);
        }
        return line;
    }

    private void expectCode(String line, String expectedPrefix) throws IOException {
        if (!line.startsWith(expectedPrefix)) {
            throw new IOException("SMTP error: " + line);
        }
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
