package model;

import java.time.LocalDate;

public class GameSession {

    public enum SessionStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }

    private String sessionID;
    private String teamID;
    private LocalDate date;
    private String time;
    private String venue;
    private SessionStatus status;
    private String sport;

    public GameSession(String sessionID, String teamID, 
        String sport, LocalDate date, String time, String venue) {
        this.sessionID = sessionID;
        this.teamID = teamID;
        this.sport = sport;
        this.date = date;
        this.time = time;
        this.venue = venue;
        this.status = SessionStatus.SCHEDULED;

    }

    public void setStatus(SessionStatus status) {
    this.status = status;
    }

    public String getSessionID() { return sessionID; }
    public String getTeamID() { return teamID; }
    public LocalDate getDate() { return date; }
    public String getTime() { return time; }
    public String getVenue() { return venue; }
    public SessionStatus getStatus() { return status; }

    public void markCompleted() {
        status = SessionStatus.COMPLETED;
    }

    public void cancel() {
        status = SessionStatus.CANCELLED;
    }

    public String getSport() {
        return sport;
    }

    /** Plain label for console or API output (no leading padding). */
    public String getStatusDisplay() {
        return switch (status) {
            case SCHEDULED -> "SCHEDULED";
            case COMPLETED -> "COMPLETED";
            case CANCELLED -> "CANCELLED";
        };
    }

    @Override
    public String toString() {
        return "\nSESSION [" + sessionID + "]"
                + "\nTeam: " + teamID
                + "\nDate: " + date
                + "\nTime: " + time
                + "\nVenue: " + venue
                + "\nStatus: " + status;
    }
}