package model;

import java.time.LocalDateTime;

public class Booking {

    public enum Status {
        CONFIRMED,
        CANCELLED
    }

    private String bookingID;
    private String sessionID;
    private String teamID;
    private LocalDateTime bookingTime;
    private Status status;

    public Booking(String bookingID, String sessionID, String teamID) {
        this.bookingID = bookingID;
        this.sessionID = sessionID;
        this.teamID = teamID;
        this.bookingTime = LocalDateTime.now();
        this.status = Status.CONFIRMED;
    }

    public String getBookingID() { return bookingID; }
    public String getSessionID() { return sessionID; }
    public String getTeamID() { return teamID; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public Status getStatus() { return status; }

    public boolean isActive() {
        return status == Status.CONFIRMED;
    }

    public void cancelBooking() {
        status = Status.CANCELLED;
    }

    @Override
    public String toString() {
        return "Booking[" + bookingID + "] "
                + "Session=" + sessionID
                + " Team=" + teamID
                + " Status=" + status
                + " Time=" + bookingTime;
    }
}