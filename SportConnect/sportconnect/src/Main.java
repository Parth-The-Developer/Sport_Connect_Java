import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import model.Admin;
import model.Booking;
import model.FriendRequest;
import model.GameSession;
import model.Player;
import model.Team;
import service.AuthService;
import service.ChatService;
import service.FriendRequestService;
import service.GameSessionService;
import service.PaymentService;
import service.PlayerService;
import service.RatingService;
import service.SignupPersistenceService;
import service.TeamService;

public class Main {

// ── Services ──────────────────────────────────────────────────────────────────
static final AuthService              authService          = new AuthService();
static final PlayerService            playerService        = new PlayerService();
static final FriendRequestService     friendRequestService = new FriendRequestService(playerService);
static final ChatService              chatService          = new ChatService();
static final TeamService              teamService          = new TeamService(playerService);
static final GameSessionService       gameSessionService   = new GameSessionService(teamService);
static final RatingService            ratingService        = new RatingService();
static final PaymentService           paymentService       = new PaymentService();
static final SignupPersistenceService signupStore          = new SignupPersistenceService();

static Player loggedInPlayer = null;
static Admin  loggedInAdmin  = null;
static String sessionToken   = null;

static final Scanner sc = new Scanner(System.in);

// ── Valid sports ──────────────────────────────────────────────────────────────
static final String[] VALID_SPORTS = {"cricket","football","kabaddi","badminton","tennis","soccer","volleyball"};

// ═════════════════════════════════════════════════════════════════════════════
//  ENTRY POINT
// ═════════════════════════════════════════════════════════════════════════════
public static void main(String[] args) {
    seedData();
    signupStore.loadInto(authService, playerService);
    mainMenu();
    sc.close();
}

// ═════════════════════════════════════════════════════════════════════════════
//  MAIN / WELCOME MENU
// ═════════════════════════════════════════════════════════════════════════════
static void mainMenu() {
    String[] valid = {"1","2","0"};
    while (true) {
        System.out.println();
        System.out.println("  ==================================================");
        System.out.println("         S P O R T C O N N E C T                   ");
        System.out.println("       Connect | Play | Compete | Achieve           ");
        System.out.println("  ==================================================");
        System.out.println("    1.  Sign Up");
        System.out.println("    2.  Login");
        System.out.println("    0.  Exit");
        System.out.println("  ==================================================");
        switch (readMenuChoice(valid)) {
            case "1" -> playerSignUp();
            case "2" -> login();
            case "0" -> { printBox("Goodbye! See you on the court."); return; }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  LOGIN
// ═════════════════════════════════════════════════════════════════════════════
static void login() {
    printHeader("LOGIN");
    int attempts = 0;
    while (attempts < 3) {
        String id   = readRequired("Email / Username");
        String pass = readRequired("Password");
        // try admin
        try {
            sessionToken  = authService.adminLogin(id, pass);
            loggedInAdmin = authService.getAdminByUsername(id);
            printSuccess("Admin access granted. Welcome, " + loggedInAdmin.getUsername() + "!");
            if (loggedInAdmin.getRole().equals("SUPER_ADMIN")) superAdminMenu();
            else adminMenu();
            return;
        } catch (Exception ignored) {}
        // try player
        try {
            sessionToken   = authService.playerLogin(id, pass);
            loggedInPlayer = playerService.getPlayerByEmail(id);
            printSuccess("Welcome back, " + loggedInPlayer.getName() + "!");
            playerMenu();
            return;
        } catch (Exception e) {
            attempts++;
            if (attempts < 3)
                printError("Invalid credentials. " + (3-attempts) + " attempt(s) remaining.");
            else
                printError("Too many failed attempts. Returning to main menu.");
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  SIGN UP
// ═════════════════════════════════════════════════════════════════════════════
static void playerSignUp() {
    printHeader("SIGN UP");
    try {
        String name  = readRequired("Full Name");

        // email validated
        String email;
        while (true) {
            email = readRequired("Email");
            if (email.contains("@") && email.contains(".")) break;
            printError("Invalid email address. Must contain @ and a domain.");
        }

        // password min 4 chars
        String pass;
        while (true) {
            pass = readRequired("Password");
            if (pass.length() >= 4) break;
            printError("Password must be at least 4 characters.");
        }

        // phone non-empty
        String phone = readRequired("Phone Number");

        // sport validated
        String sport = readSport();

        Player p = authService.registerPlayer(email, name, phone, sport);
        p.setPasswordHash(pass);

        String city = readRequired("City");
        p.setCity(city);
        p.setSkill_level(selectSkillLevel());

        playerService.addPlayer(p);
        signupStore.appendSignup(p);
        printSuccess("Account created! You can now login with " + email);

    } catch (Exception e) {
        printError(e.getMessage());
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  PLAYER MENU
// ═════════════════════════════════════════════════════════════════════════════
static void playerMenu() {
    String[] valid = {"1","2","3","4","5","6","7","8","9"};
    while (true) {
        String name = loggedInPlayer != null ? loggedInPlayer.getName() : "Player";
        System.out.println();
        System.out.println("  ==================================================");
        System.out.printf ("  Welcome, %-40s%n", name + "!");
        if (loggedInPlayer != null) {
            System.out.printf("  Sport: %-12s | Skill: %-14s | City: %s%n",
                safeValue(loggedInPlayer.getSport()),
                safeValue(loggedInPlayer.getSkill_level()),
                safeValue(loggedInPlayer.getCity()));
        }
        System.out.println("  ==================================================");
        System.out.println("  -- PROFILE ----------------------------------------");
        System.out.println("    1.  Update Profile");
        System.out.println("  -- DISCOVER ----------------------------------------");
        System.out.println("    2.  Search Players");
        System.out.println("  -- SOCIAL ------------------------------------------");
        System.out.println("    3.  Manage Friends");
        System.out.println("    4.  Open Chat");
        System.out.println("  -- TEAMS & GAMES -----------------------------------");
        System.out.println("    5.  Manage Team");
        System.out.println("    6.  Manage Sessions");
        System.out.println("  -- POST-GAME ----------------------------------------");
        System.out.println("    7.  Rate a Player");
        System.out.println("    8.  Make Payment");
        System.out.println("  ==================================================");
        System.out.println("    9.  Logout");
        System.out.println("  ==================================================");
        switch (readMenuChoice(valid)) {
            case "1" -> updatePlayerProfile();
            case "2" -> searchPlayersMenu();
            case "3" -> manageFriendsMenu();
            case "4" -> openChatModule();
            case "5" -> manageTeamMenu();
            case "6" -> manageSessionMenu();
            case "7" -> ratePlayerModule();
            case "8" -> paymentService.makePayment(loggedInPlayer, "MANUAL", sc);
            case "9" -> { logout(); return; }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  UPDATE PROFILE
// ═════════════════════════════════════════════════════════════════════════════
static void updatePlayerProfile() {
    printHeader("UPDATE PROFILE");
    Player p = loggedInPlayer;
    printInfo("Current city: " + safeValue(p.getCity()) + "  |  Skill: " + safeValue(p.getSkill_level()));
    System.out.println();
    prompt("New City (press Enter to keep '" + safeValue(p.getCity()) + "')");
    String city = sc.nextLine().trim();
    if (!city.isEmpty()) p.setCity(city);

    prompt("Update skill level? (y/n)");
    String yn = sc.nextLine().trim().toLowerCase();
    if (yn.equals("y") || yn.equals("yes")) {
        p.setSkill_level(selectSkillLevel());
    }
    printSuccess("Profile updated successfully.");
    pause();
}

// ═════════════════════════════════════════════════════════════════════════════
//  SEARCH PLAYERS
// ═════════════════════════════════════════════════════════════════════════════
static void searchPlayersMenu() {
    String[] valid = {"1","2","3","0"};
    while (true) {
        printHeader("SEARCH PLAYERS");
        System.out.println("    1.  Search by Name");
        System.out.println("    2.  Search by Skill Level");
        System.out.println("    3.  Search by Sport");
        System.out.println("    0.  Back");
        switch (readMenuChoice(valid)) {
            case "1" -> searchByNameModule();
            case "2" -> searchBySkillModule();
            case "3" -> searchBySportModule();
            case "0" -> { return; }
        }
    }
}

static void searchBySportModule() {
    printHeader("SEARCH BY SPORT");
    String sport = readSport();
    List<Player> list = playerService.searchBySport(sport);
    printPlayerTable(list);
    pause();
}

static void searchBySkillModule() {
    printHeader("SEARCH BY SKILL");
    String skill = selectSkillLevel();
    List<Player> list = playerService.searchBySkill(skill);
    printPlayerTable(list);
    pause();
}

static void searchByNameModule() {
    printHeader("SEARCH BY NAME");
    String keyword = readRequired("Name keyword");
    List<Player> list = playerService.searchByName(keyword);
    printPlayerTable(list);
    pause();
}

// ═════════════════════════════════════════════════════════════════════════════
//  FRIENDS
// ═════════════════════════════════════════════════════════════════════════════
static void manageFriendsMenu() {
    String[] valid = {"1","2","3","4","0"};
    while (true) {
        printHeader("MANAGE FRIENDS");
        System.out.println("    1.  Send Friend Request");
        System.out.println("    2.  View Incoming Requests");
        System.out.println("    3.  Accept Request");
        System.out.println("    4.  Reject Request");
        System.out.println("    0.  Back");
        switch (readMenuChoice(valid)) {
            case "1" -> sendFriendRequestModule();
            case "2" -> viewIncomingRequestsListModule();
            case "3" -> acceptFriendRequestModule();
            case "4" -> rejectFriendRequestModule();
            case "0" -> { return; }
        }
    }
}

static void sendFriendRequestModule() {
    printHeader("SEND FRIEND REQUEST");
    List<Player> available = playerService.getAllActivePlayers().stream()
        .filter(p -> loggedInPlayer.getPlayerId() != null && !loggedInPlayer.getPlayerId().equals(p.getPlayerId()))
        .collect(Collectors.toList());
    if (available.isEmpty()) { printInfo("No other players available right now."); pause(); return; }
    printPlayerTable(available);
    System.out.println();
    int toId = readPositiveInt("Enter player ID to send request (0 to cancel)");
    if (toId == 0) return;
    try {
        FriendRequest request = friendRequestService.sendFriendRequest(loggedInPlayer.getPlayerId(), (long) toId);
        printSuccess("Friend request sent. Request ID: " + request.getRequestID());
    } catch (Exception e) { printError(e.getMessage()); }
    pause();
}

static void viewIncomingRequestsListModule() {
    printHeader("INCOMING FRIEND REQUESTS");
    List<FriendRequest> incoming = friendRequestService.viewIncomingRequests(loggedInPlayer.getPlayerId());
    if (incoming.isEmpty()) { printInfo("No pending friend requests."); pause(); return; }
    System.out.println("  +------------+----------------------+--------------------+");
    System.out.println("  | Request ID | From                 | Date Sent          |");
    System.out.println("  +------------+----------------------+--------------------+");
    for (FriendRequest r : incoming) {
        String fromName = resolvePlayerName(r.getFromPlayerID());
        System.out.printf("  | %-10s | %-20s | %-18s |%n",
            r.getRequestID(), fromName, r.getDateSent());
    }
    System.out.println("  +------------+----------------------+--------------------+");
    pause();
}

static void acceptFriendRequestModule() {
    printHeader("ACCEPT FRIEND REQUEST");
    List<FriendRequest> incoming = friendRequestService.viewIncomingRequests(loggedInPlayer.getPlayerId());
    if (incoming.isEmpty()) { printInfo("No pending friend requests."); pause(); return; }
    for (FriendRequest r : incoming)
        System.out.printf("  [%s] From: %-20s | Sent: %s%n",
            r.getRequestID(), resolvePlayerName(r.getFromPlayerID()), r.getDateSent());
    System.out.println();
    // validated request ID
    String requestId;
    while (true) {
        prompt("Enter Request ID to accept (0 to back)");
        requestId = sc.nextLine().trim();
        if ("0".equals(requestId)) return;
        if (!requestId.isEmpty()) break;
        printError("Request ID cannot be empty.");
    }
    String result = friendRequestService.acceptFriendRequest(requestId, loggedInPlayer.getPlayerId());
    switch (result) {
        case FriendRequestService.RESPOND_SUCCESS        -> printSuccess("Friend request accepted!");
        case FriendRequestService.RESPOND_REQUEST_NOT_FOUND -> printError("Request not found. Check the Request ID.");
        case FriendRequestService.RESPOND_INVALID_REQUEST    -> printError("This request cannot be accepted.");
        case FriendRequestService.RESPOND_FAILED             -> printError("Failed to accept request.");
        default -> printError("Unknown error.");
    }
    pause();
}

static void rejectFriendRequestModule() {
    printHeader("REJECT FRIEND REQUEST");
    List<FriendRequest> incoming = friendRequestService.viewIncomingRequests(loggedInPlayer.getPlayerId());
    if (incoming.isEmpty()) { printInfo("No pending friend requests."); pause(); return; }
    for (FriendRequest r : incoming)
        System.out.printf("  [%s] From: %-20s | Sent: %s%n",
            r.getRequestID(), resolvePlayerName(r.getFromPlayerID()), r.getDateSent());
    System.out.println();
    String requestId;
    while (true) {
        prompt("Enter Request ID to reject (0 to back)");
        requestId = sc.nextLine().trim();
        if ("0".equals(requestId)) return;
        if (!requestId.isEmpty()) break;
        printError("Request ID cannot be empty.");
    }
    String result = friendRequestService.rejectFriendRequest(requestId, loggedInPlayer.getPlayerId());
    switch (result) {
        case FriendRequestService.RESPOND_SUCCESS           -> printSuccess("Friend request rejected.");
        case FriendRequestService.RESPOND_REQUEST_NOT_FOUND -> printError("Request not found.");
        case FriendRequestService.RESPOND_INVALID_REQUEST   -> printError("This request cannot be rejected.");
        case FriendRequestService.RESPOND_FAILED            -> printError("Failed to reject request.");
        default -> printError("Unknown error.");
    }
    pause();
}

// ═════════════════════════════════════════════════════════════════════════════
//  CHAT
// ═════════════════════════════════════════════════════════════════════════════
static void openChatModule() {
    String[] valid = {"1","2","0"};
    printHeader("OPEN CHAT");
    System.out.println("    1.  Send Message");
    System.out.println("    2.  View Conversation");
    System.out.println("    0.  Back");
    switch (readMenuChoice(valid)) {
        case "1" -> sendChatMessage();
        case "2" -> viewConversation();
        case "0" -> { }
    }
}

static void sendChatMessage() {
    printHeader("SEND MESSAGE");
    List<Player> friends = playerService.getAllActivePlayers().stream()
        .filter(p -> !p.getPlayerId().equals(loggedInPlayer.getPlayerId()))
        .filter(p -> friendRequestService.isFriendshipAccepted(loggedInPlayer.getPlayerId(), p.getPlayerId()))
        .collect(Collectors.toList());
    if (friends.isEmpty()) {
        printInfo("No friends available to chat.");
        printInfo("You can only message players who have accepted your friend request.");
        pause(); return;
    }
    printPlayerTable(friends);
    System.out.println();
    int receiverId = readPositiveInt("Enter player ID (0 to cancel)");
    if (receiverId == 0) return;
    try {
        Player toPlayer = playerService.getPlayerById((long) receiverId);
        // message must not be empty
        String message;
        while (true) {
            prompt("Type your message (0 to cancel)");
            message = sc.nextLine();
            if ("0".equals(message.trim())) return;
            if (!message.trim().isEmpty()) break;
            printError("Message cannot be empty.");
        }
        chatService.sendMessage(loggedInPlayer, toPlayer, message);
        printSuccess("Message sent to " + toPlayer.getName() + "!");
    } catch (Exception e) { printError(e.getMessage()); }
    pause();
}

static void viewConversation() {
    printHeader("VIEW CONVERSATION");
    List<Player> players = playerService.getAllActivePlayers().stream()
        .filter(p -> !p.getPlayerId().equals(loggedInPlayer.getPlayerId()))
        .collect(Collectors.toList());
    Map<Long, Integer> incomingCounts = chatService.countIncomingMessagesBySender(loggedInPlayer.getPlayerId());
    if (players.isEmpty()) { printInfo("No players available."); pause(); return; }
    System.out.println("  +------+----------------------+---------------+");
    System.out.println("  | ID   | Name                 | New Messages  |");
    System.out.println("  +------+----------------------+---------------+");
    for (Player p : players) {
        int count = incomingCounts.getOrDefault(p.getPlayerId(), 0);
        System.out.printf("  | %-4d | %-20s | %-13s |%n",
            p.getPlayerId(), p.getName(), count > 0 ? count + " new" : "-");
    }
    System.out.println("  +------+----------------------+---------------+");
    System.out.println();
    int otherId = readPositiveInt("Enter player ID (0 to cancel)");
    if (otherId == 0) return;
    try {
        Player other = playerService.getPlayerById((long) otherId);
        printHeader("CHAT WITH " + other.getName().toUpperCase());
        List<String> messages = chatService.getConversation(loggedInPlayer.getPlayerId(), other.getPlayerId());
        if (messages.isEmpty()) {
            printInfo("No messages yet. Start the conversation!");
        } else {
            System.out.println("  " + "-".repeat(50));
            for (String m : messages) {
                String[] parts = m.split(": ", 2);
                String senderLabel = parts.length == 2 ? resolvePlayerName(parts[0]) : "Unknown";
                String text = parts.length == 2 ? parts[1] : m;
                boolean mine = parts.length > 0 && parts[0].equals(String.valueOf(loggedInPlayer.getPlayerId()));
                if (mine) System.out.printf("  >> %-46s%n", senderLabel + ": " + text);
                else      System.out.printf("  %-50s%n",    senderLabel + ": " + text);
            }
            System.out.println("  " + "-".repeat(50));
        }
    } catch (Exception e) { printError(e.getMessage()); }
    pause();
}

// ═════════════════════════════════════════════════════════════════════════════
//  TEAM
// ═════════════════════════════════════════════════════════════════════════════
static void manageTeamMenu() {
    String[] valid = {"1","2","3","4","5","6","7","0"};
    while (true) {
        printHeader("MANAGE TEAM");
        System.out.println("    1.  Create Team");
        System.out.println("    2.  Join Team");
        System.out.println("    3.  View My Teams");
        System.out.println("    4.  Add Player to Team");
        System.out.println("    5.  Remove Player from Team");
        System.out.println("    6.  Leave Team");
        System.out.println("    7.  Delete Team");
        System.out.println("    0.  Back");
        switch (readMenuChoice(valid)) {
            case "1" -> createTeamModule();
            case "2" -> joinTeamModule();
            case "3" -> viewMyTeamsModule();
            case "4" -> addPlayerToTeamModule();
            case "5" -> removePlayerFromTeamModule();
            case "6" -> leaveTeamModule();
            case "7" -> deleteTeamModule();
            case "0" -> { return; }
        }
    }
}

static void createTeamModule() {
    printHeader("CREATE TEAM");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    String name  = readRequired("Team Name");
    printInfo("Your sport: " + loggedInPlayer.getSport());
    prompt("Sport (must match your sport: " + loggedInPlayer.getSport() + ")");
    String sport = sc.nextLine().trim();
    if (sport.isEmpty()) sport = loggedInPlayer.getSport();
    String captainId = String.valueOf(loggedInPlayer.getPlayerId());
    Team team = teamService.createTeamForPlayerSport(loggedInPlayer.getSport(), name, sport, captainId);
    if (team == null) { printError("You can only create a team for your own sport (" + loggedInPlayer.getSport() + ")."); pause(); return; }
    printSuccess("Team created! Team ID: " + team.getTeamID());
    pause();
}

static void joinTeamModule() {
    printHeader("JOIN TEAM");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    List<Team> teams = teamService.getAllTeams();
    if (teams.isEmpty()) { printInfo("No teams available yet."); pause(); return; }
    System.out.println("  +--------+--------------------+----------+----------+");
    System.out.println("  | ID     | Name               | Sport    | Members  |");
    System.out.println("  +--------+--------------------+----------+----------+");
    for (Team t : teams)
        System.out.printf("  | %-6s | %-18s | %-8s | %d/%-6d |%n",
            t.getTeamID(), t.getTeamName(), t.getSport(),
            t.getMemberIDs().size(), Team.MAX_MEMBERS);
    System.out.println("  +--------+--------------------+----------+----------+");
    System.out.println();
    String teamId;
    while (true) {
        prompt("Enter Team ID to join (0 to cancel)");
        teamId = sc.nextLine().trim();
        if ("0".equals(teamId)) return;
        if (!teamId.isEmpty()) break;
        printError("Team ID cannot be empty.");
    }
    String playerId = String.valueOf(loggedInPlayer.getPlayerId());
    String result = teamService.joinTeam(playerId, teamId);
    switch (result) {
        case "SUCCESS"        -> printSuccess("Joined team " + teamId + " successfully!");
        case "TEAM_NOT_FOUND" -> printError("Team not found. Check the Team ID.");
        case "ALREADY_MEMBER" -> printError("You are already a member of this team.");
        case "TEAM_FULL"      -> printError("This team is full.");
        case "SPORT_MISMATCH" -> printError("Sport mismatch: you can only join teams matching your sport (" + loggedInPlayer.getSport() + ").");
        default               -> printError("Could not join team: " + result);
    }
    pause();
}

static void viewMyTeamsModule() {
    printHeader("MY TEAMS");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    List<Team> myTeams = teamService.getTeamsForPlayer(String.valueOf(loggedInPlayer.getPlayerId()));
    if (myTeams.isEmpty()) { printInfo("You are not part of any team yet."); pause(); return; }
    System.out.println("  +--------+--------------------+----------+--------+----------+");
    System.out.println("  | ID     | Name               | Sport    | Role   | Members  |");
    System.out.println("  +--------+--------------------+----------+--------+----------+");
    for (Team t : myTeams) {
        boolean isCaptain = String.valueOf(loggedInPlayer.getPlayerId()).equals(t.getCaptainID());
        System.out.printf("  | %-6s | %-18s | %-8s | %-6s | %d/%-6d |%n",
            t.getTeamID(), t.getTeamName(), t.getSport(),
            isCaptain ? "CAPTAIN" : "MEMBER",
            t.getMemberIDs().size(), Team.MAX_MEMBERS);
    }
    System.out.println("  +--------+--------------------+----------+--------+----------+");
    pause();
}

static void addPlayerToTeamModule() {
    printHeader("ADD PLAYER TO TEAM");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    String teamId = readRequired("Team ID");
    String targetPlayerId = readRequired("Player ID to add");
    String result = teamService.addPlayerToTeam(teamId, String.valueOf(loggedInPlayer.getPlayerId()), targetPlayerId);
    switch (result) {
        case TeamService.R_ADD_TEAM_NOT_FOUND   -> printError("Team not found.");
        case TeamService.R_ADD_NOT_CAPTAIN      -> printError("Only the team captain can add players.");
        case TeamService.R_ADD_PLAYER_NOT_FOUND -> printError("Player not found. Check the Player ID.");
        case TeamService.R_ADD_ALREADY_MEMBER   -> printError("Player is already in this team.");
        case TeamService.R_ADD_TEAM_FULL        -> printError("Team is full (" + Team.MAX_MEMBERS + " max).");
        case TeamService.R_ADD_SPORT_MISMATCH   -> printError("Player's sport does not match team sport.");
        case TeamService.R_ADD_SUCCESS          -> printSuccess("Player added to team successfully!");
        default                                 -> printError("Unknown error.");
    }
    pause();
}

static void removePlayerFromTeamModule() {
    printHeader("REMOVE PLAYER FROM TEAM");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    String teamId = readRequired("Team ID");
    String targetPlayerId = readRequired("Player ID to remove");
    String result = teamService.removeMemberAsCaptain(teamId, String.valueOf(loggedInPlayer.getPlayerId()), targetPlayerId);
    switch (result) {
        case TeamService.R_REMOVE_TEAM_NOT_FOUND -> printError("Team not found.");
        case TeamService.R_REMOVE_NOT_CAPTAIN    -> printError("Only the captain can remove players.");
        case TeamService.R_REMOVE_CAPTAIN_SELF   -> printError("Captain cannot remove themselves. Use Leave Team.");
        case TeamService.R_REMOVE_NOT_IN_TEAM    -> printError("Player is not in this team.");
        case TeamService.R_REMOVE_SUCCESS        -> printSuccess("Player removed from team.");
        default                                  -> printError("Unknown error.");
    }
    pause();
}

static void leaveTeamModule() {
    printHeader("LEAVE TEAM");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    String teamId = readRequired("Team ID");
    String result = teamService.leaveTeamSafe(String.valueOf(loggedInPlayer.getPlayerId()), teamId);
    switch (result) {
        case TeamService.R_LEAVE_TEAM_NOT_FOUND -> printError("Team not found.");
        case TeamService.R_LEAVE_NOT_IN_TEAM    -> printError("You are not a member of this team.");
        case TeamService.R_LEAVE_SUCCESS        -> printSuccess("Left team successfully.");
        default                                 -> printError("Unknown error.");
    }
    pause();
}

static void deleteTeamModule() {
    printHeader("DELETE TEAM");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    String teamId = readRequired("Team ID");
    // confirm
    while (true) {
        prompt("Confirm delete team " + teamId + "? (yes/no)");
        String confirm = sc.nextLine().trim().toLowerCase();
        if (confirm.equals("yes")) break;
        if (confirm.equals("no")) { printInfo("Cancelled."); pause(); return; }
        printError("Please type yes or no.");
    }
    String result = teamService.deleteTeamAsCaptain(teamId, String.valueOf(loggedInPlayer.getPlayerId()));
    switch (result) {
        case TeamService.R_DELETE_TEAM_NOT_FOUND -> printError("Team not found.");
        case TeamService.R_DELETE_NOT_CAPTAIN    -> printError("Only the captain can delete the team.");
        case TeamService.R_DELETE_SUCCESS        -> printSuccess("Team deleted successfully.");
        default                                  -> printError("Unknown error.");
    }
    pause();
}

// ═════════════════════════════════════════════════════════════════════════════
//  SESSIONS
// ═════════════════════════════════════════════════════════════════════════════
static void manageSessionMenu() {
    String[] valid = {"1","2","3","4","5","0"};
    while (true) {
        printHeader("MANAGE SESSIONS");
        System.out.println("    1.  Schedule Game");
        System.out.println("    2.  View All Sessions");
        System.out.println("    3.  Book Session");
        System.out.println("    4.  Mark Session Completed");
        System.out.println("    5.  Cancel Session");
        System.out.println("    0.  Back");
        switch (readMenuChoice(valid)) {
            case "1" -> scheduleGameModule();
            case "2" -> viewSessionsModule();
            case "3" -> bookSessionModule();
            case "4" -> markSessionCompletedModule();
            case "5" -> cancelSessionModule();
            case "0" -> { return; }
        }
    }
}

static void scheduleGameModule() {
    printHeader("SCHEDULE GAME");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    String playerId = String.valueOf(loggedInPlayer.getPlayerId());
    List<Team> myTeams = teamService.getAllTeams().stream()
        .filter(t -> t.getMemberIDs().contains(playerId)).collect(Collectors.toList());
    if (myTeams.isEmpty()) { printError("You are not in any team. Join or create a team first."); pause(); return; }
    System.out.println("  Your teams:");
    for (Team t : myTeams)
        System.out.printf("  [%s] %s — %s%n", t.getTeamID(), t.getTeamName(), t.getSport());
    System.out.println();
    String teamId;
    while (true) {
        teamId = readRequired("Team ID to schedule for");
        Team team = teamService.getTeamByID(teamId);
        if (team == null) { printError("Team not found. Please check the Team ID."); continue; }
        if (!team.getMemberIDs().contains(playerId)) { printError("You must be a member of this team."); continue; }
        break;
    }
    String dateStr = readDate();
    String time    = readRequired("Time (e.g. 3:00 PM)");
    String venue   = readRequired("Venue");
    try {
        LocalDate date = LocalDate.parse(dateStr);
        GameSession session = gameSessionService.createSession(teamId, teamService.getTeamByID(teamId).getSport(), date, time, venue);
        if (session == null) printError("Could not create session (past date or scheduling conflict).");
        else printSuccess("Session scheduled! Session ID: " + session.getSessionID());
    } catch (Exception e) { printError("Invalid date: " + e.getMessage()); }
    pause();
}

static void viewSessionsModule() {
    printHeader("ALL SESSIONS");
    List<GameSession> sessions = gameSessionService.getAllSessions();
    if (sessions.isEmpty()) { printInfo("No sessions available."); pause(); return; }
    System.out.println("  +--------+--------+----------+------+---------------------+----------+");
    System.out.println("  | SessID | TeamID | Date     | Time | Venue               | Status   |");
    System.out.println("  +--------+--------+----------+------+---------------------+----------+");
    for (GameSession s : sessions)
        System.out.printf("  | %-6s | %-6s | %-10s | %-4s | %-19s | %-8s |%n",
            s.getSessionID(), s.getTeamID(), s.getDate(),
            s.getTime(), s.getVenue(), s.getStatusDisplay());
    System.out.println("  +--------+--------+----------+------+---------------------+----------+");
    pause();
}

static void bookSessionModule() {
    printHeader("BOOK SESSION");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    String sessionId = readRequired("Session ID");
    String teamId    = readRequired("Team ID");
    String actorId   = String.valueOf(loggedInPlayer.getPlayerId());
    String result    = gameSessionService.bookSession(actorId, sessionId, teamId);
    switch (result) {
        case GameSessionService.R_BOOK_SESSION_NOT_FOUND  -> printError("Session not found.");
        case GameSessionService.R_BOOK_TEAM_NOT_FOUND     -> printError("Team not found.");
        case GameSessionService.R_BOOK_NOT_TEAM_MEMBER    -> printError("You must be a team member to book this session.");
        case GameSessionService.R_BOOK_FAILED             -> printError("Booking failed (duplicate, mismatch, or unavailable session).");
        case GameSessionService.R_BOOK_SUCCESS -> {
            Booking latest = gameSessionService.getBookingsByTeam(teamId).stream()
                .filter(b -> b.getSessionID().equalsIgnoreCase(sessionId))
                .reduce((a, b) -> b).orElse(null);
            if (latest != null) printSuccess("Session booked! Booking ID: " + latest.getBookingID());
            else printSuccess("Session booked successfully!");
        }
        default -> printError("Unknown error.");
    }
    pause();
}

static void markSessionCompletedModule() {
    printHeader("MARK SESSION COMPLETED");
    List<GameSession> sessions = gameSessionService.getAllSessions();
    if (sessions.isEmpty()) { printError("No sessions available."); pause(); return; }
    System.out.println("  +--------+--------+----------------------+----------+");
    System.out.println("  | SessID | TeamID | Date / Venue         | Status   |");
    System.out.println("  +--------+--------+----------------------+----------+");
    for (GameSession s : sessions)
        System.out.printf("  | %-6s | %-6s | %-20s | %-8s |%n",
            s.getSessionID(), s.getTeamID(),
            s.getDate() + " " + s.getVenue(), s.getStatusDisplay());
    System.out.println("  +--------+--------+----------------------+----------+");
    System.out.println();
    String sessionId;
    while (true) {
        prompt("Enter Session ID to mark completed (0 to cancel)");
        sessionId = sc.nextLine().trim();
        if ("0".equals(sessionId)) return;
        if (!sessionId.isEmpty()) break;
        printError("Session ID cannot be empty.");
    }
    switch (gameSessionService.completeSessionValidated(sessionId)) {
        case GameSessionService.R_COMPLETE_NOT_FOUND         -> { printError("Session not found."); pause(); return; }
        case GameSessionService.R_COMPLETE_ALREADY_CANCELLED -> { printError("Cannot complete a cancelled session."); pause(); return; }
        case GameSessionService.R_COMPLETE_ALREADY_COMPLETED -> { printError("Session is already completed."); pause(); return; }
        case GameSessionService.R_COMPLETE_SUCCESS -> {
            printSuccess("Session marked COMPLETED. Rating and Payment are now unlocked!");
            System.out.println();
            prompt("Rate a player from this session? (y/n)");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                List<Player> players = playerService.getAllActivePlayers().stream()
                    .filter(p -> !p.getPlayerId().equals(loggedInPlayer.getPlayerId()))
                    .collect(Collectors.toList());
                printPlayerTable(players);
                System.out.println();
                int ratedId = readPositiveInt("Enter Player ID to rate (0 to cancel)");
                if (ratedId != 0) {
                    try {
                        Player rated = playerService.getPlayerById((long) ratedId);
                        float stars = readStars();
                        String comment = readRequired("Comment");
                        ratingService.addRating(loggedInPlayer, rated, sessionId, stars, comment);
                        printSuccess("Rating submitted for " + rated.getName() + "!");
                    } catch (Exception e) { printError(e.getMessage()); }
                }
            }
        }
        default -> printError("Unknown error.");
    }
    pause();
}

static void cancelSessionModule() {
    printHeader("CANCEL SESSION");
    if (loggedInPlayer == null) { printError("Please login first."); return; }
    String sessionId = readRequired("Session ID");
    String actorId   = String.valueOf(loggedInPlayer.getPlayerId());
    String result    = gameSessionService.cancelSession(sessionId, actorId, false);
    switch (result) {
        case GameSessionService.R_CANCEL_SESSION_NOT_FOUND        -> printError("Session not found.");
        case GameSessionService.R_CANCEL_SESSION_TEAM_NOT_FOUND   -> printError("Team not found for this session.");
        case GameSessionService.R_CANCEL_SESSION_NOT_CAPTAIN      -> printError("Only the team captain can cancel this session.");
        case GameSessionService.R_CANCEL_SESSION_ALREADY_CANCELLED-> printError("Session is already cancelled.");
        case GameSessionService.R_CANCEL_SESSION_SUCCESS          -> printSuccess("Session cancelled successfully.");
        default -> printError("Unknown error.");
    }
    pause();
}

// ═════════════════════════════════════════════════════════════════════════════
//  RATING
// ═════════════════════════════════════════════════════════════════════════════
static void ratePlayerModule() {
    printHeader("RATE A PLAYER");
    List<Player> players = playerService.getAllActivePlayers().stream()
        .filter(p -> !p.getPlayerId().equals(loggedInPlayer.getPlayerId()))
        .collect(Collectors.toList());
    if (players.isEmpty()) { printInfo("No players available to rate."); pause(); return; }
    System.out.println("  +------+----------------------+--------+");
    System.out.println("  | ID   | Name                 | Rating |");
    System.out.println("  +------+----------------------+--------+");
    for (Player p : players)
        System.out.printf("  | %-4d | %-20s | %-6.1f |%n",
            p.getPlayerId(), p.getName(), ratingService.getAverageRatingById(p.getPlayerId()));
    System.out.println("  +------+----------------------+--------+");
    System.out.println();
    // cannot rate self
    int ratedId;
    while (true) {
        ratedId = readPositiveInt("Enter Player ID to rate (0 to cancel)");
        if (ratedId == 0) return;
        if (loggedInPlayer.getPlayerId() != null && (long) ratedId == loggedInPlayer.getPlayerId()) {
            printError("You cannot rate yourself.");
            continue;
        }
        break;
    }
    try {
        Player rated  = playerService.getPlayerById((long) ratedId);
        float  stars  = readStars();
        String comment = readRequired("Comment");
        ratingService.addRating(loggedInPlayer, rated, "MANUAL", stars, comment);
        printSuccess("Rating submitted for " + rated.getName() + "! (" + stars + " stars)");
    } catch (Exception e) { printError(e.getMessage()); }
    pause();
}

// ═════════════════════════════════════════════════════════════════════════════
//  ADMIN MENUS
// ═════════════════════════════════════════════════════════════════════════════
static void adminMenu() {
    String[] valid = {"1","2","3","0"};
    while (true) {
        System.out.println();
        System.out.println("  ==================================================");
        System.out.println("                   ADMIN PANEL                      ");
        System.out.printf ("  Logged in as: %-35s%n", loggedInAdmin.getUsername());
        System.out.println("  ==================================================");
        System.out.println("    1.  List All Players");
        System.out.println("    2.  Deactivate Player");
        System.out.println("    3.  View Persisted Signups");
        System.out.println("  ==================================================");
        System.out.println("    0.  Logout");
        System.out.println("  ==================================================");
        switch (readMenuChoice(valid)) {
            case "1" -> listPlayersTable();
            case "2" -> deactivatePlayer();
            case "3" -> viewPersistedSignupsAdmin();
            case "0" -> { logout(); return; }
        }
    }
}

static void superAdminMenu() {
    String[] valid = {"1","2","3","4","0"};
    while (true) {
        System.out.println();
        System.out.println("  ==================================================");
        System.out.println("                 SUPER ADMIN PANEL                  ");
        System.out.printf ("  Logged in as: %-35s%n", loggedInAdmin.getUsername());
        System.out.println("  ==================================================");
        System.out.println("    1.  List All Players");
        System.out.println("    2.  Delete Player");
        System.out.println("    3.  View Persisted Signups");
        System.out.println("    4.  View Payment Records");
        System.out.println("  ==================================================");
        System.out.println("    0.  Logout");
        System.out.println("  ==================================================");
        switch (readMenuChoice(valid)) {
            case "1" -> listPlayersTable();
            case "2" -> deletePlayer();
            case "3" -> viewPersistedSignupsAdmin();
            case "4" -> paymentService.viewPaymentsAsAdmin(loggedInAdmin.getPasswordHash(), sc);
            case "0" -> { logout(); return; }
        }
    }
}

static void listPlayersTable() {
    printHeader("ALL PLAYERS");
    List<Player> list = playerService.getAllPlayers();
    if (list.isEmpty()) { printInfo("No players registered."); pause(); return; }
    System.out.println("  +------+----------------------+-------------------+----------+-----------+--------+");
    System.out.println("  | ID   | Name                 | Email             | Sport    | Skill     | Rating |");
    System.out.println("  +------+----------------------+-------------------+----------+-----------+--------+");
    for (Player p : list)
        System.out.printf("  | %-4d | %-20s | %-17s | %-8s | %-9s | %-6.1f |%n",
            p.getPlayerId(), p.getName(), safeValue(p.getEmail()),
            safeValue(p.getSport()), safeValue(p.getSkill_level()),
            ratingService.getAverageRatingById(p.getPlayerId()));
    System.out.println("  +------+----------------------+-------------------+----------+-----------+--------+");
    printInfo("Total: " + list.size() + " players");
    pause();
}

static void deactivatePlayer() {
    printHeader("DEACTIVATE PLAYER");
    int id = readPositiveInt("Player ID to deactivate (0 to cancel)");
    if (id == 0) return;
    playerService.deactivatePlayer((long) id);
    printSuccess("Player " + id + " deactivated.");
    pause();
}

static void deletePlayer() {
    printHeader("DELETE PLAYER");
    int id = readPositiveInt("Player ID to delete (0 to cancel)");
    if (id == 0) return;
    while (true) {
        prompt("Confirm permanent delete of player " + id + "? (yes/no)");
        String confirm = sc.nextLine().trim().toLowerCase();
        if (confirm.equals("yes")) break;
        if (confirm.equals("no")) { printInfo("Cancelled."); pause(); return; }
        printError("Please type yes or no.");
    }
    playerService.deletePlayer((long) id);
    printSuccess("Player " + id + " deleted.");
    pause();
}

static void viewPersistedSignupsAdmin() {
    printHeader("PERSISTED SIGNUPS (DECRYPTED)");
    List<String> lines = signupStore.listDecryptedRecordsForAdmin();
    if (lines.isEmpty()) { printInfo("No signup records found."); }
    else lines.forEach(l -> System.out.println("  " + l));
    pause();
}

// ═════════════════════════════════════════════════════════════════════════════
//  SHARED HELPERS
// ═════════════════════════════════════════════════════════════════════════════
static void logout() {
    authService.logout(sessionToken);
    String name = loggedInPlayer != null ? loggedInPlayer.getName() :
                  loggedInAdmin  != null ? loggedInAdmin.getUsername() : "User";
    loggedInPlayer = null;
    loggedInAdmin  = null;
    sessionToken   = null;
    System.out.println();
    System.out.println("  ==================================================");
    System.out.println("  Logged out. See you next time, " + name + "!");
    System.out.println("  ==================================================");
    System.out.println();
}

static String resolvePlayerName(String playerIdText) {
    try {
        Long pid = Long.parseLong(playerIdText);
        return playerService.getPlayerById(pid).getName();
    } catch (Exception e) { return "Player#" + playerIdText; }
}

static void printPlayerTable(List<Player> list) {
    if (list.isEmpty()) { printInfo("No players found."); return; }
    System.out.println("  +------+----------------------+-------------------+----------+-----------+");
    System.out.println("  | ID   | Name                 | Email             | Sport    | Skill     |");
    System.out.println("  +------+----------------------+-------------------+----------+-----------+");
    for (Player p : list)
        System.out.printf("  | %-4d | %-20s | %-17s | %-8s | %-9s |%n",
            p.getPlayerId(), p.getName(), safeValue(p.getEmail()),
            safeValue(p.getSport()), safeValue(p.getSkill_level()));
    System.out.println("  +------+----------------------+-------------------+----------+-----------+");
}

static boolean isValidSport(String sport) {
    for (String s : VALID_SPORTS) if (s.equalsIgnoreCase(sport)) return true;
    return false;
}

static String readSport() {
    while (true) {
        System.out.println("  Valid sports: cricket | football | kabaddi | badminton | tennis | soccer | volleyball");
        prompt("Sport");
        String input = sc.nextLine().trim().toLowerCase();
        if (isValidSport(input)) return input;
        printError("Invalid sport '" + input + "'. Choose from: cricket, football, kabaddi, badminton, tennis, soccer, volleyball.");
    }
}

static String selectSkillLevel() {
    String[] valid = {"1","2","3"};
    while (true) {
        System.out.println("  Skill levels:");
        System.out.println("    1. BEGINNER");
        System.out.println("    2. INTERMEDIATE");
        System.out.println("    3. ADVANCED");
        switch (readMenuChoice(valid)) {
            case "1" -> { return "BEGINNER"; }
            case "2" -> { return "INTERMEDIATE"; }
            case "3" -> { return "ADVANCED"; }
        }
    }
}

static float readStars() {
    while (true) {
        System.out.println("  Stars must be between 1.0 and 5.0 (e.g. 4.5)");
        prompt("Stars (1-5)");
        String input = sc.nextLine().trim();
        try {
            float stars = Float.parseFloat(input);
            if (stars >= 1.0f && stars <= 5.0f) return stars;
            printError("Stars must be between 1 and 5. You entered: " + stars);
        } catch (NumberFormatException e) {
            printError("Invalid number '" + input + "'. Enter a value like 3 or 4.5.");
        }
    }
}

static String readDate() {
    while (true) {
        prompt("Date (YYYY-MM-DD)");
        String input = sc.nextLine().trim();
        if (input.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] p = input.split("-");
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2]);
            if (m >= 1 && m <= 12 && d >= 1 && d <= 31) return input;
        }
        printError("Invalid date '" + input + "'. Use format YYYY-MM-DD (e.g. 2026-05-20).");
    }
}

static String readRequired(String label) {
    while (true) {
        prompt(label);
        String val = sc.nextLine().trim();
        if (!val.isEmpty()) return val;
        printError("This field cannot be empty. Please try again.");
    }
}

static int readPositiveInt(String label) {
    while (true) {
        prompt(label);
        String input = sc.nextLine().trim();
        try {
            int val = Integer.parseInt(input);
            if (val >= 0) return val;
            printError("Please enter a positive number or 0.");
        } catch (NumberFormatException e) {
            printError("Invalid input '" + input + "'. Please enter a whole number.");
        }
    }
}

static String readMenuChoice(String[] valid) {
    while (true) {
        System.out.print("  Enter your choice: ");
        String input = sc.nextLine().trim();
        for (String v : valid) if (v.equals(input)) return input;
        printError("Invalid choice '" + input + "'. Please try again.");
    }
}

static int readInt() {
    try { return Integer.parseInt(sc.nextLine()); }
    catch (Exception e) { return -1; }
}

static int readIntInline() {
    try { return Integer.parseInt(sc.nextLine()); }
    catch (Exception e) { return 0; }
}

static void pause() {
    System.out.print("\n  Press Enter to continue...");
    sc.nextLine();
}

// ── Print helpers ─────────────────────────────────────────────────────────────
static void printHeader(String title) {
    System.out.println();
    System.out.println("  ==================================================");
    int pad = (50 - title.length()) / 2;
    StringBuilder sb = new StringBuilder("  ");
    for (int i = 0; i < pad; i++) sb.append(" ");
    sb.append(title);
    System.out.println(sb);
    System.out.println("  ==================================================");
    System.out.println();
}

static void printBox(String message) {
    System.out.println();
    System.out.println("  +------------------------------------------------+");
    System.out.printf ("  | %-46s |%n", message);
    System.out.println("  +------------------------------------------------+");
    System.out.println();
}

static void printSuccess(String msg) {
    System.out.println("\n  [OK] " + msg + "\n");
}

static void printError(String msg) {
    System.out.println("  [ERROR] " + msg);
}

static void printInfo(String msg) {
    System.out.println("  [INFO] " + msg);
}

static void prompt(String label) {
    System.out.print("  " + label + ": ");
}

static String safeValue(String value) {
    return (value == null || value.isBlank()) ? "Not Set" : value;
}

static void warn(String msg) { printError(msg); }

// ── Seed data ────────────────────────────────────────────────────────────────
static void seedData() {
    authService.registerAdmin("admin", "admin123", "admin@mail.com", "Admin");
    authService.registerSuperAdmin("superadmin", "super123", "super@mail.com", "Super");
    addDemo("Sahil Maniya",   "sahilmaniya0092@gmail.com", "4373222786", "cricket",    "INTERMEDIATE", "Toronto",     22, 3, "sahil123");
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

// ── Unused legacy stubs kept for compatibility ────────────────────────────────
static void printBanner() { System.out.println("\n=== SportConnect ===\n"); }
static void printPlayers(List<Player> list) { printPlayerTable(list); pause(); }

}
