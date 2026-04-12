package com.sportconnect;

import com.sportconnect.model.Admin;
import com.sportconnect.model.Player;
import com.sportconnect.service.AuthService;
import com.sportconnect.service.PlayerService;

import java.util.List;
import java.util.Scanner;

/**
 * SportConnect — Console Application Entry Point
 *
 * Module ownership (per project charter):
 *   Sahil  → Player, Admin, AuthService, PlayerService     ← DONE (Week 1)
 *   Parth  → FriendRequest, Chat, Admin cancel/refund      ← TODO Week 2
 *   Kelvin → Team, GameSession, scheduleGame()             ← TODO Week 2
 *   Dhruv  → Rating, Payment, markSessionCompleted()       ← TODO Week 3
 *   All    → Main.java menus (integrated here)
 */
public class Main {

    // ── Shared services (all modules will use these) ──────────────────────────
    static final AuthService   authService   = new AuthService();
    static final PlayerService playerService = new PlayerService();

    // TODO Parth  → static final FriendRequestService friendService = new FriendRequestService();
    // TODO Parth  → static final ChatService          chatService   = new ChatService();
    // TODO Kelvin → static final TeamService          teamService   = new TeamService();
    // TODO Kelvin → static final GameService          gameService   = new GameService();
    // TODO Dhruv  → static final RatingService        ratingService = new RatingService();
    // TODO Dhruv  → static final PaymentService       paymentService = new PaymentService();

    // ── Session state ─────────────────────────────────────────────────────────
    static Player loggedInPlayer = null;
    static Admin  loggedInAdmin  = null;
    static String sessionToken   = null;

    static final Scanner sc = new Scanner(System.in);

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        seedData();         // default admin + demo player so first run works
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
    // ── SAHIL: PLAYER PORTAL ─────────────────────────────────────────────────
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
                // ── logged-in player menu ──────────────────────────────────
                System.out.println("  Logged in as: " + loggedInPlayer.getDisplayName());
                System.out.println();
                System.out.println("  1. View My Profile");
                System.out.println("  2. Search Players by Sport");
                System.out.println("  3. Search Players by Skill Level");
                System.out.println("  4. Search Players by City");
                System.out.println("  ─────────────────────────────");
                System.out.println("  5. Friend Requests        [Parth - coming Week 2]");
                System.out.println("  6. Chat                   [Parth - coming Week 2]");
                System.out.println("  7. Join / Schedule Game   [Kelvin - coming Week 2]");
                System.out.println("  8. Ratings & Payments     [Dhruv - coming Week 3]");
                System.out.println("  ─────────────────────────────");
                System.out.println("  0. Log Out");
                System.out.print("\n  Choice: ");
                switch (readInt()) {
                    case 1 -> viewMyProfile();
                    case 2 -> searchBySport();
                    case 3 -> searchBySkill();
                    case 4 -> searchByCity();
                    case 5, 6 -> stub("Parth's module (Week 2)");
                    case 7    -> stub("Kelvin's module (Week 2)");
                    case 8    -> stub("Dhruv's module (Week 3)");
                    case 0    -> playerLogout();
                    default   -> warn("Invalid option.");
                }
            }
        }
    }

    // ── Sign Up ───────────────────────────────────────────────────────────────

    static void playerSignUp() {
        printHeader("Player Sign Up");
        try {
            System.out.print("  Full name   : "); String name  = sc.nextLine().trim();
            System.out.print("  Email       : "); String email = sc.nextLine().trim();
            System.out.print("  Phone       : "); String phone = sc.nextLine().trim();
            System.out.print("  Sport       : "); String sport = sc.nextLine().trim();

            Player player = authService.registerPlayer(email, name, phone, sport);

            // Collect optional profile details directly into PlayerService
            System.out.print("  City        : "); player.setCity(sc.nextLine().trim());
            System.out.print("  Skill level (BEGINNER / INTERMEDIATE / ADVANCED): ");
            player.setSkill_level(sc.nextLine().trim().toUpperCase());
            System.out.print("  Age         : ");
            player.setAge(readIntInline());

            playerService.addPlayer(player);

            ok("Account created! Welcome, " + player.getDisplayName() + "!");
            loggedInPlayer = player;
            sessionToken   = authService.playerLogin(email, email); // auto-login after signup
        } catch (Exception e) {
            warn("Sign-up failed: " + e.getMessage());
        }
    }

    // ── Log In ────────────────────────────────────────────────────────────────

    static void playerLogin() {
        printHeader("Player Log In");
        try {
            System.out.print("  Email   : "); String email = sc.nextLine().trim();
            System.out.print("  Password: "); String pass  = sc.nextLine().trim();

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
        System.out.printf("  Name       : %s%n",    p.getName());
        System.out.printf("  Email      : %s%n",    p.getEmail());
        System.out.printf("  Phone      : %s%n",    p.getPhone());
        System.out.printf("  Sport      : %s%n",    p.getSport());
        System.out.printf("  Skill      : %s%n",    p.getSkill_level());
        System.out.printf("  City       : %s%n",    p.getCity());
        System.out.printf("  Age        : %d%n",    p.getAge());
        System.out.printf("  Experience : %d yrs%n",p.getExperience());
        System.out.printf("  Rating     : %.1f%n",  p.getRating());
        pause();
    }

    // ── Search ────────────────────────────────────────────────────────────────

    static void searchBySport() {
        printHeader("Search by Sport");
        System.out.print("  Enter sport: "); String sport = sc.nextLine().trim();
        List<Player> results = playerService.getPlayersBySport(sport);
        printPlayerList(results);
    }

    static void searchBySkill() {
        printHeader("Search by Skill Level");
        System.out.println("  Options: BEGINNER | INTERMEDIATE | ADVANCED");
        System.out.print("  Enter skill level: "); String skill = sc.nextLine().trim();
        List<Player> results = playerService.getPlayersBySkillLevel(skill);
        printPlayerList(results);
    }

    static void searchByCity() {
        printHeader("Search by City");
        System.out.print("  Enter city: "); String city = sc.nextLine().trim();
        List<Player> results = playerService.getPlayersByCity(city);
        printPlayerList(results);
    }

    // =========================================================================
    // ── SAHIL: ADMIN PORTAL ──────────────────────────────────────────────────
    // =========================================================================

    static void adminPortal() {
        if (loggedInAdmin == null) {
            adminLogin();
            if (loggedInAdmin == null) return; // login failed
        }

        while (true) {
            printHeader("Admin Portal — " + loggedInAdmin.getUsername()
                        + " [" + loggedInAdmin.getRole() + "]");
            System.out.println("  1. List All Players");
            System.out.println("  2. Deactivate a Player");
            System.out.println("  3. Activate a Player");
            System.out.println("  4. Delete a Player");
            System.out.println("  ─────────────────────────────");
            System.out.println("  5. Cancel Session / Refund   [Parth - coming Week 2]");
            System.out.println("  6. Manage Game Sessions      [Kelvin - coming Week 2]");
            System.out.println("  7. Payment Reports           [Dhruv - coming Week 3]");
            System.out.println("  ─────────────────────────────");
            System.out.println("  0. Admin Logout");
            System.out.print("\n  Choice: ");
            switch (readInt()) {
                case 1 -> adminListPlayers();
                case 2 -> adminDeactivatePlayer();
                case 3 -> adminActivatePlayer();
                case 4 -> adminDeletePlayer();
                case 5 -> stub("Parth's module (Week 2)");
                case 6 -> stub("Kelvin's module (Week 2)");
                case 7 -> stub("Dhruv's module (Week 3)");
                case 0 -> { adminLogout(); return; }
                default -> warn("Invalid option.");
            }
        }
    }

    static void adminLogin() {
        printHeader("Admin Login");
        try {
            System.out.print("  Username: "); String user = sc.nextLine().trim();
            System.out.print("  Password: "); String pass = sc.nextLine().trim();

            sessionToken   = authService.adminLogin(user, pass);
            loggedInAdmin  = authService.getAdminByUsername(user);
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
        printHeader("All Players");
        List<Player> all = playerService.getAllPlayers();
        if (all.isEmpty()) { System.out.println("  No players registered yet."); }
        else { printPlayerList(all); }
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
        printHeader("Delete Player (permanent)");
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
        System.out.println("  ║      SportConnect v1.0       ║");
        System.out.println("  ╚══════════════════════════════╝");
        System.out.println();
    }

    static void printHeader(String title) {
        System.out.println();
        System.out.println("  ── " + title + " " + "─".repeat(Math.max(0, 30 - title.length())));
        System.out.println();
    }

    static void printPlayerList(List<Player> list) {
        if (list.isEmpty()) {
            System.out.println("  No players found.");
        } else {
            System.out.printf("  %-4s %-20s %-12s %-14s %-12s %s%n",
                "ID", "Name", "Sport", "Skill", "City", "Rating");
            System.out.println("  " + "─".repeat(72));
            for (Player p : list) {
                System.out.printf("  %-4d %-20s %-12s %-14s %-12s %.1f%n",
                    p.getPlayerId(),
                    truncate(p.getName(),    20),
                    truncate(p.getSport(),   12),
                    truncate(p.getSkill_level(), 14),
                    truncate(p.getCity(),    12),
                    p.getRating());
            }
            System.out.println("  " + list.size() + " player(s) found.");
        }
        pause();
    }

    static void ok(String msg)   { System.out.println("\n  [OK] " + msg + "\n"); }
    static void warn(String msg) { System.out.println("\n  [!] " + msg + "\n"); }
    static void stub(String msg) { warn("Module not yet integrated: " + msg); }

    static void pause() {
        System.out.print("\n  Press Enter to continue...");
        sc.nextLine();
    }

    static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    static int readInt() {
        try { int v = Integer.parseInt(sc.nextLine().trim()); return v; }
        catch (NumberFormatException e) { return -1; }
    }

    static int readIntInline() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    // =========================================================================
    // SEED DATA  — gives everyone something to work with immediately
    // =========================================================================

    static void seedData() {
        // Default admin (Sahil's module)
        authService.registerAdmin("admin", "admin123", "admin@sportconnect.com", "System Admin");

        // Demo players
        Player p1 = new Player("Arjun Sharma",  "arjun@demo.com",  "416-111-2222", "Basketball", 3);
        p1.setSkill_level("INTERMEDIATE"); p1.setCity("Toronto"); p1.setAge(22);
        playerService.addPlayer(p1);

        Player p2 = new Player("Priya Desai",   "priya@demo.com",  "647-333-4444", "Soccer",      5);
        p2.setSkill_level("ADVANCED");     p2.setCity("Mississauga"); p2.setAge(25);
        playerService.addPlayer(p2);

        Player p3 = new Player("Kevin Osei",    "kevin@demo.com",  "905-555-6666", "Basketball",  1);
        p3.setSkill_level("BEGINNER");     p3.setCity("Brampton"); p3.setAge(19);
        playerService.addPlayer(p3);
    }
}
