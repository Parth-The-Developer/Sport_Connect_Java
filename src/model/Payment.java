
public class Payment {

    // ── Enums (live here) ─────────────────────────────────────────────────────
    public enum PaymentMethod {
        CARD, PAYPAL
    }

    public enum PaymentStatus {
        PAID, REFUNDED
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private String paymentID;
    private String playerID;
    private String sessionID;
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Payment(String paymentID, String playerID, String sessionID,
            double amount, PaymentMethod method) {
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getPaymentID() {
        return paymentID;
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getSessionID() {
        return sessionID;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    // ── Methods ───────────────────────────────────────────────────────────────
    public void processPayment() {
    }

    public void refund() {
    }

    @Override
    public String toString() {
        return "";
    }
}
