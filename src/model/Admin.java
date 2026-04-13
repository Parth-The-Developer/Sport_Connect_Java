package model;

import enums.PaymentStatus;
import java.time.LocalDateTime;

/**
 * Admin extends Player — an admin IS a player with elevated privileges.
 *
 * Sahil  → constructor, getRole(), getAdminLevel(), viewAllUsers(), updateLastLogin()
 * Parth  → cancelSession(), processRefund(), isRefundEligible(), dateToMillis()
 */
public class Admin extends Player {

    private String        fullName;
    private int           adminLevel;   // 1 = regular admin, 2 = super admin
    private LocalDateTime lastLoginAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Admin(String username, String password, String email, String fullName) {
        super();
        setName(username);
        setPasswordHash(password);
        setEmail(email);
        this.fullName   = fullName;
        this.adminLevel = 1;
    }

    public Admin(Long adminId, String username, String password,
                 String email, String fullName) {
        this(username, password, email, fullName);
        setPlayerId(adminId);
    }

    public Admin(Long adminId, String username, String password,
                 String email, String fullName, int adminLevel) {
        this(adminId, username, password, email, fullName);
        this.adminLevel = adminLevel;
    }

    // ── Role ──────────────────────────────────────────────────────────────────

    @Override
    public String getRole() {
        return adminLevel >= 2 ? "SUPER_ADMIN" : "ADMIN";
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String        getFullName()         { return fullName; }
    public void          setFullName(String v) { this.fullName = v; }

    @Override
    public String        getUsername()         { return getName(); }

    public int           getAdminLevel()           { return adminLevel; }
    public void          setAdminLevel(int v)       { this.adminLevel = v; }

    public LocalDateTime getLastLoginAt()          { return lastLoginAt; }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
        setUpdatedAt(LocalDateTime.now());
    }

    public boolean isSuperAdmin() { return adminLevel >= 2; }

    // ── Permissions ───────────────────────────────────────────────────────────

    public boolean hasPermission(String permission) {
        if (!isActive()) return false;
        if (isSuperAdmin()) return true;
        return !permission.equals("DELETE_ADMIN")
            && !permission.equals("MANAGE_SYSTEM_SETTINGS");
    }

    // ── View all users (Sahil) ────────────────────────────────────────────────

    public void viewAllUsers(Player[] players, int count) {
        if (count == 0) {
            System.out.println("No users registered yet.");
            return;
        }
        System.out.println("\n--- All Registered Users (" + count + ") ---");
        for (int i = 0; i < count; i++) {
            System.out.println((i + 1) + ". [" + players[i].getRole() + "] "
                               + players[i].toString());
        }
    }

    // ── Cancel session + refund (Parth) ──────────────────────────────────────

    public void cancelSession(String sessionId,
                              GameSession[] sessions, int sessionCount,
                              Payment[]     payments, int paymentCount) {
        GameSession target = null;
        for (int i = 0; i < sessionCount; i++) {
            if (sessions[i].getSessionID().equals(sessionId)) {
                target = sessions[i];
                break;
            }
        }
        if (target == null) {
            System.out.println("Session not found.");
            return;
        }

        boolean refundOk = isRefundEligible(target.getDate().toString());
        target.cancel();
        System.out.println("Session " + sessionId + " cancelled.");

        if (refundOk) {
            processRefund(sessionId, payments, paymentCount);
        } else {
            System.out.println("Less than 24 hrs notice — no refund issued.");
        }
    }

    public void processRefund(String sessionId,
                              Payment[] payments, int count) {
        boolean found = false;
        for (int i = 0; i < count; i++) {
            if (payments[i].getSessionID().equals(sessionId)
                    && payments[i].getStatus() == PaymentStatus.PAID) {
                payments[i].refund();
                System.out.println("Refund issued: $" + payments[i].getAmount()
                        + " to playerID " + payments[i].getPlayerID());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No paid payments found for session " + sessionId);
        }
    }

    private boolean isRefundEligible(String dateStr) {
        try {
            String[] parts  = dateStr.split("-");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int d = Integer.parseInt(parts[2]);
            long hoursUntil = (dateToMillis(y, m, d) - System.currentTimeMillis())
                              / 3_600_000L;
            return hoursUntil >= 24;
        } catch (Exception e) {
            return false;
        }
    }

    private long dateToMillis(int year, int month, int day) {
        int[] dpm = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        long days = 0;
        for (int y = 1970; y < year; y++)  days += (y % 4 == 0) ? 366 : 365;
        for (int mo = 1; mo < month; mo++) {
            days += dpm[mo - 1];
            if (mo == 2 && year % 4 == 0) days++;
        }
        days += (day - 1);
        return days * 24L * 60 * 60 * 1000;
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "[ADMIN Lvl " + adminLevel + "] " + super.toString();
    }
}