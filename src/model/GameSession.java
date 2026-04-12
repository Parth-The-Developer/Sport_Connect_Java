package model;

import enums.SessionStatus;
import java.time.LocalDate;

public class GameSession {

    private String sessionID;
    private String teamID;
    private LocalDate date;
    private String time;
    private String venue;
    private SessionStatus status;

    public GameSession(String sessionID, String teamID, String dateStr, String time, String venue) {
        this.sessionID = sessionID;
        this.teamID = teamID;
        this.date = LocalDate.parse(dateStr);
        this.time = time;
        this.venue = venue;
        this.status = SessionStatus.SCHEDULED;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getTeamID() {
        return teamID;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getVenue() {
        return venue;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void markCompleted() {
        this.status = SessionStatus.COMPLETED;
    }

    public void cancel() {
        this.status = SessionStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return sessionID + " | " + date + " " + time + " @ " + venue + " [" + status + "]";
    }
}
