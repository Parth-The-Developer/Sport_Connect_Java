package service;

import model.GameSession;
import model.Team;
import model.Booking;

import java.time.LocalDate;
import java.util.ArrayList;

public class GameSessionService {

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

    public Booking bookSession(String sessionID, String teamID) {

        GameSession session = getSessionByID(sessionID);
        Team team = teamService.getTeamByID(teamID);

        if (session == null || team == null) return null;

        // SPORT CHECK (MOVED HERE)
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

    public boolean cancelBooking(String id) {
        for (Booking b : bookings) {
            if (b.getBookingID().equalsIgnoreCase(id)) {
                b.cancelBooking();
                return true;
            }
        }
        return false;
    }

    public enum CancelBookingAuthResult {
        BOOKING_NOT_FOUND,
        TEAM_NOT_FOUND,
        NOT_CAPTAIN,
        SUCCESS,
        FAILED
    }

    /** Cancels a booking if the actor is admin or team captain for the booking's team. */
    public CancelBookingAuthResult cancelBookingAuthorized(String bookingId, String actingPlayerId, boolean isAdmin) {
        Booking booking = getBookingByID(bookingId);
        if (booking == null)
            return CancelBookingAuthResult.BOOKING_NOT_FOUND;
        Team team = teamService.getTeamByID(booking.getTeamID());
        if (team == null)
            return CancelBookingAuthResult.TEAM_NOT_FOUND;
        if (!isAdmin && !team.getCaptainID().equals(actingPlayerId))
            return CancelBookingAuthResult.NOT_CAPTAIN;
        return cancelBooking(bookingId) ? CancelBookingAuthResult.SUCCESS : CancelBookingAuthResult.FAILED;
    }

    public enum BookSessionForMemberResult {
        TEAM_NOT_FOUND,
        NOT_TEAM_MEMBER,
        BOOKING_FAILED,
        SUCCESS
    }

    /** Books a session for a team only if the player is a member of that team. */
    public BookSessionForMemberResult bookSessionForTeamMember(String sessionID, String teamID, String playerId) {
        Team team = teamService.getTeamByID(teamID);
        if (team == null)
            return BookSessionForMemberResult.TEAM_NOT_FOUND;
        if (!team.getMemberIDs().contains(playerId))
            return BookSessionForMemberResult.NOT_TEAM_MEMBER;
        Booking b = bookSession(sessionID, teamID);
        return b != null ? BookSessionForMemberResult.SUCCESS : BookSessionForMemberResult.BOOKING_FAILED;
    }

    // This method searches for a booking using its ID
    // It is used before operations like canceling a booking
    // to ensure the booking exists and to retrieve its details
    public Booking getBookingByID(String id) {

        // Loop through all bookings in the system
        for (Booking b : bookings) {

            // Check if the booking ID matches the input (case-insensitive)
            if (b.getBookingID().equalsIgnoreCase(id)) {
                return b;
            }
        }

        // If no booking is found, return null
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

    public enum CancelSessionResult {
        NOT_FOUND,
        ALREADY_CANCELLED,
        SUCCESS
    }

    public enum CompleteSessionResult {
        NOT_FOUND,
        ALREADY_CANCELLED,
        ALREADY_COMPLETED,
        SUCCESS
    }

    /** Cancels the session and cancels all confirmed bookings for that session. */
    public CancelSessionResult cancelSessionAndRelatedBookings(String sessionID) {
        GameSession s = getSessionByID(sessionID);
        if (s == null)
            return CancelSessionResult.NOT_FOUND;
        if (s.getStatus() == GameSession.SessionStatus.CANCELLED)
            return CancelSessionResult.ALREADY_CANCELLED;
        s.cancel();
        for (Booking b : bookings) {
            if (b.getSessionID().equalsIgnoreCase(sessionID) && b.getStatus() == Booking.Status.CONFIRMED)
                b.cancelBooking();
        }
        return CancelSessionResult.SUCCESS;
    }

    public CompleteSessionResult completeSessionValidated(String sessionID) {
        GameSession s = getSessionByID(sessionID);
        if (s == null)
            return CompleteSessionResult.NOT_FOUND;
        if (s.getStatus() == GameSession.SessionStatus.CANCELLED)
            return CompleteSessionResult.ALREADY_CANCELLED;
        if (s.getStatus() == GameSession.SessionStatus.COMPLETED)
            return CompleteSessionResult.ALREADY_COMPLETED;
        s.markCompleted();
        return CompleteSessionResult.SUCCESS;
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