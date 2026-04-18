package model;

import enums.RequestStatus;
import java.time.LocalDate;

public class FriendRequest {

    private String requestID;
    private String fromPlayerID;
    private String toPlayerID;
    private RequestStatus status;
    private LocalDate dateSent;

    public FriendRequest(String requestID, String fromPlayerID, String toPlayerID, String dateSentStr) {
        this.requestID = requestID;
        this.fromPlayerID = fromPlayerID;
        this.toPlayerID = toPlayerID;
        this.dateSent = LocalDate.parse(dateSentStr);
        this.status = RequestStatus.PENDING;
    }

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

    public void accept() {
        this.status = RequestStatus.ACCEPTED;
    }

    public void decline() {
        this.status = RequestStatus.DECLINED;
    }

    @Override
    public String toString() {
        return requestID + " | " + fromPlayerID + " -> " + toPlayerID + " | " + status;
    }
}
