package model;

import enums.PaymentMethod;
import enums.PaymentStatus;

public class Payment {

    private String        paymentID;
    private String        playerID;
    private String        playerName;
    private String        sessionID;
    private double        amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String        postalCode;

    public Payment(String paymentID, String playerID, String playerName,
                   String sessionID, double amount, PaymentMethod method,
                   String postalCode) {
        this.paymentID  = paymentID;
        this.playerID   = playerID;
        this.playerName = playerName;
        this.sessionID  = sessionID;
        this.amount     = amount;
        this.method     = method;
        this.postalCode = postalCode;
        this.status     = PaymentStatus.PENDING;
    }

    public String        getPaymentID()  { return paymentID;  }
    public String        getPlayerID()   { return playerID;   }
    public String        getPlayerName() { return playerName; }
    public String        getSessionID()  { return sessionID;  }
    public double        getAmount()     { return amount;     }
    public PaymentMethod getMethod()     { return method;     }
    public PaymentStatus getStatus()     { return status;     }
    public String        getPostalCode() { return postalCode; }

    public void processPayment() {
        this.status = PaymentStatus.PAID;
        System.out.println("Payment processed successfully.");
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
        System.out.println("Payment refunded.");
    }

    @Override
    public String toString() {
        return "PaymentID: " + paymentID + ", Player: " + playerName +
               ", Session: " + sessionID + ", Amount: $" + amount +
               ", Method: " + method + ", Status: " + status +
               ", Postal: " + postalCode;
    }
}