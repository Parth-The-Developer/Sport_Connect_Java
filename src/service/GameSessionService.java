package service;

import model.GameSession;
import model.Team;
import model.Booking;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Session and booking logic. Result codes are {@code String} constants (no nested enums)
 * so compilation produces a single {@code GameSessionService.class} file.
 */
public class GameSessionService {

    public static final String R_BOOK_SUCCESS = "BOOK_SUCCESS";
    public static final String R_BOOK_SESSION_NOT_FOUND = "SESSION_NOT_FOUND";
    public static final String R_BOOK_TEAM_NOT_FOUND = "TEAM_NOT_FOUND";
    public static final String R_BOOK_NOT_TEAM_MEMBER = "NOT_TEAM_MEMBER";
    public static final String R_BOOK_FAILED = "BOOKING_FAILED";

    public static final String R_CANCEL_SESSION_SUCCESS = "CANCEL_SESSION_SUCCESS";
    public static final String R_CANCEL_SESSION_NOT_FOUND = "CANCEL_SESSION_NOT_FOUND";
    public static final String R_CANCEL_SESSION_TEAM_NOT_FOUND = "CANCEL_SESSION_TEAM_NOT_FOUND";
    public static final String R_CANCEL_SESSION_NOT_CAPTAIN = "CANCEL_SESSION_NOT_CAPTAIN";
    public static final String R_CANCEL_SESSION_ALREADY_CANCELLED = "CANCEL_SESSION_ALREADY_CANCELLED";

    public static final String R_CANCEL_BOOKING_NOT_FOUND = "CANCEL_BOOKING_NOT_FOUND";
    public static final String R_CANCEL_BOOKING_TEAM_NOT_FOUND = "CANCEL_BOOKING_TEAM_NOT_FOUND";
    public static final String R_CANCEL_BOOKING_NOT_CAPTAIN = "CANCEL_BOOKING_NOT_CAPTAIN";
    public static final String R_CANCEL_BOOKING_SUCCESS = "CANCEL_BOOKING_SUCCESS";
    public static final String R_CANCEL_BOOKING_FAILED = "CANCEL_BOOKING_FAILED";

    public static final String R_BOOK_FOR_MEMBER_TEAM_NOT_FOUND = "BOOK_FOR_MEMBER_TEAM_NOT_FOUND";
    public static final String R_BOOK_FOR_MEMBER_NOT_MEMBER = "BOOK_FOR_MEMBER_NOT_MEMBER";
    public static final String R_BOOK_FOR_MEMBER_FAILED = "BOOK_FOR_MEMBER_FAILED";
    public static final String R_BOOK_FOR_MEMBER_SUCCESS = "BOOK_FOR_MEMBER_SUCCESS";

    public static final String R_CANCEL_RELATED_NOT_FOUND = "CANCEL_RELATED_NOT_FOUND";
    public static final String R_CANCEL_RELATED_ALREADY_CANCELLED = "CANCEL_RELATED_ALREADY_CANCELLED";
    public static final String R_CANCEL_RELATED_SUCCESS = "CANCEL_RELATED_SUCCESS";

    public static final String R_COMPLETE_NOT_FOUND = "COMPLETE_NOT_FOUND";
    public static final String R_COMPLETE_ALREADY_CANCELLED = "COMPLETE_ALREADY_CANCELLED";
    public static final String R_COMPLETE_ALREADY_COMPLETED = "COMPLETE_ALREADY_COMPLETED";
    public static final String R_COMPLETE_SUCCESS = "COMPLETE_SUCCESS";

    public static final String R_COMPLETE_AUTH_SESSION_NOT_FOUND = "COMPLETE_AUTH_SESSION_NOT_FOUND";
    public static final String R_COMPLETE_AUTH_TEAM_NOT_FOUND = "COMPLETE_AUTH_TEAM_NOT_FOUND";
    public static final String R_COMPLETE_AUTH_NOT_CAPTAIN = "COMPLETE_AUTH_NOT_CAPTAIN";
    public static final String R_COMPLETE_AUTH_ALREADY_CANCELLED = "COMPLETE_AUTH_ALREADY_CANCELLED";
    public static final String R_COMPLETE_AUTH_ALREADY_COMPLETED = "COMPLETE_AUTH_ALREADY_COMPLETED";
    public static final String R_COMPLETE_AUTH_SUCCESS = "COMPLETE_AUTH_SUCCESS";

    private ArrayList<GameSession> sessions = new ArrayList<>();
    private ArrayList<Booking> bookings = new ArrayList<>();

    private int sessionCounter = 1;
    private int bookingCounter = 1;
    private TeamService teamService;

    public GameSessionService(TeamService teamService) {
        this.teamService = teamService;
    }

    public GameSession createSession(String teamId, String sport, LocalDate date, String time, String venue) {

        if (date.isBefore(LocalDate.now())) return null;

        for (GameSession s : sessions) {
            if (s.getDate().equals(date)
                    && s.getTime().equalsIgnoreCase(time)
                    && s.getVenue().equalsIgnoreCase(venue)
                    && s.getStatus() == GameSession.SessionStatus.SCHEDULED) {
                return null;
            }
        }

        GameSession s = new GameSession("S" + sessionCounter++, teamId, sport, date, time, venue);
        sessions.add(s);
        return s;
    }

    /** Last error for {@link #createSessionForTeamMember}; null if none. */
    private String lastCreateSessionForMemberError;

    public String getLastCreateSessionForMemberError() {
        return lastCreateSessionForMemberError;
    }

    /**
     * Creates a session for a team when the actor is a member. Returns session or null;
     * on null, see {@link #getLastCreateSessionForMemberError()}.
     */
    public GameSession createSessionForTeamMember(String actorPlayerId, String teamId, LocalDate date, String time, String venue) {
        lastCreateSessionForMemberError = null;
        Team team = teamService.getTeamByID(teamId);
        if (team == null) {
            lastCreateSessionForMemberError = "TEAM_NOT_FOUND";
            return null;
        }
        if (!team.getMemberIDs().contains(actorPlayerId)) {
            lastCreateSessionForMemberError = "NOT_TEAM_MEMBER";
            return null;
        }
        GameSession session = createSession(teamId, team.getSport(), date, time, venue);
        if (session == null) {
            lastCreateSessionForMemberError = "INVALID_DATE_OR_CONFLICT";
            return null;
        }
        return session;
    }

    public Booking bookSession(String sessionID, String teamID) {

        GameSession session = getSessionByID(sessionID);
        Team team = teamService.getTeamByID(teamID);

        if (session == null || team == null) return null;

        if (!team.getSport().equalsIgnoreCase(session.getSport())) {
            return null;
        }

        if (session.getStatus() != GameSession.SessionStatus.SCHEDULED) return null;
        if (session.getDate().isBefore(LocalDate.now())) return null;

        for (Booking b : bookings) {
            if (b.getSessionID().equals(sessionID)
                    && b.getTeamID().equals(teamID)
                    && b.getStatus() == Booking.Status.CONFIRMED) {
                return null;
            }
        }

        Booking b = new Booking("B" + bookingCounter++, sessionID, teamID);
        bookings.add(b);
        return b;
    }

    public String bookSession(String actorPlayerId, String sessionID, String teamID) {
        GameSession session = getSessionByID(sessionID);
        if (session == null) return R_BOOK_SESSION_NOT_FOUND;
        Team team = teamService.getTeamByID(teamID);
        if (team == null) return R_BOOK_TEAM_NOT_FOUND;
        if (!team.getMemberIDs().contains(actorPlayerId)) return R_BOOK_NOT_TEAM_MEMBER;
        Booking booking = bookSession(sessionID, teamID);
        return booking != null ? R_BOOK_SUCCESS : R_BOOK_FAILED;
    }

    public boolean cancelBooking(String id) {
        for (Booking b : bookings) {
            if (b.getBookingID().equalsIgnoreCase(id)) {
                b.cancelBooking();
                return true;
            }
        }
        return false;
    }

    public String cancelBookingAuthorized(String bookingId, String actingPlayerId, boolean isAdmin) {
        Booking booking = getBookingByID(bookingId);
        if (booking == null)
            return R_CANCEL_BOOKING_NOT_FOUND;
        Team team = teamService.getTeamByID(booking.getTeamID());
        if (team == null)
            return R_CANCEL_BOOKING_TEAM_NOT_FOUND;
        if (!isAdmin && !team.getCaptainID().equals(actingPlayerId))
            return R_CANCEL_BOOKING_NOT_CAPTAIN;
        return cancelBooking(bookingId) ? R_CANCEL_BOOKING_SUCCESS : R_CANCEL_BOOKING_FAILED;
    }

    public String bookSessionForTeamMember(String sessionID, String teamID, String playerId) {
        Team team = teamService.getTeamByID(teamID);
        if (team == null)
            return R_BOOK_FOR_MEMBER_TEAM_NOT_FOUND;
        if (!team.getMemberIDs().contains(playerId))
            return R_BOOK_FOR_MEMBER_NOT_MEMBER;
        Booking b = bookSession(sessionID, teamID);
        return b != null ? R_BOOK_FOR_MEMBER_SUCCESS : R_BOOK_FOR_MEMBER_FAILED;
    }

    public Booking getBookingByID(String id) {

        for (Booking b : bookings) {

            if (b.getBookingID().equalsIgnoreCase(id)) {
                return b;
            }
        }

        return null;
    }

    public ArrayList<Booking> getBookingsByTeam(String teamID) {

        ArrayList<Booking> list = new ArrayList<>();

        for (Booking b : bookings) {
            if (b.getTeamID().equalsIgnoreCase(teamID)) {
                list.add(b);
            }
        }

        return list;
    }

    public String cancelSessionAndRelatedBookings(String sessionID) {
        GameSession s = getSessionByID(sessionID);
        if (s == null)
            return R_CANCEL_RELATED_NOT_FOUND;
        if (s.getStatus() == GameSession.SessionStatus.CANCELLED)
            return R_CANCEL_RELATED_ALREADY_CANCELLED;
        s.cancel();
        for (Booking b : bookings) {
            if (b.getSessionID().equalsIgnoreCase(sessionID) && b.getStatus() == Booking.Status.CONFIRMED)
                b.cancelBooking();
        }
        return R_CANCEL_RELATED_SUCCESS;
    }

    public String cancelSession(String sessionID, String actingPlayerId, boolean isAdmin) {
        GameSession session = getSessionByID(sessionID);
        if (session == null) return R_CANCEL_SESSION_NOT_FOUND;
        Team team = teamService.getTeamByID(session.getTeamID());
        if (team == null) return R_CANCEL_SESSION_TEAM_NOT_FOUND;
        if (!isAdmin && !team.getCaptainID().equals(actingPlayerId)) return R_CANCEL_SESSION_NOT_CAPTAIN;

        String result = cancelSessionAndRelatedBookings(sessionID);
        if (R_CANCEL_RELATED_ALREADY_CANCELLED.equals(result)) return R_CANCEL_SESSION_ALREADY_CANCELLED;
        return R_CANCEL_RELATED_SUCCESS.equals(result)
                ? R_CANCEL_SESSION_SUCCESS
                : R_CANCEL_SESSION_NOT_FOUND;
    }

    public String completeSessionValidated(String sessionID) {
        GameSession s = getSessionByID(sessionID);
        if (s == null)
            return R_COMPLETE_NOT_FOUND;
        if (s.getStatus() == GameSession.SessionStatus.CANCELLED)
            return R_COMPLETE_ALREADY_CANCELLED;
        if (s.getStatus() == GameSession.SessionStatus.COMPLETED)
            return R_COMPLETE_ALREADY_COMPLETED;
        s.markCompleted();
        return R_COMPLETE_SUCCESS;
    }

    public String completeSession(String sessionID, String actingPlayerId, boolean isAdmin) {
        GameSession session = getSessionByID(sessionID);
        if (session == null) return R_COMPLETE_AUTH_SESSION_NOT_FOUND;
        Team team = teamService.getTeamByID(session.getTeamID());
        if (team == null) return R_COMPLETE_AUTH_TEAM_NOT_FOUND;
        if (!isAdmin && !team.getCaptainID().equals(actingPlayerId)) return R_COMPLETE_AUTH_NOT_CAPTAIN;

        String result = completeSessionValidated(sessionID);
        if (R_COMPLETE_NOT_FOUND.equals(result)) return R_COMPLETE_AUTH_SESSION_NOT_FOUND;
        if (R_COMPLETE_ALREADY_CANCELLED.equals(result)) return R_COMPLETE_AUTH_ALREADY_CANCELLED;
        if (R_COMPLETE_ALREADY_COMPLETED.equals(result)) return R_COMPLETE_AUTH_ALREADY_COMPLETED;
        if (R_COMPLETE_SUCCESS.equals(result)) return R_COMPLETE_AUTH_SUCCESS;
        return R_COMPLETE_AUTH_SESSION_NOT_FOUND;
    }

    public ArrayList<GameSession> getSessionsByTeam(String teamId) {
        ArrayList<GameSession> result = new ArrayList<>();
        for (GameSession s : sessions) {
            if (s.getTeamID().equalsIgnoreCase(teamId)) {
                result.add(s);
            }
        }
        return result;
    }

    public ArrayList<Booking> getBookingsBySession(String sessionId) {
        ArrayList<Booking> result = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getSessionID().equalsIgnoreCase(sessionId)) {
                result.add(b);
            }
        }
        return result;
    }

    public ArrayList<GameSession> getAllSessions() {
        return sessions;
    }

    public GameSession getSessionByID(String id) {
        for (GameSession s : sessions) {
            if (s.getSessionID().equalsIgnoreCase(id)) {
                return s;
            }
        }
        return null;
    }
}
