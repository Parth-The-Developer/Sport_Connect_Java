import model.Admin;
import model.Player;
import service.AuthService;
import service.PlayerService;

import java.util.List;
import java.util.Scanner;

/**
 * SportConnect — Console Application
 *
 * Module ownership:
 *   Sahil  → Player, Admin, AuthService, PlayerService        ← DONE
 *   Parth  → FriendRequest, Chat, Admin cancelSession/refund  ← TODO Week 2
 *   Kelvin → Team, GameSession, scheduleGame()                ← TODO Week 2
 *   Dhruv  → Rating, Payment, markSessionCompleted()          ← TODO Week 3
 *   All    → Main.java menus
 */
public class Main {

    // ── Services ──────────────────────────────────────────────────────────────
    static final AuthService   authService   = new AuthService();
    static final PlayerService playerService = new PlayerService();

    // TODO Parth  → static final FriendRequestService friendService  = new FriendRequestService();
    // TODO Parth  → static final ChatService          chatService    = new ChatService();
    // TODO Kelvin → static final TeamService          teamService    = new TeamService();
    // TODO Kelvin → static final GameService          gameService    = new GameService();
    // TODO Dhruv  → static final RatingService        ratingService  = new RatingService();
    // TODO Dhruv  → static final PaymentService       paymentService = new PaymentService();

    // ── Session state ─────────────────────────────────────────────────────────
    static Player loggedInPlayer = null;
    static Admin  loggedInAdmin  = null;
    static String sessionToken   = null;

    static final Scanner sc = new Scanner(System.in);

    // =========================================================================
    // ENTRY POINT
    // =========================================================================

    public static void main(String[] args) {
        seedData();
        mainMenu();
        sc.close();
    }

    // =========================================================================
    // MAIN MENU
    // =========================================================================

    static void mainMenu() {
        while (true) {
            printBanner();
            System.out.println("  1. Player Portal");
            System.out.println("  2. Admin Portal");
            System.out.println("  0. Exit");
            System.out.print("\n  Choice: ");
            switch (readInt()) {
                case 1 -> playerPortal();
                case 2 -> adminPortal();
                case 0 -> { System.out.println("\n  Goodbye!\n"); return; }
                default -> warn("Invalid option.");
            }
        }
    }

    // =========================================================================
    // PLAYER PORTAL  (Sahil)
    // =========================================================================

    static void playerPortal() {
        while (true) {
            printHeader("Player Portal");

            if (loggedInPlayer == null) {
                System.out.println("  1. Sign Up");
                System.out.println("  2. Log In");
                System.out.println("  0. Back");
                System.out.print("\n  Choice: ");
                switch (readInt()) {
                    case 1 -> playerSignUp();
                    case 2 -> playerLogin();
                    case 0 -> { return; }
                    default -> warn("Invalid option.");
                }
            } else {
                System.out.println("  Logged in as: " + loggedInPlayer.getDisplayName());
                System.out.println();
                System.out.println("  1. View My Profile");
                System.out.println("  2. Search Players by Sport");
                System.out.println("  3. Search Players by Skill Level");
                System.out.println("  4. Search Players by City");
                System.out.println("  ─────────────────────────────────────");
                System.out.println("  5. Friend Requests     [Parth - Week 2]");
                System.out.println("  6. Chat                [Parth - Week 2]");
                System.out.println("  7. Join / Schedule Game[Kelvin - Week 2]");
                System.out.println("  8. Ratings & Payments  [Dhruv - Week 3]");
                System.out.println("  ─────────────────────────────────────");
                System.out.println("  0. Log Out");
                System.out.print("\n  Choice: ");
                switch (readInt()) {
                    case 1 -> viewMyProfile();
                    case 2 -> searchBySport();
                    case 3 -> searchBySkill();
                    case 4 -> searchByCity();
                    case 5, 6 -> stub("Parth's module — coming Week 2");
                    case 7    -> stub("Kelvin's module — coming Week 2");
                    case 8    -> stub("Dhruv's module — coming Week 3");
                    case 0    -> { playerLogout(); return; }
                    default   -> warn("Invalid option.");
                }
            }
        }
    }

    // ── Sign Up ───────────────────────────────────────────────────────────────

    static void playerSignUp() {
        printHeader("Player Sign Up");
        try {
            System.out.print("  Full name    : "); String name  = sc.nextLine().trim();
            System.out.print("  Email        : "); String email = sc.nextLine().trim();
            System.out.print("  Password     : "); String pass  = sc.nextLine().trim();
            System.out.print("  Phone        : "); String phone = sc.nextLine().trim();
            System.out.print("  Sport        : "); String sport = sc.nextLine().trim();

            // Register in AuthService (handles email uniqueness)
            Player player = authService.registerPlayer(email, name, phone, sport);
            player.setPasswordHash(pass);

            // Collect extra profile fields
            System.out.print("  City         : "); player.setCity(sc.nextLine().trim());
            System.out.print("  Skill level\n"
                           + "  (BEGINNER / INTERMEDIATE / ADVANCED): ");
            player.setSkill_level(sc.nextLine().trim().toUpperCase());
            System.out.print("  Age          : "); player.setAge(readIntInline());

            // Also register in PlayerService for search
            playerService.addPlayer(player);

            loggedInPlayer = player;
            sessionToken   = generateSessionToken(email, "PLAYER");
            ok("Welcome to SportConnect, " + player.getDisplayName() + "!");
        } catch (Exception e) {
            warn("Sign-up failed: " + e.getMessage());
        }
    }

    // ── Log In ────────────────────────────────────────────────────────────────

    static void playerLogin() {
        printHeader("Player Log In");
        try {
            System.out.print("  Email    : "); String email = sc.nextLine().trim();
            System.out.print("  Password : "); String pass  = sc.nextLine().trim();

            sessionToken   = authService.playerLogin(email, pass);
            loggedInPlayer = authService.getPlayerByEmail(email);
            ok("Welcome back, " + loggedInPlayer.getDisplayName() + "!");
        } catch (Exception e) {
            warn("Login failed: " + e.getMessage());
        }
    }

    static void playerLogout() {
        authService.logout(sessionToken);
        ok("Logged out. See you next time, " + loggedInPlayer.getDisplayName() + "!");
        loggedInPlayer = null;
        sessionToken   = null;
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    static void viewMyProfile() {
        printHeader("My Profile");
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
        pause();
    }

    // ── Search ────────────────────────────────────────────────────────────────

    static void searchBySport() {
        printHeader("Search by Sport");
        System.out.print("  Enter sport: ");
        printPlayerList(playerService.getPlayersBySport(sc.nextLine().trim()));
    }

    static void searchBySkill() {
        printHeader("Search by Skill Level");
        System.out.println("  Options: BEGINNER | INTERMEDIATE | ADVANCED");
        System.out.print("  Enter level: ");
        printPlayerList(playerService.getPlayersBySkillLevel(sc.nextLine().trim()));
    }

    static void searchByCity() {
        printHeader("Search by City");
        System.out.print("  Enter city: ");
        printPlayerList(playerService.getPlayersByCity(sc.nextLine().trim()));
    }

    // =========================================================================
    // ADMIN PORTAL  (Sahil)
    // =========================================================================

    static void adminPortal() {
        if (loggedInAdmin == null) {
            adminLogin();
            if (loggedInAdmin == null) return;
        }

        while (true) {
            printHeader("Admin Portal — " + loggedInAdmin.getUsername()
                      + "  [" + loggedInAdmin.getRole() + "]");
            System.out.println("  1. List All Players");
            System.out.println("  2. Deactivate a Player");
            System.out.println("  3. Activate a Player");
            System.out.println("  4. Delete a Player (permanent)");
            System.out.println("  ─────────────────────────────────────");
            System.out.println("  5. Cancel Session & Refund [Parth - Week 2]");
            System.out.println("  6. Manage Game Sessions    [Kelvin - Week 2]");
            System.out.println("  7. Payment Reports         [Dhruv - Week 3]");
            System.out.println("  ─────────────────────────────────────");
            System.out.println("  0. Admin Logout");
            System.out.print("\n  Choice: ");
            switch (readInt()) {
                case 1 -> adminListPlayers();
                case 2 -> adminDeactivatePlayer();
                case 3 -> adminActivatePlayer();
                case 4 -> adminDeletePlayer();
                case 5 -> stub("Parth's module — coming Week 2");
                case 6 -> stub("Kelvin's module — coming Week 2");
                case 7 -> stub("Dhruv's module — coming Week 3");
                case 0 -> { adminLogout(); return; }
                default -> warn("Invalid option.");
            }
        }
    }

    static void adminLogin() {
        printHeader("Admin Login");
        try {
            System.out.print("  Username : "); String user = sc.nextLine().trim();
            System.out.print("  Password : "); String pass = sc.nextLine().trim();

            sessionToken  = authService.adminLogin(user, pass);
            loggedInAdmin = authService.getAdminByUsername(user);
            ok("Welcome, " + loggedInAdmin.getFullName() + "!");
        } catch (Exception e) {
            warn("Admin login failed: " + e.getMessage());
        }
    }

    static void adminLogout() {
        authService.logout(sessionToken);
        ok("Admin session ended.");
        loggedInAdmin = null;
        sessionToken  = null;
    }

    static void adminListPlayers() {
        printHeader("All Players (" + playerService.getTotalPlayers() + " total)");
        printPlayerList(playerService.getAllPlayers());
    }

    static void adminDeactivatePlayer() {
        printHeader("Deactivate Player");
        System.out.print("  Player ID: ");
        try { playerService.deactivatePlayer((long) readIntInline()); ok("Player deactivated."); }
        catch (Exception e) { warn(e.getMessage()); }
    }

    static void adminActivatePlayer() {
        printHeader("Activate Player");
        System.out.print("  Player ID: ");
        try { playerService.activatePlayer((long) readIntInline()); ok("Player activated."); }
        catch (Exception e) { warn(e.getMessage()); }
    }

    static void adminDeletePlayer() {
        printHeader("Delete Player");
        System.out.print("  Player ID: ");
        try { playerService.deletePlayer((long) readIntInline()); ok("Player deleted."); }
        catch (Exception e) { warn(e.getMessage()); }
    }

    // =========================================================================
    // DISPLAY HELPERS
    // =========================================================================

    static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════╗");
        System.out.println("  ║      SportConnect  v1.0      ║");
        System.out.println("  ╚══════════════════════════════╝");
        System.out.println();
    }

    static void printHeader(String title) {
        System.out.println();
        System.out.println("  ── " + title + " " + "─".repeat(Math.max(0, 34 - title.length())));
        System.out.println();
    }

    static void printPlayerList(List<Player> list) {
        if (list.isEmpty()) {
            System.out.println("  No players found.");
        } else {
            System.out.printf("  %-4s  %-20s  %-12s  %-14s  %-12s  %s%n",
                "ID", "Name", "Sport", "Skill", "City", "Rating");
            System.out.println("  " + "─".repeat(74));
            for (Player p : list) {
                System.out.printf("  %-4d  %-20s  %-12s  %-14s  %-12s  %.1f  %s%n",
                    p.getPlayerId(),
                    cut(p.getName(),        20),
                    cut(p.getSport(),       12),
                    cut(p.getSkill_level(), 14),
                    cut(p.getCity(),        12),
                    p.getRating(),
                    p.isActive() ? "" : "[inactive]");
            }
            System.out.println("\n  " + list.size() + " player(s) found.");
        }
        pause();
    }

    static void ok(String msg)   { System.out.println("\n  [OK] " + msg + "\n"); }
    static void warn(String msg) { System.out.println("\n  [!]  " + msg + "\n"); }
    static void stub(String msg) { warn("Not yet integrated: " + msg); }

    static void pause() {
        System.out.print("\n  Press Enter to continue...");
        sc.nextLine();
    }

    static String cut(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    static int readInt() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    static int readIntInline() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    static String generateSessionToken(String id, String type) {
        return type + "_" + id + "_" + System.currentTimeMillis();
    }

    // =========================================================================
    // SEED DATA — works on first launch without any setup
    // =========================================================================

    static void seedData() {
        // Default admin account  →  username: admin  |  password: admin123
        authService.registerAdmin("admin", "admin123",
                                  "admin@sportconnect.com", "System Admin");
        authService.registerSuperAdmin("superadmin", "super123",
                               "super@sportconnect.com", "Super Admin");

        // Demo players so search works immediately
           addDemo("Arjun Sharma",  "arjun@demo.com",  "416-111-2222",
            "Basketball", "INTERMEDIATE", "Toronto",     22, 3);
    addDemo("Priya Desai",   "priya@demo.com",  "647-333-4444",
            "Soccer",     "ADVANCED",     "Mississauga", 25, 5);
    addDemo("Kevin Osei",    "kevin@demo.com",  "905-555-6666",
            "Basketball", "BEGINNER",     "Brampton",    19, 1);
    addDemo("Sara Nguyen",   "sara@demo.com",   "416-777-8888",
            "Tennis",     "INTERMEDIATE", "Toronto",     23, 2);
    addDemo("Lien Tran",     "lien@demo.com",   "647-111-2222",
            "Soccer",     "BEGINNER",     "Toronto",     21, 2);
    addDemo("Brian Carter",  "brian@demo.com",  "905-222-3333",
            "Basketball", "ADVANCED",     "Brampton",    24, 5);
    addDemo("Shahshree Das", "shahshree@demo.com", "416-333-4444",
            "Tennis",     "INTERMEDIATE", "Mississauga", 22, 3);
    addDemo("Hassana Diallo","hassana@demo.com","647-444-5555",
            "Volleyball", "BEGINNER",     "Toronto",     20, 1);
    addDemo("Pooja Mehta",   "pooja@demo.com",  "905-555-6666",
            "Cricket",    "ADVANCED",     "Brampton",    26, 6);
    addDemo("Riddhi Shah",   "riddhi@demo.com", "416-666-7777",
            "Soccer",     "INTERMEDIATE", "Mississauga", 23, 4);
    }

    static void addDemo(String name, String email, String phone,
                        String sport, String skill, String city,
                        int age, int exp) {
        Player p = authService.registerPlayer(email, name, phone, sport);
        p.setPasswordHash("demo123");
        p.setSkill_level(skill);
        p.setCity(city);
        p.setAge(age);
        p.setExperience(exp);
        playerService.addPlayer(p);
    }
}
