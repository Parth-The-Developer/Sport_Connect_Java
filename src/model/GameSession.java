package model;
import java.time.LocalDate;

public class GameSession {

    // ── Enum (lives here) ─────────────────────────────────────────────────────
    public enum SessionStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private String sessionID;
    private String teamID;
    private LocalDate date;
    private String time;
    private String venue;
    private SessionStatus status;

    // ── Constructor ───────────────────────────────────────────────────────────
    public GameSession(String sessionID, String teamID, LocalDate date,
            String time, String venue) {
    }

    // ── Getters ───────────────────────────────────────────────────────────────
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

    // ── Methods ───────────────────────────────────────────────────────────────
    public void markCompleted() {
    }

    public void cancel() {
    }

    @Override
    public String toString() {
        return "";
    }
}
