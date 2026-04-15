import model.Admin;
import model.FriendRequest;
import model.Player;
import service.AuthService;
import service.ChatService;
import service.FriendRequestService;
import service.PlayerService;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {


static final AuthService   authService   = new AuthService();
static final PlayerService playerService = new PlayerService();
static final FriendRequestService friendRequestService = new FriendRequestService();
static final ChatService chatService = new ChatService();

static Player loggedInPlayer = null;
static Admin  loggedInAdmin  = null;
static String sessionToken   = null;

static final Scanner sc = new Scanner(System.in);

public static void main(String[] args) {
    seedData();
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
            case 2 -> searchBySport();
            case 3 -> sendFriendRequestModule();
            case 4 -> viewIncomingRequestsModule();
            case 5 -> openChatModule();
            case 6 -> workingOnModule("Create Team");
            case 7 -> workingOnModule("Join Team");
            case 8 -> workingOnModule("Schedule Game");
            case 9 -> workingOnModule("Mark Session Completed");
            case 10 -> workingOnModule("Rate a Player");
            case 11 -> workingOnModule("Make Payment");
            case 12 -> { logout(); return; }
            default -> warn("Invalid option.");
        }
    }
}

static void printPlayerMainMenu() {
    String name = loggedInPlayer != null ? loggedInPlayer.getName() : "Player";
    System.out.println("\n======================================");
    System.out.println("   Main Menu - " + name);
    System.out.println("======================================\n");
    System.out.println("1.  Update Profile");
    System.out.println("2.  Search Players");
    System.out.println("3.  Send Friend Request");
    System.out.println("4.  View Incoming Requests");
    System.out.println("5.  Open Chat");
    System.out.println("6.  Create Team");
    System.out.println("7.  Join Team");
    System.out.println("8.  Schedule Game");
    System.out.println("9.  Mark Session Completed");
    System.out.println("10. Rate a Player");
    System.out.println("11. Make Payment");
    System.out.println("12. Logout");
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
        Player target = playerService.getPlayerById((long) toId);
        FriendRequest request = friendRequestService.sendRequest(loggedInPlayer, target);
        System.out.println("[OK] Friend request sent. Request ID: " + request.getRequestID());
    } catch (Exception e) {
        warn(e.getMessage());
    }
    pause();
}

static void viewIncomingRequestsModule() {
    printHeader("View Incoming Requests");
    List<FriendRequest> incoming = friendRequestService.getIncomingRequests(loggedInPlayer.getPlayerId());
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

    System.out.print("\nEnter Request ID to respond (or 0 to back): ");
    String requestId = sc.nextLine().trim();
    if ("0".equals(requestId)) {
        return;
    }

    System.out.print("Choose action: 1. Accept  2. Decline  (0 to cancel): ");
    int action = readIntInline();
    if (action == 0) {
        System.out.println("Cancelled.");
        pause();
        return;
    }

    try {
        boolean accept = action == 1;
        if (!accept && action != 2) {
            throw new IllegalArgumentException("Invalid action. Please choose 1 or 2.");
        }

        FriendRequest updated = friendRequestService.respondToRequest(
            requestId,
            loggedInPlayer.getPlayerId(),
            accept
        );

        if (accept) {
            Player requester = playerService.getPlayerById(Long.parseLong(updated.getFromPlayerID()));
            friendRequestService.sendAcceptanceEmail(loggedInPlayer, requester);
        }

        System.out.println("[OK] Request " + updated.getRequestID() + " marked as " + updated.getStatus() + ".");
    } catch (Exception e) {
        warn(e.getMessage());
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
        System.out.println("  0. Logout");

        switch (readInt()) {
            case 1 -> listPlayers();
            case 2 -> deactivatePlayer();
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
        System.out.println("  0. Logout");

        switch (readInt()) {
            case 1 -> listPlayers();
            case 2 -> deletePlayer();
            case 0 -> { logout(); return; }
        }
    }
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

static void searchBySport() {
    System.out.print("Enter sport: ");
    printPlayers(playerService.getPlayersBySport(sc.nextLine()));
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
