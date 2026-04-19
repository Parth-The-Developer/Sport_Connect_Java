import model.Admin;
import model.Booking;
import model.FriendRequest;
import model.Player;
import model.Team;
import model.GameSession;
import service.AuthService;
import service.ChatService;
import service.FriendRequestService;
import service.GameSessionService;
import service.PlayerService;
import service.SignupPersistenceService;
import service.TeamService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {


static final AuthService   authService   = new AuthService();
static final PlayerService playerService = new PlayerService();
static final FriendRequestService friendRequestService = new FriendRequestService(playerService);
static final ChatService chatService = new ChatService();
static final TeamService teamService = new TeamService(playerService);
static final GameSessionService gameSessionService = new GameSessionService(teamService);
static final SignupPersistenceService signupStore = new SignupPersistenceService();

static Player loggedInPlayer = null;
static Admin  loggedInAdmin  = null;
static String sessionToken   = null;

static final Scanner sc = new Scanner(System.in);

public static void main(String[] args) {
    seedData();
    signupStore.loadInto(authService, playerService);
    mainMenu();
    sc.close();
}

// ─────────────────────────────────────────────

static void mainMenu() {
    while (true) {
        printBanner();
        System.out.println("  1. Sign Up");
        System.out.println("  2. Login");
        System.out.println("  0. Exit");
        System.out.print("\n  Choice: ");

        switch (readInt()) {
            case 1 -> playerSignUp();
            case 2 -> login();
            case 0 -> { System.out.println("\nGoodbye!\n"); return; }
            default -> warn("Invalid option.");
        }
    }
}

// ─────────────────────────────────────────────

static void login() {
    printHeader("Login");

    System.out.print("  Email / Username: ");
    String id = sc.nextLine().trim();

    System.out.print("  Password: ");
    String pass = sc.nextLine().trim();

    // 🔥 TRY ADMIN LOGIN
    try {
        sessionToken  = authService.adminLogin(id, pass);
        loggedInAdmin = authService.getAdminByUsername(id);

        if (loggedInAdmin.getRole().equals("SUPER_ADMIN")) {
            superAdminMenu();
        } else {
            adminMenu();
        }
        return;

    } catch (Exception ignored) {}

    // 🔥 TRY PLAYER LOGIN
    try {
        sessionToken   = authService.playerLogin(id, pass);
        loggedInPlayer = playerService.getPlayerByEmail(id);

        playerMenu();
        return;

    } catch (Exception e) {
        warn("Invalid credentials!");
    }
}

// ─────────────────────────────────────────────

static void playerSignUp() {
    printHeader("Sign Up");

    try {
        System.out.print("  Name     : "); String name  = sc.nextLine();
        System.out.print("  Email    : "); String email = sc.nextLine();
        System.out.print("  Password : "); String pass  = sc.nextLine();
        System.out.print("  Phone    : "); String phone = sc.nextLine();

        System.out.println("  Sports: cricket, football, kabaddi, badminton, tennis");
        System.out.print("  Sport    : ");
        String sport = sc.nextLine().toLowerCase();

        if (!isValidSport(sport)) {
            throw new IllegalArgumentException("Invalid sport!");
        }

        Player p = authService.registerPlayer(email, name, phone, sport);
        p.setPasswordHash(pass);

        System.out.print("  City     : "); p.setCity(sc.nextLine());
        p.setSkill_level(selectSkillLevel());

        playerService.addPlayer(p);

        signupStore.appendSignup(p);

        System.out.println("\n[OK] Account created!\n");

    } catch (Exception e) {
        warn(e.getMessage());
    }
}

// ─────────────────────────────────────────────
// 🔥 PLAYER MENU
// ─────────────────────────────────────────────

static void playerMenu() {
    while (true) {
        printPlayerMainMenu();

        switch (readInt()) {
            case 1 -> updatePlayerProfile();
            case 2 -> searchPlayersMenu();
            case 3 -> manageFriendsMenu();
            case 4 -> openChatModule();
            case 5 -> manageTeamMenu();
            case 6 -> manageSessionMenu();
            case 7 -> workingOnModule("Rate a Player");
            case 8 -> workingOnModule("Make Payment");
            case 9 -> { logout(); return; }
            default -> warn("Invalid option.");
        }
    }
}

// ─────────────────────────────────────────────
// 🔥 TEAM + GAME SESSION MODULES
// ─────────────────────────────────────────────

static void createTeamModule() {
    printHeader("Create Team");

    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    System.out.print("  Team name: ");
    String name = sc.nextLine().trim();

    System.out.print("  Sport (must match your sport: " + loggedInPlayer.getSport() + "): ");
    String sport = sc.nextLine().trim();

    String captainId = String.valueOf(loggedInPlayer.getPlayerId());
    Team team = teamService.createTeamForPlayerSport(loggedInPlayer.getSport(), name, sport, captainId);

    if (team == null) {
        warn("You can only create a team for your own sport.");
        pause();
        return;
    }

    System.out.println("\n[OK] Team created. Team ID: " + team.getTeamID() + "\n");
    pause();
}

static void joinTeamModule() {
    printHeader("Join Team");

    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    List<Team> teams = teamService.getAllTeams();
    if (teams.isEmpty()) {
        System.out.println("No teams available yet.");
        pause();
        return;
    }

    System.out.println("Available teams:");
    for (Team t : teams) {
        System.out.printf("  %s | %s | sport=%s | members=%d/%d%n",
            t.getTeamID(), t.getTeamName(), t.getSport(), t.getMemberIDs().size(), Team.MAX_MEMBERS);
    }

    System.out.print("\nEnter Team ID to join (0 to cancel): ");
    String teamId = sc.nextLine().trim();
    if ("0".equals(teamId)) return;

    String playerId = String.valueOf(loggedInPlayer.getPlayerId());
    String result = teamService.joinTeam(playerId, teamId);

    switch (result) {
        case "SUCCESS" -> System.out.println("\n[OK] Joined team " + teamId + ".\n");
        case "TEAM_NOT_FOUND" -> warn("Team not found.");
        case "PLAYER_NOT_FOUND" -> warn("Player not found.");
        case "ALREADY_MEMBER" -> warn("You are already in this team.");
        case "TEAM_FULL" -> warn("Team is full.");
        case "SPORT_MISMATCH" -> warn("Sport mismatch: you can only join teams for your sport.");
        default -> warn("Unknown error.");
    }
    pause();
}

static void scheduleGameModule() {
    printHeader("Schedule Game");

    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    String playerId = String.valueOf(loggedInPlayer.getPlayerId());
    List<Team> myTeams = teamService.getAllTeams().stream()
        .filter(t -> t.getMemberIDs().contains(playerId))
        .collect(Collectors.toList());

    if (myTeams.isEmpty()) {
        warn("You are not in any team yet. Join/create a team first.");
        pause();
        return;
    }

    System.out.println("Your teams:");
    for (Team t : myTeams) {
        System.out.printf("  %s | %s | sport=%s%n", t.getTeamID(), t.getTeamName(), t.getSport());
    }

    System.out.print("\nEnter Team ID to schedule for: ");
    String teamId = sc.nextLine().trim();
    Team team = teamService.getTeamByID(teamId);
    if (team == null) {
        warn("Team not found.");
        pause();
        return;
    }
    if (!team.getMemberIDs().contains(playerId)) {
        warn("You must be a team member to schedule a session for this team.");
        pause();
        return;
    }

    System.out.print("  Date (YYYY-MM-DD): ");
    String dateStr = sc.nextLine().trim();
    System.out.print("  Time: ");
    String time = sc.nextLine().trim();
    System.out.print("  Venue: ");
    String venue = sc.nextLine().trim();

    try {
        LocalDate date = LocalDate.parse(dateStr);
        GameSession session = gameSessionService.createSession(teamId, team.getSport(), date, time, venue);
        if (session == null) {
            warn("Could not create session (past date or conflict).");
        } else {
            System.out.println("\n[OK] Session created. Session ID: " + session.getSessionID() + "\n");
        }
    } catch (Exception e) {
        warn("Invalid date format. Use YYYY-MM-DD.");
    }

    pause();
}

static void markSessionCompletedModule() {
    printHeader("Mark Session Completed");

    List<GameSession> sessions = gameSessionService.getAllSessions();
    if (sessions.isEmpty()) {
        warn("No sessions available.");
        pause();
        return;
    }

    System.out.println("Sessions:");
    for (GameSession s : sessions) {
        System.out.printf("  %s | team=%s | %s %s | %s | status=%s%n",
            s.getSessionID(), s.getTeamID(), s.getDate(), s.getTime(), s.getVenue(), s.getStatusDisplay());
    }

    System.out.print("\nEnter Session ID to mark completed (0 to cancel): ");
    String sessionId = sc.nextLine().trim();
    if ("0".equals(sessionId)) return;

    switch (gameSessionService.completeSessionValidated(sessionId)) {
        case GameSessionService.R_COMPLETE_NOT_FOUND -> warn("Session not found.");
        case GameSessionService.R_COMPLETE_ALREADY_CANCELLED -> warn("Cannot complete a cancelled session.");
        case GameSessionService.R_COMPLETE_ALREADY_COMPLETED -> warn("Session already completed.");
        case GameSessionService.R_COMPLETE_SUCCESS -> System.out.println("\n[OK] Session marked completed.\n");
        default -> warn("Unknown error.");
    }

    pause();
}

static void manageFriendsMenu() {
    while (true) {
        printHeader("Manage Friends");
        System.out.println("1. Send Friend Request");
        System.out.println("2. View Incoming Requests");
        System.out.println("3. Accept Request");
        System.out.println("4. Reject Request");
        System.out.println("0. Back");
        System.out.print("Choice: ");

        switch (readInt()) {
            case 1 -> sendFriendRequestModule();
            case 2 -> viewIncomingRequestsListModule();
            case 3 -> acceptFriendRequestModule();
            case 4 -> rejectFriendRequestModule();
            case 0 -> { return; }
            default -> warn("Invalid option.");
        }
    }
}

static void manageTeamMenu() {
    while (true) {
        printHeader("Manage Team");
        System.out.println("1. Create Team");
        System.out.println("2. Join Team");
        System.out.println("3. View My Team");
        System.out.println("4. Add Player to Team");
        System.out.println("5. Remove Player from Team");
        System.out.println("6. Leave Team");
        System.out.println("7. Delete Team");
        System.out.println("0. Back");
        System.out.print("Choice: ");

        switch (readInt()) {
            case 1 -> createTeamModule();
            case 2 -> joinTeamModule();
            case 3 -> viewMyTeamsModule();
            case 4 -> addPlayerToTeamModule();
            case 5 -> removePlayerFromTeamModule();
            case 6 -> leaveTeamModule();
            case 7 -> deleteTeamModule();
            case 0 -> { return; }
            default -> warn("Invalid option.");
        }
    }
}

static void manageSessionMenu() {
    while (true) {
        printHeader("Manage Sessions");
        System.out.println("1. Schedule Game");
        System.out.println("2. View Sessions");
        System.out.println("3. Book Session");
        System.out.println("4. Mark Session Completed");
        System.out.println("5. Cancel Session");
        System.out.println("0. Back");
        System.out.print("Choice: ");

        switch (readInt()) {
            case 1 -> scheduleGameModule();
            case 2 -> viewSessionsModule();
            case 3 -> bookSessionModule();
            case 4 -> markSessionCompletedModule();
            case 5 -> cancelSessionModule();
            case 0 -> { return; }
            default -> warn("Invalid option.");
        }
    }
}

static void searchPlayersMenu() {
    while (true) {
        printHeader("Search Players");
        System.out.println("1. Search by Name");
        System.out.println("2. Search by Skill");
        System.out.println("3. Search by Sport");
        System.out.println("0. Back");
        System.out.print("Choice: ");

        switch (readInt()) {
            case 1 -> searchByNameModule();
            case 2 -> searchBySkillModule();
            case 3 -> searchBySportModule();
            case 0 -> { return; }
            default -> warn("Invalid option.");
        }
    }
}

static void viewMyTeamsModule() {
    printHeader("View My Team");

    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    String playerId = String.valueOf(loggedInPlayer.getPlayerId());
    List<Team> myTeams = teamService.getTeamsForPlayer(playerId);
    if (myTeams.isEmpty()) {
        System.out.println("You are not part of any team yet.");
        pause();
        return;
    }

    for (Team t : myTeams) {
        System.out.printf("  %s | %s | sport=%s | captain=%s | members=%d/%d%n",
                t.getTeamID(), t.getTeamName(), t.getSport(), t.getCaptainID(), t.getMemberIDs().size(), Team.MAX_MEMBERS);
    }
    pause();
}

static void addPlayerToTeamModule() {
    printHeader("Add Player to Team");
    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    System.out.print("  Team ID: ");
    String teamId = sc.nextLine().trim();
    System.out.print("  Player ID to add: ");
    String targetPlayerId = sc.nextLine().trim();

    String actorId = String.valueOf(loggedInPlayer.getPlayerId());
    String result = teamService.addPlayerToTeam(teamId, actorId, targetPlayerId);
    switch (result) {
        case TeamService.R_ADD_TEAM_NOT_FOUND -> warn("Team not found.");
        case TeamService.R_ADD_NOT_CAPTAIN -> warn("Only captain can add players.");
        case TeamService.R_ADD_PLAYER_NOT_FOUND -> warn("Player not found.");
        case TeamService.R_ADD_ALREADY_MEMBER -> warn("Player is already in this team.");
        case TeamService.R_ADD_TEAM_FULL -> warn("Team is full.");
        case TeamService.R_ADD_SPORT_MISMATCH -> warn("Player sport does not match team sport.");
        case TeamService.R_ADD_SUCCESS -> System.out.println("\n[OK] Player added to team.\n");
        default -> warn("Unknown error.");
    }
    pause();
}

static void removePlayerFromTeamModule() {
    printHeader("Remove Player from Team");
    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    System.out.print("  Team ID: ");
    String teamId = sc.nextLine().trim();
    System.out.print("  Player ID to remove: ");
    String targetPlayerId = sc.nextLine().trim();

    String actorId = String.valueOf(loggedInPlayer.getPlayerId());
    String result = teamService.removeMemberAsCaptain(teamId, actorId, targetPlayerId);
    switch (result) {
        case TeamService.R_REMOVE_TEAM_NOT_FOUND -> warn("Team not found.");
        case TeamService.R_REMOVE_NOT_CAPTAIN -> warn("Only captain can remove players.");
        case TeamService.R_REMOVE_CAPTAIN_SELF -> warn("Captain cannot remove self. Use Leave Team.");
        case TeamService.R_REMOVE_NOT_IN_TEAM -> warn("Player is not in this team.");
        case TeamService.R_REMOVE_SUCCESS -> System.out.println("\n[OK] Player removed from team.\n");
        default -> warn("Unknown error.");
    }
    pause();
}

static void leaveTeamModule() {
    printHeader("Leave Team");
    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    System.out.print("  Team ID: ");
    String teamId = sc.nextLine().trim();
    String actorId = String.valueOf(loggedInPlayer.getPlayerId());
    String result = teamService.leaveTeamSafe(actorId, teamId);
    switch (result) {
        case TeamService.R_LEAVE_TEAM_NOT_FOUND -> warn("Team not found.");
        case TeamService.R_LEAVE_NOT_IN_TEAM -> warn("You are not in this team.");
        case TeamService.R_LEAVE_SUCCESS -> System.out.println("\n[OK] Left team successfully.\n");
        default -> warn("Unknown error.");
    }
    pause();
}

static void deleteTeamModule() {
    printHeader("Delete Team");
    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    System.out.print("  Team ID: ");
    String teamId = sc.nextLine().trim();
    String actorId = String.valueOf(loggedInPlayer.getPlayerId());
    String result = teamService.deleteTeamAsCaptain(teamId, actorId);
    switch (result) {
        case TeamService.R_DELETE_TEAM_NOT_FOUND -> warn("Team not found.");
        case TeamService.R_DELETE_NOT_CAPTAIN -> warn("Only captain can delete the team.");
        case TeamService.R_DELETE_SUCCESS -> System.out.println("\n[OK] Team deleted successfully.\n");
        default -> warn("Unknown error.");
    }
    pause();
}

static void viewSessionsModule() {
    printHeader("View Sessions");
    List<GameSession> sessions = gameSessionService.getAllSessions();
    if (sessions.isEmpty()) {
        System.out.println("No sessions available.");
        pause();
        return;
    }

    for (GameSession s : sessions) {
        System.out.printf("  %s | team=%s | sport=%s | %s %s | %s | status=%s%n",
                s.getSessionID(), s.getTeamID(), s.getSport(), s.getDate(), s.getTime(), s.getVenue(), s.getStatusDisplay());
    }
    pause();
}

static void bookSessionModule() {
    printHeader("Book Session");
    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    System.out.print("  Session ID: ");
    String sessionId = sc.nextLine().trim();
    System.out.print("  Team ID: ");
    String teamId = sc.nextLine().trim();

    String actorId = String.valueOf(loggedInPlayer.getPlayerId());
    String result = gameSessionService.bookSession(actorId, sessionId, teamId);
    switch (result) {
        case GameSessionService.R_BOOK_SESSION_NOT_FOUND -> warn("Session not found.");
        case GameSessionService.R_BOOK_TEAM_NOT_FOUND -> warn("Team not found.");
        case GameSessionService.R_BOOK_NOT_TEAM_MEMBER -> warn("You must be a team member to book this session.");
        case GameSessionService.R_BOOK_FAILED -> warn("Booking failed (duplicate, mismatch, or unavailable session).");
        case GameSessionService.R_BOOK_SUCCESS -> {
            Booking latest = gameSessionService.getBookingsByTeam(teamId).stream()
                    .filter(b -> b.getSessionID().equalsIgnoreCase(sessionId))
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (latest != null) {
                System.out.println("\n[OK] Session booked. Booking ID: " + latest.getBookingID() + "\n");
            } else {
                System.out.println("\n[OK] Session booked.\n");
            }
        }
        default -> warn("Unknown error.");
    }
    pause();
}

static void cancelSessionModule() {
    printHeader("Cancel Session");
    if (loggedInPlayer == null) {
        warn("Please login first.");
        return;
    }

    System.out.print("  Session ID: ");
    String sessionId = sc.nextLine().trim();
    String actorId = String.valueOf(loggedInPlayer.getPlayerId());
    String result = gameSessionService.cancelSession(sessionId, actorId, false);
    switch (result) {
        case GameSessionService.R_CANCEL_SESSION_NOT_FOUND -> warn("Session not found.");
        case GameSessionService.R_CANCEL_SESSION_TEAM_NOT_FOUND -> warn("Team not found for this session.");
        case GameSessionService.R_CANCEL_SESSION_NOT_CAPTAIN -> warn("Only team captain can cancel this session.");
        case GameSessionService.R_CANCEL_SESSION_ALREADY_CANCELLED -> warn("Session is already cancelled.");
        case GameSessionService.R_CANCEL_SESSION_SUCCESS -> System.out.println("\n[OK] Session cancelled successfully.\n");
        default -> warn("Unknown error.");
    }
    pause();
}

static void printPlayerMainMenu() {
    String name = loggedInPlayer != null ? loggedInPlayer.getName() : "Player";
    System.out.println("\n======================================");
    System.out.println("   Main Menu - " + name);
    System.out.println("======================================\n");
    System.out.println("1.  Update Profile");
    System.out.println("2.  Search Players");
    System.out.println("3.  Manage Friends");
    System.out.println("4.  Open Chat");
    System.out.println("5.  Manage Team");
    System.out.println("6.  Manage Sessions");
    System.out.println("7.  Rate a Player");
    System.out.println("8.  Make Payment");
    System.out.println("9.  Logout");
    System.out.print("\nEnter your choice: ");
}

static void viewPlayerProfile() {
    Player p = loggedInPlayer;

    System.out.println("  Name  : " + p.getName());
    System.out.println("  Email : " + p.getEmail());
    System.out.println("  Sport : " + p.getSport());
    System.out.println("  City  : " + p.getCity());
    System.out.println("  Skill : " + p.getSkill_level());

    pause();
}

static void updatePlayerProfile() {
    printHeader("Update Profile");

    Player p = loggedInPlayer;
    System.out.println("Leave field blank to keep current value.");

    System.out.print("  City (" + safeValue(p.getCity()) + "): ");
    String city = sc.nextLine().trim();
    if (!city.isEmpty()) p.setCity(city);

    System.out.print("  Update skill level? (y/n): ");
    String updateSkill = sc.nextLine().trim().toLowerCase();
    if (updateSkill.equals("y") || updateSkill.equals("yes")) {
        p.setSkill_level(selectSkillLevel());
    }

    System.out.println("\n[OK] Profile updated.\n");
    pause();
}

static void workingOnModule(String moduleName) {
    printHeader(moduleName);
    System.out.println("Working on this module...");
    pause();
}

static void sendFriendRequestModule() {
    printHeader("Send Friend Request");
    List<Player> players = playerService.getAllActivePlayers();

    List<Player> available = players.stream()
        .filter(p -> loggedInPlayer.getPlayerId() != null && !loggedInPlayer.getPlayerId().equals(p.getPlayerId()))
        .collect(Collectors.toList());

    if (available.isEmpty()) {
        System.out.println("No other players available right now.");
        pause();
        return;
    }

    System.out.println("Available players:");
    for (Player p : available) {
        System.out.printf("  ID=%d | %s | sport=%s | city=%s%n",
            p.getPlayerId(),
            p.getName(),
            safeValue(p.getSport()),
            safeValue(p.getCity())
        );
    }

    System.out.print("\nEnter player ID to send request (0 to cancel): ");
    int toId = readIntInline();
    if (toId == 0) {
        System.out.println("Cancelled.");
        pause();
        return;
    }

    try {
        FriendRequest request = friendRequestService.sendFriendRequest(loggedInPlayer.getPlayerId(), (long) toId);
        System.out.println("[OK] Friend request sent. Request ID: " + request.getRequestID());
    } catch (Exception e) {
        warn(e.getMessage());
    }
    pause();
}

static void viewIncomingRequestsListModule() {
    printHeader("View Incoming Requests");
    List<FriendRequest> incoming = friendRequestService.viewIncomingRequests(loggedInPlayer.getPlayerId());
    if (incoming.isEmpty()) {
        System.out.println("No incoming friend requests.");
        pause();
        return;
    }

    System.out.println("Pending requests:");
    for (FriendRequest r : incoming) {
        String fromName = resolvePlayerName(r.getFromPlayerID());
        System.out.printf("  %s | From: %s | Sent: %s%n",
            r.getRequestID(), fromName, r.getDateSent());
    }
    pause();
}

static void acceptFriendRequestModule() {
    printHeader("Accept Request");
    List<FriendRequest> incoming = friendRequestService.viewIncomingRequests(loggedInPlayer.getPlayerId());
    if (incoming.isEmpty()) {
        System.out.println("No incoming friend requests.");
        pause();
        return;
    }

    System.out.println("Pending requests:");
    for (FriendRequest r : incoming) {
        System.out.printf("  %s | From: %s | Sent: %s%n", r.getRequestID(), resolvePlayerName(r.getFromPlayerID()), r.getDateSent());
    }

    System.out.print("\nEnter Request ID to accept (0 to back): ");
    String requestId = sc.nextLine().trim();
    if ("0".equals(requestId)) return;

    String result = friendRequestService.acceptFriendRequest(requestId, loggedInPlayer.getPlayerId());
    switch (result) {
        case FriendRequestService.RESPOND_SUCCESS -> System.out.println("[OK] Request accepted.");
        case FriendRequestService.RESPOND_REQUEST_NOT_FOUND -> warn("Request not found.");
        case FriendRequestService.RESPOND_INVALID_REQUEST -> warn("Request cannot be accepted.");
        case FriendRequestService.RESPOND_FAILED -> warn("Failed to accept request.");
        default -> warn("Unknown error.");
    }
    pause();
}

static void rejectFriendRequestModule() {
    printHeader("Reject Request");
    List<FriendRequest> incoming = friendRequestService.viewIncomingRequests(loggedInPlayer.getPlayerId());
    if (incoming.isEmpty()) {
        System.out.println("No incoming friend requests.");
        pause();
        return;
    }

    System.out.println("Pending requests:");
    for (FriendRequest r : incoming) {
        System.out.printf("  %s | From: %s | Sent: %s%n", r.getRequestID(), resolvePlayerName(r.getFromPlayerID()), r.getDateSent());
    }

    System.out.print("\nEnter Request ID to reject (0 to back): ");
    String requestId = sc.nextLine().trim();
    if ("0".equals(requestId)) return;

    String result = friendRequestService.rejectFriendRequest(requestId, loggedInPlayer.getPlayerId());
    switch (result) {
        case FriendRequestService.RESPOND_SUCCESS -> System.out.println("[OK] Request rejected.");
        case FriendRequestService.RESPOND_REQUEST_NOT_FOUND -> warn("Request not found.");
        case FriendRequestService.RESPOND_INVALID_REQUEST -> warn("Request cannot be rejected.");
        case FriendRequestService.RESPOND_FAILED -> warn("Failed to reject request.");
        default -> warn("Unknown error.");
    }
    pause();
}

static String resolvePlayerName(String playerIdText) {
    try {
        Long playerId = Long.parseLong(playerIdText);
        return playerService.getPlayerById(playerId).getName();
    } catch (Exception e) {
        return "Player#" + playerIdText;
    }
}

static void openChatModule() {
    printHeader("Open Chat");
    System.out.println("1. Send Message");
    System.out.println("2. View Conversation");
    System.out.println("0. Back");
    System.out.print("Choice: ");

    switch (readInt()) {
        case 1 -> sendChatMessage();
        case 2 -> viewConversation();
        case 0 -> { return; }
        default -> {
            warn("Invalid option.");
            pause();
        }
    }
}

static void sendChatMessage() {
    List<Player> players = playerService.getAllActivePlayers().stream()
        .filter(p -> !p.getPlayerId().equals(loggedInPlayer.getPlayerId()))
        .filter(p -> friendRequestService.isFriendshipAccepted(loggedInPlayer.getPlayerId(), p.getPlayerId()))
        .collect(Collectors.toList());

    if (players.isEmpty()) {
        System.out.println("No friends available to chat.");
        System.out.println("You can send messages only after friend request is accepted.");
        pause();
        return;
    }

    System.out.println("Choose receiver:");
    for (Player p : players) {
        System.out.printf("  ID=%d | %s%n", p.getPlayerId(), p.getName());
    }

    System.out.print("Enter receiver ID (0 to cancel): ");
    int receiverId = readIntInline();
    if (receiverId == 0) {
        return;
    }

    try {
        Player toPlayer = playerService.getPlayerById((long) receiverId);
        System.out.print("Type message: ");
        String message = sc.nextLine();
        chatService.sendMessage(loggedInPlayer, toPlayer, message);
        System.out.println("[OK] Message sent.");
    } catch (Exception e) {
        warn(e.getMessage());
    }
    pause();
}

static void viewConversation() {
    List<Player> players = playerService.getAllActivePlayers().stream()
        .filter(p -> !p.getPlayerId().equals(loggedInPlayer.getPlayerId()))
        .collect(Collectors.toList());
    Map<Long, Integer> incomingCounts = chatService.countIncomingMessagesBySender(loggedInPlayer.getPlayerId());

    if (players.isEmpty()) {
        System.out.println("No players available.");
        pause();
        return;
    }

    System.out.println("View chat with:");
    for (Player p : players) {
        int count = incomingCounts.getOrDefault(p.getPlayerId(), 0);
        if (count > 0) {
            System.out.printf("  ID=%d | %s | %d message(s) from this person%n",
                p.getPlayerId(), p.getName(), count);
        } else {
            System.out.printf("  ID=%d | %s%n", p.getPlayerId(), p.getName());
        }
    }

    System.out.print("Enter player ID (0 to cancel): ");
    int otherId = readIntInline();
    if (otherId == 0) {
        return;
    }

    try {
        Player other = playerService.getPlayerById((long) otherId);
        List<String> messages = chatService.getConversation(loggedInPlayer.getPlayerId(), other.getPlayerId());
        printHeader("Conversation with " + other.getName());
        if (messages.isEmpty()) {
            System.out.println("No messages yet.");
        } else {
            messages.forEach(m -> {
                String[] parts = m.split(": ", 2);
                String senderLabel = parts.length == 2 ? resolvePlayerName(parts[0]) : "Unknown";
                String text = parts.length == 2 ? parts[1] : m;
                System.out.println(senderLabel + ": " + text);
            });
        }
    } catch (Exception e) {
        warn(e.getMessage());
    }
    pause();
}

// ─────────────────────────────────────────────
// 🔥 ADMIN MENU
// ─────────────────────────────────────────────

static void adminMenu() {
    while (true) {
        printHeader("Admin Menu - " + loggedInAdmin.getUsername());

        System.out.println("  1. List Players");
        System.out.println("  2. Deactivate Player");
        System.out.println("  3. View persisted signups (decrypted file)");
        System.out.println("  0. Logout");

        switch (readInt()) {
            case 1 -> listPlayers();
            case 2 -> deactivatePlayer();
            case 3 -> viewPersistedSignupsAdmin();
            case 0 -> { logout(); return; }
        }
    }
}

// ─────────────────────────────────────────────
// 🔥 SUPER ADMIN MENU
// ─────────────────────────────────────────────

static void superAdminMenu() {
    while (true) {
        printHeader("SUPER ADMIN - " + loggedInAdmin.getUsername());

        System.out.println("  1. List Players");
        System.out.println("  2. Delete Player");
        System.out.println("  3. View persisted signups (decrypted file)");
        System.out.println("  0. Logout");

        switch (readInt()) {
            case 1 -> listPlayers();
            case 2 -> deletePlayer();
            case 3 -> viewPersistedSignupsAdmin();
            case 0 -> { logout(); return; }
        }
    }
}

static void viewPersistedSignupsAdmin() {
    printHeader("Persisted signups (AES-GCM decrypted)");
    List<String> lines = signupStore.listDecryptedRecordsForAdmin();
    if (lines.isEmpty()) {
        System.out.println("No rows in data/player_signups.txt (or file empty).");
    } else {
        for (String line : lines) {
            System.out.println(line);
        }
    }
    pause();
}

// ─────────────────────────────────────────────

static void listPlayers() {
    List<Player> list = playerService.getAllPlayers();
    for (Player p : list) System.out.println(p);
    pause();
}

static void deactivatePlayer() {
    System.out.print("Player ID: ");
    playerService.deactivatePlayer((long) readIntInline());
    System.out.println("Deactivated!");
}

static void deletePlayer() {
    System.out.print("Player ID: ");
    playerService.deletePlayer((long) readIntInline());
    System.out.println("Deleted!");
}

static void searchBySportModule() {
    System.out.print("Enter sport: ");
    printPlayers(playerService.searchBySport(sc.nextLine()));
}

static void searchBySkillModule() {
    System.out.print("Enter skill (BEGINNER / INTERMEDIATE / ADVANCED): ");
    printPlayers(playerService.searchBySkill(sc.nextLine()));
}

static void searchByNameModule() {
    System.out.print("Enter name keyword: ");
    printPlayers(playerService.searchByName(sc.nextLine()));
}

static String selectSkillLevel() {
    while (true) {
        System.out.println("  Skill Level:");
        System.out.println("    1. Beginner");
        System.out.println("    2. Intermediate");
        System.out.println("    3. Advanced");
        System.out.print("  Choose skill level: ");

        switch (readInt()) {
            case 1 -> { return "BEGINNER"; }
            case 2 -> { return "INTERMEDIATE"; }
            case 3 -> { return "ADVANCED"; }
            default -> warn("Please choose 1, 2 or 3.");
        }
    }
}

static void printPlayers(List<Player> list) {
    if (list.isEmpty()) System.out.println("No players found.");
    else list.forEach(System.out::println);
    pause();
}

// ─────────────────────────────────────────────

static void logout() {
    authService.logout(sessionToken);
    loggedInPlayer = null;
    loggedInAdmin  = null;
    sessionToken   = null;
    System.out.println("\nLogged out.\n");
}

// ─────────────────────────────────────────────

static boolean isValidSport(String sport) {
    return switch (sport) {
        case "cricket", "football", "kabaddi", "badminton", "tennis" -> true;
        default -> false;
    };
}

static void seedData() {
    authService.registerAdmin("admin", "admin123", "admin@mail.com", "Admin");
    authService.registerSuperAdmin("superadmin", "super123", "super@mail.com", "Super");

    addDemo("Parth",          "user@mail.com",      "999",          "cricket",    "INTERMEDIATE", "Toronto",     22, 3, "123");
    addDemo("Lien Tran",      "lien@demo.com",      "647-111-2222", "cricket",    "BEGINNER",     "Toronto",     21, 2, "demo123");
    addDemo("Brian Carter",   "brian@demo.com",     "905-222-3333", "cricket",    "ADVANCED",     "Brampton",    24, 5, "demo123");
    addDemo("Shahshree Das",  "shahshree@demo.com", "416-333-4444", "cricket",    "INTERMEDIATE", "Mississauga", 22, 3, "demo123");
    addDemo("Hassana Diallo", "hassana@demo.com",   "647-444-5555", "volleyball", "BEGINNER",     "Toronto",     20, 1, "demo123");
    addDemo("Pooja Mehta",    "pooja@demo.com",     "905-555-6666", "cricket",    "ADVANCED",     "Brampton",    26, 6, "demo123");
    addDemo("Riddhi Shah",    "riddhi@demo.com",    "416-666-7777", "soccer",     "INTERMEDIATE", "Mississauga", 23, 4, "demo123");
}

static void addDemo(String name, String email, String phone, String sport,
                    String skill, String city, int age, int experience, String password) {
    Player p = authService.registerPlayer(email, name, phone, sport);
    p.setPasswordHash(password);
    p.setSkill_level(skill);
    p.setCity(city);
    p.setAge(age);
    p.setExperience(experience);
    playerService.addPlayer(p);
}

// ─────────────────────────────────────────────

static void printBanner() {
    System.out.println("\n=== SportConnect ===\n");
}

static void printHeader(String t) {
    System.out.println("\n--- " + t + " ---\n");
}

static void pause() {
    System.out.print("\nPress Enter...");
    sc.nextLine();
}

static int readInt() {
    try { return Integer.parseInt(sc.nextLine()); }
    catch (Exception e) { return -1; }
}

static int readIntInline() {
    try { return Integer.parseInt(sc.nextLine()); }
    catch (Exception e) { return 0; }
}

static void warn(String msg) {
    System.out.println("\n[!] " + msg + "\n");
}

static String safeValue(String value) {
    return value == null || value.isBlank() ? "Not Set" : value;
}


}
