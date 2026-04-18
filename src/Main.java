import model.Admin;
import model.Player;
import model.Team;
import model.GameSession;
import model.Booking;

import service.AuthService;
import service.PlayerService;
import service.TeamService;
import service.GameSessionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {

    // ================= SERVICES =================
    static final AuthService authService = new AuthService();
    static final PlayerService playerService = new PlayerService();
    static final TeamService teamService = new TeamService(playerService);
    static final GameSessionService gameSessionService = new GameSessionService(teamService);

    // ================= SESSION =================
    static Player loggedInPlayer = null;
    static Admin loggedInAdmin = null;
    static String sessionToken = null;

    static final Scanner sc = new Scanner(System.in);
    private static final ConsoleUi ui = new ConsoleUi(sc);

    public static void main(String[] args) {
        SeedData.seed(authService, playerService);
        mainMenu();
        sc.close();
    }

    // =====================================================
    // MAIN MENU
    // =====================================================
    static void mainMenu() {
        while (true) {
            ui.printBanner();
            System.out.println("  1. Player Portal");
            System.out.println("  2. Admin Portal");
            System.out.println("  0. Exit");
            System.out.print("\n  Choice: ");
            switch (ui.readInt()) {
                case 1 -> playerPortal();
                case 2 -> adminPortal();
                case 0 -> { System.out.println("\n  Goodbye!\n"); return; }
                default -> ui.warn("Invalid option.");
            }
        }
    }

    // =====================================================
    // PLAYER PORTAL
    // =====================================================
    static void playerPortal() {

        while (true) {
            ui.printHeader("Player Portal");
            if (loggedInPlayer == null) {
                System.out.println("  1. Sign Up");
                System.out.println("  2. Log In");
                System.out.println("  0. Back");
                System.out.print("\n  Choice: ");
                switch (ui.readInt()) {
                    case 1 -> playerSignUp();
                    case 2 -> playerLogin();
                    case 0 -> { return; }
                    default -> ui.warn("Invalid option.");
                }

            } else {

                System.out.println("  Logged in as: " + loggedInPlayer.getDisplayName());
                System.out.println();
                System.out.println("  1. View My Profile");
                System.out.println("  2. Update My Profile");
                System.out.println("  3. Search Players by Sport");
                System.out.println("  4. Search Players by Skill Level");
                System.out.println("  5. Search Players by City");
                System.out.println("  6. Friend Requests     [Parth - Week 2]");
                System.out.println("  7. Chat                [Parth - Week 2]");
                System.out.println("  8. View My Teams");
                System.out.println("  9. Schedule Game");
                System.out.println("  10. Ratings & Payments  [Dhruv - Week 3]");
                System.out.println("  0. Logout");

                System.out.print("\n  Choice: ");

                switch (ui.readInt()) {
                    case 1 -> viewMyProfile();
                    case 2 -> updateMyProfile();
                    case 3 -> searchBySport();
                    case 4 -> searchBySkill();
                    case 5 -> searchByCity();
                    case 6, 7 -> ui.stub("Parth's module — coming Week 2");
                    case 8 -> viewMyTeams();
                    case 9 -> gameHub();
                    case 10    -> ui.stub("Dhruv's module — coming Week 3");
                    case 0 -> { playerLogout(); return; }
                    default -> ui.warn("Invalid option");
                }
            }
        }
    }

    // =====================================================
    // GAME HUB
    // =====================================================
    static void gameHub() {

        while (true) {
            ui.printHeader("GAME HUB");

            System.out.println("  1. Create Team");
            System.out.println("  2. View Teams");
            System.out.println("  3. Join Team");
            System.out.println("  4. Leave Team");
            System.out.println("  5. View Sessions");
            System.out.println("  6. Create Session");
            System.out.println("  7. Book Session");
            System.out.println("  8. Cancel Booking");
            System.out.println("  9. View Bookings");
            System.out.println("  10. Add Player to Team");
            System.out.println("  11. Remove Player from Team (Captain)");
            System.out.println("  0. Back");

            System.out.print("\n  Choice: ");

            switch (ui.readInt()) {
                case 1 -> createTeam();
                case 2 -> viewTeams();
                case 3 -> joinTeam();
                case 4 -> leaveTeam();
                case 5 -> viewSessionsPlayer();
                case 6 -> createSession();
                case 7 -> bookSession();
                case 8 -> cancelBooking();
                case 9 -> viewBookings();
                case 10 -> addPlayerToTeam();
                case 11 -> captainRemovePlayer();
                case 0 -> { return; }
                default -> ui.warn("Invalid option");
            }
        }
    }

    static void playerSignUp() {
        ui.printHeader("Player Sign Up");
        try {
            System.out.print("  Full name    : "); String name  = sc.nextLine().trim();
            System.out.print("  Email        : "); String email = sc.nextLine().trim();
            System.out.print("  Password     : "); String pass  = sc.nextLine().trim();
            System.out.print("  Phone        : "); String phone = sc.nextLine().trim();
            System.out.print("  Sport        : "); String sport = sc.nextLine().trim();

            Player player = authService.registerPlayer(email, name, phone, sport);
            player.setPasswordHash(pass);

            System.out.print("  City         : "); player.setCity(sc.nextLine().trim());
            System.out.print("  Skill level\n  (BEGINNER / INTERMEDIATE / ADVANCED): ");
            player.setSkill_level(sc.nextLine().trim().toUpperCase());
            System.out.print("  Age          : "); player.setAge(ui.readIntInline());
            System.out.print("  Experience (years) : "); player.setExperience(ui.readIntInline());

            playerService.addPlayer(player);
            loggedInPlayer = player;
            sessionToken   = generateSessionToken(email, "PLAYER");
            ui.ok("Welcome to SportConnect, " + player.getDisplayName() + " (ID: " + loggedInPlayer.getPlayerId() + ")");
        } catch (Exception e) {
            ui.warn("Sign-up failed: " + e.getMessage());
        }
    }

    static void playerLogin() {
        ui.printHeader("Player Log In");
        try {
            System.out.print("  Email    : "); String email = sc.nextLine().trim();
            System.out.print("  Password : "); String pass  = sc.nextLine().trim();
            sessionToken   = authService.playerLogin(email, pass);
            loggedInPlayer = playerService.getPlayerByEmail(email);
            ui.ok("Welcome back, " + loggedInPlayer.getDisplayName() + " (ID: " + loggedInPlayer.getPlayerId() + ")");
        } catch (Exception e) {
            ui.warn("Login failed: " + e.getMessage());
        }
    }

    static void playerLogout() {
        authService.logout(sessionToken);
        ui.ok("Logged out. See you next time, " + loggedInPlayer.getDisplayName() + "!");
        loggedInPlayer = null;
        sessionToken   = null;
    }

    static void viewMyProfile() {
        ui.printHeader("My Profile");
        Player p = loggedInPlayer;
        System.out.printf("  Name        : %s%n",     p.getName());
        System.out.printf("  Email       : %s%n",     p.getEmail());
        System.out.printf("  Phone       : %s%n",     p.getPhone());
        System.out.printf("  Sport       : %s%n",     p.getSport());
        System.out.printf("  Skill Level : %s%n",     p.getSkill_level());
        System.out.printf("  City        : %s%n",     p.getCity());
        System.out.printf("  Age         : %d%n",     p.getAge());
        System.out.printf("  Experience  : %d yrs%n", p.getExperience());
        System.out.printf("  Rating      : %.1f%n",   p.getRating());
        System.out.printf("  Member since: %s%n",     p.getCreatedAt().toLocalDate());
        ui.pause();
    }

    static void updateMyProfile() {
        ui.printHeader("Update My Profile");
        Player p = loggedInPlayer;
        try {
            System.out.print("  New Phone (Enter to skip)    : ");
            String phone = sc.nextLine().trim();
            if (!phone.isEmpty()) p.setPhone(phone);

            System.out.print("  New City (Enter to skip)     : ");
            String city = sc.nextLine().trim();
            if (!city.isEmpty()) p.setCity(city);

            System.out.print("  New Sport (Enter to skip)    : ");
            String sport = sc.nextLine().trim();
            if (!sport.isEmpty()) p.setSport(sport);

            System.out.print("  New Skill Level (Enter to skip)\n"
                           + "  (BEGINNER / INTERMEDIATE / ADVANCED): ");
            String skill = sc.nextLine().trim();
            if (!skill.isEmpty()) p.setSkill_level(skill.toUpperCase());

            System.out.print("  New Bio (Enter to skip)      : ");
            String bio = sc.nextLine().trim();
            if (!bio.isEmpty()) p.setBio(bio);

            playerService.updatePlayer(p.getPlayerId(), p);
            ui.ok("Profile updated successfully!");
        } catch (Exception e) {
            ui.warn("Update failed: " + e.getMessage());
        }
    }

    static void searchBySport() {
        ui.printHeader("Search by Sport");
        System.out.print("  Enter sport: ");
        ui.printPlayerList(playerService.getPlayersBySport(sc.nextLine().trim()));
    }

    static void searchBySkill() {
        ui.printHeader("Search by Skill Level");
        System.out.println("  Options: BEGINNER | INTERMEDIATE | ADVANCED");
        System.out.print("  Enter level: ");
        ui.printPlayerList(playerService.getPlayersBySkillLevel(sc.nextLine().trim()));
    }

    static void searchByCity() {
        ui.printHeader("Search by City");
        System.out.print("  Enter city: ");
        ui.printPlayerList(playerService.getPlayersByCity(sc.nextLine().trim()));
    }

    // =====================================================
    // ADMIN PORTAL
    // =====================================================
    static void adminPortal() {

        if (loggedInAdmin == null) {
            adminLogin();
            if (loggedInAdmin == null) return;
        }

        while (true) {

            ui.printHeader("ADMIN PORTAL");

            ui.printHeader("Admin Portal — " + loggedInAdmin.getUsername()
                      + "  [" + loggedInAdmin.getRole() + "]");
            System.out.println("  1. List All Players");
            System.out.println("  2. Deactivate a Player");
            System.out.println("  3. Activate a Player");
            System.out.println("  4. Delete a Player (permanent)");
            System.out.println("  5. Cancel Session & Refund [Parth - Week 2]");
            System.out.println("  6. Manage Teams");
            System.out.println("  7. Manage Game Sessions");
            System.out.println("  8. Payment Reports         [Dhruv - Week 3]");
            System.out.println("  0. Admin Logout");

            System.out.print("\n  Choice: ");

            switch (ui.readInt()) {
                case 1 -> adminListPlayers();
                case 2 -> adminDeactivatePlayer();
                case 3 -> adminActivatePlayer();
                case 4 -> adminDeletePlayer();
                case 5 -> ui.stub("Refund system pending");
                case 6 -> manageTeamsAdmin();
                case 7 -> manageGameSessionsAdmin();
                case 8 -> ui.stub("Reports pending");
                case 0 -> { adminLogout(); return; }
                default -> ui.warn("Invalid option");
            }
        }
    }

    static void adminLogin() {
        ui.printHeader("Admin Login");
        try {
            System.out.print("  Username : "); String user = sc.nextLine().trim();
            System.out.print("  Password : "); String pass = sc.nextLine().trim();
            sessionToken  = authService.adminLogin(user, pass);
            loggedInAdmin = authService.getAdminByUsername(user);
            ui.ok("Welcome, " + loggedInAdmin.getFullName() + "!");
        } catch (Exception e) {
            ui.warn("Admin login failed: " + e.getMessage());
        }
    }

    static void adminLogout() {
        authService.logout(sessionToken);
        ui.ok("Admin session ended.");
        loggedInAdmin = null;
        sessionToken  = null;
    }

    static void adminListPlayers() {
        ui.printHeader("All Players (" + playerService.getTotalPlayers() + " total)");
        ui.printPlayerList(playerService.getAllPlayers());
    }

    static void adminDeactivatePlayer() {
        ui.printHeader("Deactivate Player");
        System.out.print("  Player ID: ");
        try { playerService.deactivatePlayer((long) ui.readIntInline()); ui.ok("Player deactivated."); }
        catch (Exception e) { ui.warn(e.getMessage()); }
    }

    static void adminActivatePlayer() {
        ui.printHeader("Activate Player");
        System.out.print("  Player ID: ");
        try { playerService.activatePlayer((long) ui.readIntInline()); ui.ok("Player activated."); }
        catch (Exception e) { ui.warn(e.getMessage()); }
    }

    static void adminDeletePlayer() {
        ui.printHeader("Delete Player");
        System.out.print("  Player ID: ");
        try { playerService.deletePlayer((long) ui.readIntInline()); ui.ok("Player deleted."); }
        catch (Exception e) { ui.warn(e.getMessage()); }
    } 
    
    // =====================================================
    // TEAM SYSTEM
    // =====================================================
    static void createTeam() {

        System.out.print("  Team Name: ");
        String name = sc.nextLine();

        System.out.print("  Sport: ");
        String sport = sc.nextLine();

        String playerSport = loggedInPlayer.getSport();

        // SPORT VALIDATION (NEW)
        if (!teamService.isSportCompatible(loggedInPlayer.getSport(), sport)) {
            ui.warn("You can only create teams for your own sport: " + playerSport);
            return;
        }

        String captain = String.valueOf(loggedInPlayer.getPlayerId());

        Team t = teamService.createTeam(name, sport, captain);

        ui.ok("Team created: " + t.getTeamID());
    }

    static void viewTeams() {

        List<Team> teams = teamService.getAllTeams();

        //  NEW: handle empty case
        if (teams.isEmpty()) {
            ui.warn("No teams available.");
            return;
        }

        for (Team t : teams) {

            System.out.println("\n  TEAM [" + t.getTeamID() + "]");
            System.out.println("  Name   : " + t.getTeamName());
            System.out.println("  Sport  : " + t.getSport());

            System.out.println("  Captain: " + getPlayerName(t.getCaptainID()));

            System.out.print("  Members: ");
                for (int i = 0; i < t.getMemberIDs().size(); i++) {

                    String name = getPlayerName(t.getMemberIDs().get(i));

                    if (i == t.getMemberIDs().size() - 1) {
                        System.out.print(name);
                    } else {
                        System.out.print(name + ", ");
                    }
                }

            System.out.println();
            System.out.println("  Members: " + t.getMemberIDs().size() + "/" + Team.MAX_MEMBERS);
        }
    }

    // ==============================
    // SESSION VIEW (PLAYER SIDE)
    // ==============================
    static void viewSessionsPlayer() {

        ui.printHeader("AVAILABLE SESSIONS");

        List<GameSession> sessions = gameSessionService.getAllSessions();

        if (sessions.isEmpty()) {
            ui.warn("No available sessions at the moment.");
            ui.pause();
            return;
        }

        for (GameSession s : sessions) {

            System.out.println("\n  Session ID : " + s.getSessionID());
            System.out.println("  Team       : " + s.getTeamID());
            System.out.println("  Date       : " + s.getDate());
            System.out.println("  Time       : " + s.getTime());
            System.out.println("  Venue      : " + s.getVenue());
            System.out.println("  Status     : " + ui.formatStatus(s.getStatus()));
        }

        ui.pause();
    }

    static void joinTeam() {

        System.out.print("  Team ID: ");
        String id = sc.nextLine();

        String playerId = String.valueOf(loggedInPlayer.getPlayerId());

        String result = teamService.joinTeam(playerId, id);

        switch (result) {

            case "SUCCESS" -> ui.ok("Player successfully added to team.");

            case "TEAM_NOT_FOUND" -> ui.warn("Team not found.");

            case "PLAYER_NOT_FOUND" -> ui.warn("Player does not exist.");

            case "ALREADY_MEMBER" -> ui.warn("Player is already in this team.");

            case "TEAM_FULL" -> ui.warn("Team is full.");

            case "SPORT_MISMATCH" -> ui.warn("Cannot add player: sport does not match team.");

            default -> ui.warn("Unknown error occurred.");
        }
    }

    static void leaveTeam() {

        System.out.print("  Team ID: ");
        String id = sc.nextLine();

        String playerId = String.valueOf(loggedInPlayer.getPlayerId());

        boolean left = teamService.leaveTeam(playerId, id);

        if (left) ui.ok("You left team, [" + id + "], no more a member");
        else ui.warn("Could not leave team (not a member or team not found).");
    }

    static void addPlayerToTeam() {

        System.out.print("  Team ID: ");
        String teamId = sc.nextLine();

        Team team = teamService.getTeamByID(teamId);

        if (team == null) {
            ui.warn("Team not found.");
            return;
        }

        String currentUserId = String.valueOf(loggedInPlayer.getPlayerId());

        // Only captain can add players
        if (!team.getCaptainID().equals(currentUserId)) {
            ui.warn("Only the team captain can add players.");
            return;
        }

        // Show only same sport players
        List<Player> players = playerService.getAllPlayers();

        System.out.println("\n  Available Players (Same Sport Only): ");

        for (Player p : players) {
            if (p.getSport().equalsIgnoreCase(team.getSport())) {
                System.out.println("  " + p.getPlayerId() + " - " + p.getDisplayName());
            }
        }

        System.out.print("\n  Enter Player ID to add: ");
        String playerId = sc.nextLine();

        // Prevent adding yourself again
        if (playerId.equals(currentUserId)) {
            ui.warn("You are already in the team.");
            return;
        }

        String result = teamService.joinTeam(playerId, teamId);

        switch (result) {

            case "SUCCESS" -> ui.ok("Player successfully added to team.");

            case "TEAM_NOT_FOUND" -> ui.warn("Team not found.");

            case "PLAYER_NOT_FOUND" -> ui.warn("Player does not exist.");

            case "ALREADY_MEMBER" -> ui.warn("Player is already in this team.");

            case "TEAM_FULL" -> ui.warn("Team is full.");

            case "SPORT_MISMATCH" -> ui.warn("Cannot add player: sport does not match team.");

            default -> ui.warn("Unknown error occurred.");
        }
    }

    static void captainRemovePlayer() {

        System.out.print("  Team ID: ");
        String teamId = sc.nextLine();

        String currentUserId = String.valueOf(loggedInPlayer.getPlayerId());

        System.out.print("  Player ID to remove: ");
        String playerId = sc.nextLine();

        switch (teamService.removeMemberAsCaptain(teamId, currentUserId, playerId)) {
            case TEAM_NOT_FOUND -> ui.warn("Team not found.");
            case NOT_CAPTAIN -> ui.warn("Only the team captain can remove players.");
            case CAPTAIN_CANNOT_REMOVE_SELF -> ui.warn("Captain cannot remove themselves.");
            case PLAYER_NOT_IN_TEAM -> ui.warn("Player not found in team.");
            case SUCCESS -> ui.ok("Player removed from team.");
        }
    }

    static void viewMyTeams() {

        String pid = String.valueOf(loggedInPlayer.getPlayerId());
        boolean found = false;

        for (Team t : teamService.getAllTeams()) {

            if (t.getMemberIDs().contains(pid)) {

                System.out.println("\n  TEAM [" + t.getTeamID() + "]");
                System.out.println("  Name   : " + t.getTeamName());
                System.out.println("  Sport  : " + t.getSport());
                System.out.println("  Captain: " + getPlayerName(t.getCaptainID()));

                System.out.print("  Members: ");
                for (String id : t.getMemberIDs()) {
                    System.out.print(getPlayerName(id) + " ");
                }

                System.out.println("\n  Total Members: " + t.getMemberIDs().size());

                found = true;
            }
        }

        if (!found) ui.warn("You are not part of any teams.");
    }

    static String getPlayerName(String playerId) {
        for (Player p : playerService.getAllPlayers()) {
            if (String.valueOf(p.getPlayerId()).equals(playerId)) {
                return p.getDisplayName();
            }
        }
        return playerId; // fallback
    }    

    // =====================================================
    // SESSION SYSTEM
    // =====================================================
    static void createSession() {

        System.out.print("  Team ID: ");
        String teamID = sc.nextLine();

        // Get team first (needed for sport)
        Team team = teamService.getTeamByID(teamID);

        if (team == null) {
            ui.warn("Team not found.");
            return;
        }

        System.out.print("  Date (YYYY-MM-DD): ");
        String dateStr = sc.nextLine();

        System.out.print("  Time: ");
        String time = sc.nextLine();

        System.out.print("  Venue: ");
        String venue = sc.nextLine();

        try {
            LocalDate date = LocalDate.parse(dateStr);

            GameSession s = gameSessionService.createSession(teamID, team.getSport(), date, time, venue);

            if (s == null) ui.warn("Session creation failed: cannot schedule sessions in the past");
            else ui.ok("Session created successfully");

        } catch (Exception e) {
            ui.warn("Invalid date format. Please use YYYY-MM-DD");
        }
    }

    static void bookSession() {

        System.out.print("  Session ID: ");
        String sid = sc.nextLine();

        System.out.print("  Team ID: ");
        String tid = sc.nextLine();

        String pid = String.valueOf(loggedInPlayer.getPlayerId());

        switch (gameSessionService.bookSessionForTeamMember(sid, tid, pid)) {
            case TEAM_NOT_FOUND -> ui.warn("Team not found: invalid or non-existent Team ID");
            case NOT_TEAM_MEMBER -> ui.warn("Booking denied: You must be in the team to book");
            case BOOKING_FAILED -> ui.warn("Booking failed: session may not exist or is unavailable");
            case SUCCESS -> ui.ok("Booked successfully");
        }
    }

    static void cancelBooking() {

        System.out.print("  Booking ID: ");
        String bid = sc.nextLine();

        String pid = String.valueOf(loggedInPlayer.getPlayerId());
        boolean isAdmin = loggedInAdmin != null;

        switch (gameSessionService.cancelBookingAuthorized(bid, pid, isAdmin)) {
            case BOOKING_NOT_FOUND -> ui.warn("Booking not found");
            case TEAM_NOT_FOUND -> ui.warn("Team not found");
            case NOT_CAPTAIN -> ui.warn("Only the team captain can cancel this booking");
            case FAILED -> ui.warn("Cancel failed");
            case SUCCESS -> {
                if (isAdmin) ui.ok("Booking cancelled by admin");
                else ui.ok("Booking cancelled");
            }
        }
    }

    static void viewBookings() {

        System.out.print("  Team ID: ");
        String teamId = sc.nextLine();

        List<Booking> bookings = gameSessionService.getBookingsByTeam(teamId);

        if (bookings.isEmpty()) {
            ui.warn("No bookings found for this team.");
            ui.pause();
            return;
        }

        System.out.println("\n  BOOKINGS: ");

        for (Booking b : bookings) {
            System.out.println("  Booking ID : " + b.getBookingID());
            System.out.println("  Session ID : " + b.getSessionID());
            System.out.println("  Team ID    : " + b.getTeamID());
            System.out.println("  Status     : " + b.getStatus());
            System.out.println("------------------------");
        }

        ui.pause();
    }

    // =====================================================
    // ADMIN TEAM MGMT
    // =====================================================
    static void manageTeamsAdmin() {

        while (true) {

            ui.printHeader("MANAGE TEAMS");

            System.out.println("  1. View Teams");
            System.out.println("  2. Delete Team");
            System.out.println("  3. Remove Player from Team");
            System.out.println("  0. Back");
            System.out.print("\n  Choice: ");

            switch (ui.readInt()) {
                case 1 -> { viewTeams(); ui.pause(); }
                case 2 -> { deleteTeam(); ui.pause(); }
                case 3 -> { removePlayerFromTeam(); ui.pause(); }
                case 0 -> { return; }
                default -> ui.warn("Invalid option");
            }
        }
    }

    static void deleteTeam() {

        System.out.print("  Team ID: ");
        String id = sc.nextLine();

        Team team = teamService.getTeamByID(id);

        if (team == null) {
            ui.warn("Team not found.");
            return;
        }

        System.out.print("  Are you sure you want to delete this team? (yes/no): ");
        String confirm = sc.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes")) {
            ui.warn("Delete cancelled.");
            return;
        }

        boolean deleted = teamService.deleteTeam(id);

        if (deleted) {
            ui.ok("Team deleted successfully.");
        } else {
            ui.warn("Delete failed.");
        }
    }

    static void removePlayerFromTeam() {

        System.out.print("  Team ID: ");
        String t = sc.nextLine();

        Team team = teamService.getTeamByID(t);

        if (team == null) {
            ui.warn("Team not found.");
            return;
        }

        System.out.print("  Player ID: ");
        String p = sc.nextLine();

        if (!team.getMemberIDs().contains(p)) {
            ui.warn("Player is not in this team.");
            return;
        }

        boolean removed = teamService.removeMemberAsAdmin(t, p);

        if (removed) {
            ui.ok("Player successfully removed from team.");
        } else {
            ui.warn("Removal failed due to system error (Player is the captain).");
        }
    }

    static void viewAllSessionsAdmin() {

        List<GameSession> sessions = gameSessionService.getAllSessions();

        if (ui.adminEmpty(sessions, "No sessions available.")) return;

        for (GameSession s : sessions) {
            System.out.println("\n  Session ID  : " + s.getSessionID());
            System.out.println("  Team        : " + s.getTeamID());
            System.out.println("  Date        : " + s.getDate());
            System.out.println("  Time        : " + s.getTime());
            System.out.println("  Venue       : " + s.getVenue());
            System.out.println("  Status      : " + ui.formatStatus(s.getStatus()));
        }
    }

    static void manageGameSessionsAdmin() {

        while (true) {

            ui.printHeader("MANAGE GAME SESSIONS");

            System.out.println("  1. View All Sessions");
            System.out.println("  2. Cancel Session");
            System.out.println("  3. Mark Session Completed");
            System.out.println("  0. Back");
            System.out.print("\n  Choice: ");

            switch (ui.readInt()) {
                case 1 -> viewAllSessionsAdmin();
                case 2 -> cancelSession();
                case 3 -> completeSession();
                case 0 -> { return; }
                default -> ui.warn("Invalid option");
            }
        }
    }

    static void cancelSession() {

        System.out.print("  Session ID: ");
        String id = sc.nextLine();

        switch (gameSessionService.cancelSessionAndRelatedBookings(id)) {
            case NOT_FOUND -> ui.warn("Session not found.");
            case ALREADY_CANCELLED -> ui.warn("Session is already cancelled.");
            case SUCCESS -> ui.ok("Session cancelled and related bookings updated.");
        }
    }   

    static void completeSession() {

        System.out.print("  Session ID: ");
        String id = sc.nextLine();

        switch (gameSessionService.completeSessionValidated(id)) {
            case NOT_FOUND -> ui.warn("Session not found.");
            case ALREADY_CANCELLED -> ui.warn("Cannot complete a cancelled session.");
            case ALREADY_COMPLETED -> ui.warn("Session is already completed.");
            case SUCCESS -> ui.ok("Session marked as completed.");
        }
    }

    static String generateSessionToken(String id, String type) {
        return type + "_" + id + "_" + System.currentTimeMillis();
    }
}
