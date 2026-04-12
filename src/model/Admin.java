package model;

import enums.SkillLevel;

public class Admin extends Player {          // Inheritance

    // ── Encapsulation ─────────────────────────────────────────────────────────
    private int adminLevel;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Admin(String playerID, String username, String password,
            String fullName, String sport, SkillLevel skillLevel,
            String city, String availability, int adminLevel) {
        super(playerID, username, password, fullName, sport, skillLevel, city, availability);
        this.adminLevel = adminLevel;
    }

    // ── Getter ────────────────────────────────────────────────────────────────
    public int getAdminLevel() {
        return adminLevel;
    }

    // ── Polymorphism: overrides Player.getRole() ──────────────────────────────
    @Override
    public String getRole() {
        return "ADMIN";
    }

    // ── Cancel a session with refund logic ────────────────────────────────────
    public void cancelSession(String sessionID, GameSession[] sessionList, int sessionCount,
            Payment[] paymentList, int paymentCount) {
        GameSession target = null;
        for (int i = 0; i < sessionCount; i++) {
            if (sessionList[i].getSessionID().equals(sessionID)) {
                target = sessionList[i];
                break;
            }
        }
        if (target == null) {
            System.out.println("Session not found.");
            return;
        }

        boolean refundEligible = isRefundEligible(target.getDate().toString());
        target.cancel();
        System.out.println("Session " + sessionID + " cancelled.");

        if (refundEligible) {
            processRefund(sessionID, paymentList, paymentCount);
        } else {
            System.out.println("Cancellation less than 24 hours before session. No refund issued.");
        }
    }

    // ── Check if cancellation is 24+ hours before session ────────────────────
    private boolean isRefundEligible(String sessionDate) {
        try {
            String[] parts = sessionDate.split("-");
            int sessionYear = Integer.parseInt(parts[0]);
            int sessionMonth = Integer.parseInt(parts[1]);
            int sessionDay = Integer.parseInt(parts[2]);
            long nowMillis = System.currentTimeMillis();
            long sessionMillis = dateToMillis(sessionYear, sessionMonth, sessionDay);
            long diffHours = (sessionMillis - nowMillis) / (1000 * 60 * 60);
            return diffHours >= 24;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Convert date to milliseconds (no imports) ─────────────────────────────
    private long dateToMillis(int year, int month, int day) {
        int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        long days = 0;
        for (int y = 1970; y < year; y++) {
            days += (y % 4 == 0) ? 366 : 365;
        }
        for (int m = 1; m < month; m++) {
            days += daysInMonth[m - 1];
            if (m == 2 && year % 4 == 0) {
                days++;
            }
        }
        days += day - 1;
        return days * 24 * 60 * 60 * 1000L;
    }

    // ── Process refunds for all payments in a session ─────────────────────────
    public void processRefund(String sessionID, Payment[] paymentList, int paymentCount) {
        boolean anyRefund = false;
        for (int i = 0; i < paymentCount; i++) {
            if (paymentList[i].getSessionID().equals(sessionID)
                    && paymentList[i].getStatus() == enums.PaymentStatus.PAID) {
                paymentList[i].refund();
                System.out.println("Refund issued: $" + paymentList[i].getAmount()
                        + " to playerID " + paymentList[i].getPlayerID());
                anyRefund = true;
            }
        }
        if (!anyRefund) {
            System.out.println("No paid payments found for session " + sessionID + ".");
        }
    }

    // ── View all registered users ─────────────────────────────────────────────
    public void viewAllUsers(Player[] playerList, int playerCount) {
        if (playerCount == 0) {
            System.out.println("No users registered yet.");
            return;
        }
        System.out.println("\n--- All Registered Users (" + playerCount + ") ---");
        for (int i = 0; i < playerCount; i++) {
            System.out.println((i + 1) + ". [" + playerList[i].getRole() + "] "
                    + playerList[i].toString());
        }
    }

    // ── toString ──────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "[ADMIN Lvl " + adminLevel + "] " + super.toString();
    }
}
