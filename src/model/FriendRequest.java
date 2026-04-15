package model;

import enums.RequestStatus;
import java.time.LocalDate;

public class FriendRequest {

    // ── Fields ────────────────────────────────────────────────────────────────
    private String requestID;
    private String fromPlayerID;
    private String toPlayerID;
    private RequestStatus status;
    private LocalDate dateSent;

    // ── Constructor ───────────────────────────────────────────────────────────
    public FriendRequest(String requestID, String fromPlayerID, String toPlayerID) {
        this.requestID = requestID;
        this.fromPlayerID = fromPlayerID;
        this.toPlayerID = toPlayerID;
        this.status = RequestStatus.PENDING;
        this.dateSent = LocalDate.now();
    }

    public FriendRequest(String requestID, String fromPlayerID, String toPlayerID,
                         RequestStatus status, LocalDate dateSent) {
        this.requestID = requestID;
        this.fromPlayerID = fromPlayerID;
        this.toPlayerID = toPlayerID;
        this.status = status;
        this.dateSent = dateSent;
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
        this.status = RequestStatus.ACCEPTED;
    }

    public void decline() {
        this.status = RequestStatus.DECLINED;
    }

    @Override
    public String toString() {
        return String.format(
            "requestId=%s, from=%s, to=%s, status=%s, sentOn=%s",
            requestID, fromPlayerID, toPlayerID, status, dateSent
        );
    }
}
