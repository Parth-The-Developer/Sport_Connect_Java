 package model;   import java.time.LocalDate;

public class FriendRequest {

    // ── Enum (lives here) ─────────────────────────────────────────────────────
    public enum RequestStatus {
        PENDING, ACCEPTED, DECLINED
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private String requestID;
    private String fromPlayerID;
    private String toPlayerID;
    private RequestStatus status;
    private LocalDate dateSent;

    // ── Constructor ───────────────────────────────────────────────────────────
    public FriendRequest(String requestID, String fromPlayerID, String toPlayerID) {
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getRequestID() {
        return requestID;
    }

    public String getFromPlayerID() {
        return fromPlayerID;
    }

    public String getToPlayerID() {
        return toPlayerID;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public LocalDate getDateSent() {
        return dateSent;
    }

    // ── Methods ───────────────────────────────────────────────────────────────
    public void accept() {
    }

    public void decline() {
    }

    @Override
    public String toString() {
        return "";
    }
}
