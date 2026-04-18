package model;

import enums.PaymentMethod;
import enums.PaymentStatus;

public class Payment {

    private String paymentID;
    private String playerID;
    private String sessionID;
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status;

    public Payment(String paymentID, String playerID, String sessionID,
            double amount, PaymentMethod method) {
        this.paymentID = paymentID;
        this.playerID = playerID;
        this.sessionID = sessionID;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PAID;
    }

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

    public void processPayment() {
        this.status = PaymentStatus.PAID;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    @Override
    public String toString() {
        return paymentID + " | " + playerID + " | $" + amount + " | " + method + " | " + status;
    }
}
