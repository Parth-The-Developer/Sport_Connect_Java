package service;

import enums.PaymentMethod;
import model.Payment;
import model.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PaymentService {

    private List<Payment> payments = new ArrayList<>();
    private Map<String, String> playerPostalCodes = new HashMap<>(); // playerID -> postalCode
    private int idCounter = 1;
    private static final String CSV_FILE        = "data/payments.csv";
    private static final String POSTAL_CSV_FILE = "data/player_postal_codes.csv";

    public PaymentService() {
        loadPostalCodes();
    }

    // Main entry point called from Main.java
    public void makePayment(Player player, String sessionID, Scanner sc) {

        System.out.println("\n--- Select Payment Method ---\n");
        System.out.println("  1. Card");
        System.out.println("  2. Apple Wallet");
        System.out.println("  3. Google Wallet");
        System.out.println("  4. PayPal");
        System.out.print("\n  Choice: ");

        String choice = sc.nextLine().trim();

        System.out.print("  Amount to pay: $");
        double amount;
        try {
            amount = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid amount.");
            return;
        }

        // Postal code validation
        String playerID = String.valueOf(player.getPlayerId());
        String postalCode = handlePostalCode(playerID, sc);
        if (postalCode == null) return;

        switch (choice) {
            case "1" -> processCard(player, sessionID, amount, postalCode, sc);
            case "2" -> processAppleWallet(player, sessionID, amount, postalCode);
            case "3" -> processGoogleWallet(player, sessionID, amount, postalCode);
            case "4" -> processPayPal(player, sessionID, amount, postalCode, sc);
            default  -> System.out.println("[!] Invalid option.");
        }
    }

    // ── Postal code handler ───────────────────────────────────────────────────

    private String handlePostalCode(String playerID, Scanner sc) {

        String storedPostal = playerPostalCodes.get(playerID);

        if (storedPostal == null) {
            // First time — ask and store
            System.out.print("  Postal Code (Canadian format e.g. M5V 1J1): ");
            String entered = sc.nextLine().trim().toUpperCase();

            if (!isValidCanadianPostal(entered)) {
                System.out.println("[!] Invalid Canadian postal code. Must be in format A1A 1A1.");
                return null;
            }

            playerPostalCodes.put(playerID, entered);
            savePostalCodes();
            System.out.println("  Postal code saved for future payments.");
            return entered;

        } else {
            // Returning player — validate against stored
            System.out.print("  Postal Code (for billing): ");
            String entered = sc.nextLine().trim().toUpperCase();

            if (!entered.equals(storedPostal)) {
                System.out.println("[!] Postal code does not match our records. Payment declined.");
                return null;
            }

            return entered;
        }
    }

    // ── Card ──────────────────────────────────────────────────────────────────

    private void processCard(Player player, String sessionID, double amount,
                             String postalCode, Scanner sc) {
        System.out.println("\n--- Card Payment ---\n");

        System.out.print("  Card Number (16 digits): ");
        String cardNumber = sc.nextLine().trim();

        if (!cardNumber.matches("\\d{16}")) {
            System.out.println("[!] Invalid card number. Must be 16 digits.");
            return;
        }

        String cardType = detectCardType(cardNumber);
        if (cardType.equals("UNKNOWN")) {
            System.out.println("[!] Card declined. Unsupported card type.");
            System.out.println("    Accepted: Visa (starts with 4), Mastercard (starts with 5), Amex (starts with 34 or 37, 15 digits).");
            return;
        }

        if (cardNumber.equals("1234567890123456")) {
            System.out.println("[!] Card declined. Please use a valid card.");
            return;
        }

        System.out.println("  Card Type   : " + cardType);

        System.out.print("  CVV (3 digits): ");
        String cvv = sc.nextLine().trim();
        if (!cvv.matches("\\d{3}")) {
            System.out.println("[!] Invalid CVV. Must be 3 digits.");
            return;
        }

        System.out.print("  Expiry Date (MM/YY): ");
        String expiry = sc.nextLine().trim();
        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            System.out.println("[!] Invalid expiry date. Use MM/YY format.");
            return;
        }

        if (isCardExpired(expiry)) {
            System.out.println("[!] Card is expired.");
            return;
        }

        System.out.println("  Processing card payment...");
        waitThreeSeconds();

        Payment payment = createPayment(player, sessionID, amount, PaymentMethod.CARD, postalCode);
        payment.processPayment();
        System.out.println("\n[OK] Card payment of $" + amount + " successful!\n");
        printReceipt(payment);
        saveToCSV();
    }

    // ── Apple Wallet ──────────────────────────────────────────────────────────

    private void processAppleWallet(Player player, String sessionID,
                                    double amount, String postalCode) {
        System.out.println("\n--- Apple Wallet ---\n");
        System.out.println("  A payment prompt of $" + amount + " has been sent to your iPhone.");
        System.out.println("  Please confirm with Face ID or Touch ID on your device...");
        waitThreeSeconds();

        Payment payment = createPayment(player, sessionID, amount, PaymentMethod.APPLE_WALLET, postalCode);
        payment.processPayment();
        System.out.println("\n[OK] Apple Wallet payment of $" + amount + " successful!\n");
        printReceipt(payment);
        saveToCSV();
    }

    // ── Google Wallet ─────────────────────────────────────────────────────────

    private void processGoogleWallet(Player player, String sessionID,
                                     double amount, String postalCode) {
        System.out.println("\n--- Google Wallet ---\n");
        System.out.println("  A payment prompt of $" + amount + " has been sent to your Android device.");
        System.out.println("  Please confirm on your device...");
        waitThreeSeconds();

        Payment payment = createPayment(player, sessionID, amount, PaymentMethod.GOOGLE_WALLET, postalCode);
        payment.processPayment();
        System.out.println("\n[OK] Google Wallet payment of $" + amount + " successful!\n");
        printReceipt(payment);
        saveToCSV();
    }

    // ── PayPal ────────────────────────────────────────────────────────────────

    private void processPayPal(Player player, String sessionID, double amount,
                               String postalCode, Scanner sc) {
        System.out.println("\n--- PayPal ---\n");

        System.out.print("  PayPal Email: ");
        String email = sc.nextLine().trim();
        if (!email.contains("@") || !email.contains(".")) {
            System.out.println("[!] Invalid email address.");
            return;
        }

        System.out.println("  Connecting to PayPal...");
        waitThreeSeconds();

        Payment payment = createPayment(player, sessionID, amount, PaymentMethod.PAYPAL, postalCode);
        payment.processPayment();
        System.out.println("\n[OK] PayPal payment of $" + amount + " successful!\n");
        printReceipt(payment);
        saveToCSV();
    }

    // ── Card validation helpers ───────────────────────────────────────────────

    private String detectCardType(String cardNumber) {
        if (cardNumber.startsWith("4") && cardNumber.length() == 16)
            return "Visa";
        if (cardNumber.startsWith("5") && cardNumber.length() == 16)
            return "Mastercard";
        if ((cardNumber.startsWith("34") || cardNumber.startsWith("37")) && cardNumber.length() == 15)
            return "Amex";
        return "UNKNOWN";
    }

    private boolean isCardExpired(String expiry) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth cardExpiry = YearMonth.parse(expiry, formatter);
            return cardExpiry.isBefore(YearMonth.now());
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isValidCanadianPostal(String postal) {
        return postal.matches("[A-Za-z][0-9][A-Za-z][ ]?[0-9][A-Za-z][0-9]");
    }

    // ── Postal code persistence ───────────────────────────────────────────────

    private void savePostalCodes() {
        try {
            new java.io.File("data").mkdirs();
            FileWriter writer = new FileWriter(POSTAL_CSV_FILE);
            writer.write("playerID,postalCode\n");
            for (Map.Entry<String, String> entry : playerPostalCodes.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not save postal codes: " + e.getMessage());
        }
    }

    private void loadPostalCodes() {
        try {
            java.io.File file = new java.io.File(POSTAL_CSV_FILE);
            if (!file.exists()) return;

            BufferedReader reader = new BufferedReader(new FileReader(POSTAL_CSV_FILE));
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.isBlank()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    playerPostalCodes.put(parts[0].trim(), parts[1].trim());
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Could not load postal codes: " + e.getMessage());
        }
    }

    // ── Super admin payment view ──────────────────────────────────────────────

    public void viewPaymentsAsAdmin(String superAdminPasswordHash, Scanner sc) {
        System.out.print("\n  Enter Super Admin password to view payments: ");
        String entered = sc.nextLine().trim();

        if (!entered.equals(superAdminPasswordHash)) {
            System.out.println("[!] Incorrect password. Access denied.");
            return;
        }

        try {
            java.io.File file = new java.io.File(CSV_FILE);
            if (!file.exists()) {
                System.out.println("No payments recorded yet.");
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE));
            String line;
            System.out.println("\n--- Payment Records ---\n");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
            reader.close();

        } catch (IOException e) {
            System.out.println("Could not read payments: " + e.getMessage());
        }
    }

    // ── Common helpers ────────────────────────────────────────────────────────

    private Payment createPayment(Player player, String sessionID, double amount,
                                  PaymentMethod method, String postalCode) {
        String paymentID = "PAY-" + idCounter;
        idCounter++;
        Payment payment = new Payment(
            paymentID,
            String.valueOf(player.getPlayerId()),
            player.getName(),
            sessionID,
            amount,
            method,
            postalCode
        );
        payments.add(payment);
        return payment;
    }

    private void waitThreeSeconds() {
        try {
            for (int i = 3; i > 0; i--) {
                System.out.println("  " + i + "...");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void printReceipt(Payment payment) {
        System.out.println("  --- Receipt ---");
        System.out.println("  Payment ID  : " + payment.getPaymentID());
        System.out.println("  Player      : " + payment.getPlayerName());
        System.out.println("  Session     : " + payment.getSessionID());
        System.out.println("  Amount      : $" + payment.getAmount());
        System.out.println("  Method      : " + payment.getMethod());
        System.out.println("  Postal Code : " + payment.getPostalCode());
        System.out.println("  Status      : " + payment.getStatus());
        System.out.println("  ---------------");
    }

    private void saveToCSV() {
        try {
            new java.io.File("data").mkdirs();
            FileWriter writer = new FileWriter(CSV_FILE);
            writer.write("paymentID,playerID,playerName,sessionID,amount,method,postalCode,status\n");
            for (Payment p : payments) {
                writer.write(
                    p.getPaymentID()  + "," +
                    p.getPlayerID()   + "," +
                    p.getPlayerName() + "," +
                    p.getSessionID()  + "," +
                    p.getAmount()     + "," +
                    p.getMethod()     + "," +
                    p.getPostalCode() + "," +
                    p.getStatus()     + "\n"
                );
            }
            writer.close();
            System.out.println("Payment saved to " + CSV_FILE);
        } catch (IOException e) {
            System.out.println("Could not save payment: " + e.getMessage());
        }
    }

    public List<Payment> getAllPayments() { return payments; }

    public List<Payment> getPaymentsForPlayer(Long playerId) {
        List<Payment> result = new ArrayList<>();
        for (Payment p : payments) {
            if (p.getPlayerID().equals(String.valueOf(playerId))) {
                result.add(p);
            }
        }
        return result;
    }
}