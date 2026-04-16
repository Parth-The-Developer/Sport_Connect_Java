
import enums.*;
import java.util.Scanner;
import model.*;
import service.PlayerService;

public class Main {

    // ── Valid sports list ─────────────────────────────────────────────────────
    static final String[] VALID_SPORTS = {
        "Football", "Tennis", "Basketball", "Cricket", "Badminton", "Volleyball"
    };

    // ── Arrays ────────────────────────────────────────────────────────────────
    static Player[] playerList = new Player[100];
    static FriendRequest[] requestList = new FriendRequest[200];
    static Chat[] chatList = new Chat[100];
    static Team[] teamList = new Team[100];
    static GameSession[] sessionList = new GameSession[100];
    static Rating[] ratingList = new Rating[200];
    static Payment[] paymentList = new Payment[200];
    static SportCategory[] categoryList = new SportCategory[10];

    static int playerCount = 0;
    static int requestCount = 0;
    static int chatCount = 0;
    static int teamCount = 0;
    static int sessionCount = 0;
    static int ratingCount = 0;
    static int paymentCount = 0;
    static int categoryCount = 0;

    static Player currentPlayer = null;
    static Scanner scanner = new Scanner(System.in);
    static PlayerService playerService;

    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        loadCategories();
        loadDummyData();
        playerService = new PlayerService(playerList, playerCount);
        showWelcomeMenu();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  VALIDATION HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    /**
     * Read a non-empty string — loops until user types something.
     */
    static String readRequired(String label) {
        while (true) {
            prompt(label);
            String val = scanner.nextLine().trim();
            if (!val.isEmpty()) {
                return val;
            }
            error("This field cannot be empty. Please try again.");
        }
    }

    /**
     * Read an optional string — returns empty string if user just presses
     * Enter.
     */
    static String readOptional(String label) {
        prompt(label);
        return scanner.nextLine().trim();
    }

    static String readSport() {
        while (true) {
            System.out.println("  Valid sports: Football | Tennis | Basketball | Cricket | Badminton | Volleyball");
            prompt("Sport");
            String input = scanner.nextLine().trim();
            for (String sport : VALID_SPORTS) {
                if (sport.equalsIgnoreCase(input)) {
                    return sport;
                }
            }
            error("Invalid sport '" + input + "'. Choose from: Football, Tennis, Basketball, Cricket, Badminton, Volleyball.");
        }
    }

    static SkillLevel readSkillLevel() {
        while (true) {
            System.out.println("  Valid levels: BEGINNER | INTERMEDIATE | ADVANCED");
            prompt("Skill Level");
            String input = scanner.nextLine().trim().toUpperCase();
            switch (input) {
                case "BEGINNER":
                    return SkillLevel.BEGINNER;
                case "INTERMEDIATE":
                    return SkillLevel.INTERMEDIATE;
                case "ADVANCED":
                    return SkillLevel.ADVANCED;
                default:
                    error("Invalid skill level '" + input + "'. Enter BEGINNER, INTERMEDIATE or ADVANCED.");
            }
        }
    }

    static SkillLevel readSkillLevelOptional() {
        while (true) {
            System.out.println("  Valid levels: BEGINNER | INTERMEDIATE | ADVANCED  (or press Enter for all)");
            prompt("Skill Level");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isEmpty()) {
                return null;
            }
            switch (input) {
                case "BEGINNER":
                    return SkillLevel.BEGINNER;
                case "INTERMEDIATE":
                    return SkillLevel.INTERMEDIATE;
                case "ADVANCED":
                    return SkillLevel.ADVANCED;
                default:
                    error("Invalid level '" + input + "'. Enter BEGINNER, INTERMEDIATE, ADVANCED or press Enter.");
            }
        }
    }

    static int readInt(String label, int min, int max) {
        while (true) {
            prompt(label);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                error("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                error("Invalid input '" + input + "'. Please enter a whole number.");
            }
        }
    }

    // /**
    //  * Read a valid sport name — loops until correct.
    //  */
    // static String readSport() {
    //     while (true) {
    //         System.out.println("  Valid sports: Football | Tennis | Basketball | Cricket | Badminton | Volleyball");
    //         prompt("Sport");
    //         String input = scanner.nextLine().trim();
    //         for (String s : VALID_SPORTS) {
    //             if (s.equalsIgnoreCase(input)) {
    //                 return s;
    //             }
    //         }
    //         error("Invalid sport '" + input + "'. Choose from: Football, Tennis, Basketball, Cricket, Badminton, Volleyball.");
    //     }
    // }

    // /**
    //  * Read a valid skill level — loops until correct.
    //  */
    // static SkillLevel readSkillLevel() {
    //     while (true) {
    //         System.out.println("  Valid levels: BEGINNER | INTERMEDIATE | ADVANCED");
    //         prompt("Skill Level");
    //         String input = scanner.nextLine().trim().toUpperCase();
    //         switch (input) {
    //             case "BEGINNER":
    //                 return SkillLevel.BEGINNER;
    //             case "INTERMEDIATE":
    //                 return SkillLevel.INTERMEDIATE;
    //             case "ADVANCED":
    //                 return SkillLevel.ADVANCED;
    //             default:
    //                 error("Invalid skill level '" + input + "'. Enter BEGINNER, INTERMEDIATE or ADVANCED.");
    //         }
    //     }
    // }

    // /**
    //  * Read an optional skill level — null means 'search all'.
    //  */
    // static SkillLevel readSkillLevelOptional() {
    //     while (true) {
    //         System.out.println("  Valid levels: BEGINNER | INTERMEDIATE | ADVANCED  (or press Enter for all)");
    //         prompt("Skill Level");
    //         String input = scanner.nextLine().trim().toUpperCase();
    //         if (input.isEmpty()) {
    //             return null;
    //         }
    //         switch (input) {
    //             case "BEGINNER":
    //                 return SkillLevel.BEGINNER;
    //             case "INTERMEDIATE":
    //                 return SkillLevel.INTERMEDIATE;
    //             case "ADVANCED":
    //                 return SkillLevel.ADVANCED;
    //             default:
    //                 error("Invalid level '" + input + "'. Enter BEGINNER, INTERMEDIATE, ADVANCED or press Enter.");
    //         }
    //     }
    // }

    // /**
    //  * Read integer between min and max (inclusive) — loops until valid.
    //  */
    // static int readInt(String label, int min, int max) {
    //     while (true) {
    //         prompt(label);
    //         String input = scanner.nextLine().trim();
    //         try {
    //             int val = Integer.parseInt(input);
    //             if (val >= min && val <= max) {
    //                 return val;
    //             }
    //             error("Please enter a number between " + min + " and " + max + ".");
    //         } catch (NumberFormatException e) {
    //             error("Invalid input '" + input + "'. Please enter a whole number.");
    //         }
    //     }
    // }

    // /**
    //  * Read a positive decimal number — loops until valid.
    //  */
    // static double readAmount(String label) {
    //     while (true) {
    //         prompt(label + " ($)");
    //         String input = scanner.nextLine().trim();
    //         try {
    //             double val = Double.parseDouble(input);
    //             if (val > 0) {
    //                 return val;
    //             }
    //             error("Amount must be greater than $0.00.");
    //         } catch (NumberFormatException e) {
    //             error("Invalid amount '" + input + "'. Enter a number like 20 or 15.50.");
    //         }
    //     }
    // }

    // /**
    //  * Read payment method — loops until CARD or PAYPAL.
    //  */
    // static PaymentMethod readPaymentMethod() {
    //     while (true) {
    //         System.out.println("  Methods: CARD | PAYPAL");
    //         prompt("Payment Method");
    //         String input = scanner.nextLine().trim().toUpperCase();
    //         switch (input) {
    //             case "CARD":
    //                 return PaymentMethod.CARD;
    //             case "PAYPAL":
    //                 return PaymentMethod.PAYPAL;
    //             default:
    //                 error("Invalid method '" + input + "'. Enter CARD or PAYPAL.");
    //         }
    //     }
    // }

    // /**
    //  * Read a date in YYYY-MM-DD format — loops until format is correct.
    //  */
    // static String readDate() {
    //     while (true) {
    //         prompt("Date (YYYY-MM-DD)");
    //         String input = scanner.nextLine().trim();
    //         if (input.matches("\\d{4}-\\d{2}-\\d{2}")) {
    //             String[] p = input.split("-");
    //             int m = Integer.parseInt(p[1]);
    //             int d = Integer.parseInt(p[2]);
    //             if (m >= 1 && m <= 12 && d >= 1 && d <= 31) {
    //                 return input;
    //             }
    //         }
    //         error("Invalid date '" + input + "'. Use format YYYY-MM-DD (e.g. 2026-05-20).");
    //     }
    // }

    /**
     * Read a menu choice from a fixed set — loops until valid.
     */
    static String readMenuChoice(String[] valid) {
        while (true) {
            System.out.print("  Enter your choice: ");
            String input = scanner.nextLine().trim();
            for (String v : valid) {
                if (v.equals(input)) {
                    return input;
                }
            }
            error("Invalid choice '" + input + "'. Please try again.");
        }
    }

    /**
     * Read a username that exists in playerList — loops until found.
     */
    static Player readExistingPlayer(String label) {
        while (true) {
            prompt(label);
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                error("Username cannot be empty.");
                continue;
            }
            Player p = playerService.findByUsername(username);
            if (p != null) {
                return p;
            }
            error("No player found with username '" + username + "'. Please try again.");
        }
    }

    // /**
    //  * Read a team index from the displayed list — 1-based, loops until valid.
    //  */
    // static int readTeamIndex() {
    //     return readInt("Select team number", 1, teamCount) - 1;
    // }

    // /**
    //  * Read a session index from the displayed list — 1-based, loops until
    //  * valid.
    //  */
    // static int readSessionIndex(int count) {
    //     return readInt("Select session number", 1, count) - 1;
    // }

    // ══════════════════════════════════════════════════════════════════════════
    //  PRINT HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    static void printHeader(String title) {
        System.out.println();
        System.out.println("==================================================");
        int pad = (50 - title.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pad; i++) {
            sb.append(" ");
        }
        sb.append(title);
        System.out.println(sb.toString());
        System.out.println("==================================================");
    }

    static void success(String msg) {
        System.out.println("\n  [OK] " + msg);
    }

    static void error(String msg) {
        System.out.println("  [ERROR] " + msg);
    }

    static void info(String msg) {
        System.out.println("  [INFO] " + msg);
    }

    static void prompt(String label) {
        System.out.print("  " + label + ": ");
    }

    static String spaces(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.max(0, n); i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    static String stars(double avg) {
        int full = (int) avg;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < full; i++) {
            sb.append("*");
        }
        for (int i = full; i < 5; i++) {
            sb.append("-");
        }
        return sb.toString();
    }

    static void printTableHeader(String c1, String c2, String c3, String c4) {
        System.out.println("  +------------------+--------------+------------+------------+");
        System.out.printf("  | %-18s| %-14s| %-12s| %-12s|%n", c1, c2, c3, c4);
        System.out.println("  +------------------+--------------+------------+------------+");
    }

    static void printTableRow(String c1, String c2, String c3, String c4) {
        System.out.printf("  | %-18s| %-14s| %-12s| %-12s|%n", c1, c2, c3, c4);
    }

    static void printTableFooter() {
        System.out.println("  +------------------+--------------+------------+------------+");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DUMMY DATA
    // ══════════════════════════════════════════════════════════════════════════
    static void loadCategories() {
        categoryList[categoryCount++] = new SportCategory("C1", "Football");
        categoryList[categoryCount++] = new SportCategory("C2", "Tennis");
        categoryList[categoryCount++] = new SportCategory("C3", "Basketball");
        categoryList[categoryCount++] = new SportCategory("C4", "Cricket");
        categoryList[categoryCount++] = new SportCategory("C5", "Badminton");
        categoryList[categoryCount++] = new SportCategory("C6", "Volleyball");
    }

    static void loadDummyData() {
        playerList[playerCount++] = new Player("P1", "sahil123", "pass123", "Sahil Maniya", "Tennis", SkillLevel.INTERMEDIATE, "Toronto", "Weekends");
        playerList[playerCount++] = new Player("P2", "parth99", "pass123", "Parth Patel", "Tennis", SkillLevel.INTERMEDIATE, "Toronto", "Weekends");
        playerList[playerCount++] = new Player("P3", "kelvin22", "pass123", "Kelvin Idoko", "Football", SkillLevel.ADVANCED, "Brampton", "Evenings");
        playerList[playerCount++] = new Player("P4", "sara_h", "pass123", "Sara Hussain", "Basketball", SkillLevel.BEGINNER, "Mississauga", "Weekends");
        playerList[playerCount++] = new Player("P5", "alex_k", "pass123", "Alex Kim", "Cricket", SkillLevel.INTERMEDIATE, "Scarborough", "Mornings");
        playerList[playerCount++] = new Player("P6", "priya_m", "pass123", "Priya Mehta", "Badminton", SkillLevel.ADVANCED, "Toronto", "Evenings");
        playerList[playerCount++] = new Player("P7", "james_o", "pass123", "James Okafor", "Football", SkillLevel.BEGINNER, "Brampton", "Weekends");
        playerList[playerCount++] = new Player("P8", "nina_r", "pass123", "Nina Rodriguez", "Volleyball", SkillLevel.INTERMEDIATE, "Toronto", "Mornings");
        playerList[playerCount++] = new Player("P9", "david_c", "pass123", "David Chen", "Basketball", SkillLevel.ADVANCED, "Markham", "Evenings");
        playerList[playerCount++] = new Player("P10", "fatima_b", "pass123", "Fatima Bangura", "Tennis", SkillLevel.BEGINNER, "Toronto", "Weekends");
        playerList[playerCount++] = new Admin("A1", "admin", "admin123", "Admin User", "Football", SkillLevel.ADVANCED, "Toronto", "Always", 1);

        requestList[requestCount] = new FriendRequest("REQ1", "P1", "P2", "2026-04-01");
        requestList[requestCount++].accept();
        requestList[requestCount++] = new FriendRequest("REQ2", "P3", "P1", "2026-04-02");

        chatList[chatCount] = new Chat("CH1", "P1", "P2");
        chatList[chatCount].sendMessage("P1", "Hey Parth, want to play tennis this weekend?");
        chatList[chatCount++].sendMessage("P2", "Sure! Saturday 3pm works for me.");

        teamList[teamCount] = new Team("T1", "Humber Smashers", "Tennis", "P1");
        teamList[teamCount++].addMember("P2");
        teamList[teamCount++] = new Team("T2", "Brampton Strikers", "Football", "P3");

        sessionList[sessionCount++] = new GameSession("S1", "T1", "2026-04-15", "3:00 PM", "Humber North Tennis Court");
        sessionList[sessionCount] = new GameSession("S2", "T1", "2026-03-20", "2:00 PM", "Humber South Court");
        sessionList[sessionCount++].markCompleted();

        ratingList[ratingCount] = new Rating("R1", "P1", "P2", "S2", 5, "Great match!");
        ratingList[ratingCount++].submit();
        playerList[1].addRating(5);

        ratingList[ratingCount] = new Rating("R2", "P2", "P1", "S2", 4, "Good game, solid skills.");
        ratingList[ratingCount++].submit();
        playerList[0].addRating(4);

        paymentList[paymentCount] = new Payment("PAY1", "P1", "S2", 20.00, PaymentMethod.CARD);
        paymentList[paymentCount++].processPayment();
        paymentList[paymentCount] = new Payment("PAY2", "P2", "S2", 20.00, PaymentMethod.PAYPAL);
        paymentList[paymentCount++].processPayment();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MENUS
    // ══════════════════════════════════════════════════════════════════════════
    static void showWelcomeMenu() {
        String[] valid = {"1", "2", "3"};
        while (true) {
            System.out.println();
            System.out.println("  ==================================================");
            System.out.println("           S P O R T C O N N E C T                 ");
            System.out.println("       Connect | Play | Compete | Achieve           ");
            System.out.println("  ==================================================");
            System.out.println("                                                    ");
            System.out.println("    1.  Sign Up                                     ");
            System.out.println("    2.  Login                                       ");
            System.out.println("    3.  Exit                                        ");
            System.out.println("                                                    ");
            System.out.println("  ==================================================");
            switch (readMenuChoice(valid)) {
                case "1":
                    signUp();
                    break;
                case "2":
                    login();
                    break;
                case "3":
                    System.out.println("\n  Goodbye! See you on the court.\n");
                    return;
            }
        }
    }

    static void showMainMenu() {
        String[] valid = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        while (currentPlayer != null && !(currentPlayer instanceof Admin)) {
            System.out.println();
            System.out.println("  ==================================================");
            System.out.printf("  Welcome, %-40s%n", currentPlayer.getFullName() + "!");
            System.out.printf("  %-49s%n",
                    currentPlayer.getSport() + " | " + currentPlayer.getSkillLevel()
                    + " | " + currentPlayer.getCity()
                    + " | " + stars(currentPlayer.getAverageRating()));
            System.out.println("  ==================================================");
            System.out.println("  -- PROFILE ----------------------------------------");
            System.out.println("    1.  Update Profile");
            System.out.println("  -- DISCOVER ----------------------------------------");
            System.out.println("    2.  Search Player");
            System.out.println("  -- SOCIAL ------------------------------------------");
            System.out.println("    3.  Send Friend Request");
            System.out.println("    4.  View Incoming Requests");
            System.out.println("    5.  Open Chat");
            System.out.println("  -- TEAMS & GAMES -----------------------------------");
            System.out.println("    6.  Create Team");
            System.out.println("    7.  Join Team");
            System.out.println("    8.  Schedule Game");
            System.out.println("    9.  Mark Session Completed");
            System.out.println("  -- POST-GAME ----------------------------------------");
            System.out.println("   10.  Rate a Player");
            System.out.println("   11.  Make Payment");
            System.out.println("  ==================================================");
            System.out.println("   12.  Logout");
            System.out.println("  ==================================================");
            switch (readMenuChoice(valid)) {
                case "1":
                    updateProfile();
                    break;
                case "2":
                    searchPlayers();
                    break;
                case "3":
                    sendFriendRequest();
                    break;
                case "4":
                    viewRequests();
                    break;
                case "5":
                    openChat();
                    break;
                case "6":
                    createTeam();
                    break;
                case "7":
                    joinTeam();
                    break;
                case "8":
                    scheduleGame();
                    break;
                case "9":
                    markSessionCompleted();
                    break;
                case "10":
                    ratePlayer();
                    break;
                case "11":
                    makePayment();
                    break;
                case "12":
                    logout();
                    break;
            }
        }
    }

    static void showAdminMenu() {
        String[] valid = {"1", "2", "3"};
        while (currentPlayer instanceof Admin) {
            System.out.println();
            System.out.println("  ==================================================");
            System.out.println("                   ADMIN PANEL                      ");
            System.out.printf("  Logged in as: %-35s%n", currentPlayer.getFullName());
            System.out.println("  ==================================================");
            System.out.println("    1.  View All User");
            System.out.println("    2.  Cancel Session");
            System.out.println("  ==================================================");
            System.out.println("    3.  Logout");
            System.out.println("  ==================================================");
            switch (readMenuChoice(valid)) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    cancelSession();
                    break;
                case "3":
                    logout();
                    break;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FEATURE METHODS
    // ══════════════════════════════════════════════════════════════════════════
    static void signUp() {
        printHeader("CREATE ACCOUNT");

        // username — must not be blank and must not already exist
        String username;
        while (true) {
            username = readRequired("Username");
            if (playerService.findByUsername(username) == null) {
                break;
            }
            error("Username '" + username + "' is already taken. Please choose another.");
        }

        // password — min 4 chars
        String password;
        while (true) {
            password = readRequired("Password");
            if (password.length() >= 4) {
                break;
            }
            error("Password must be at least 4 characters.");
        }

        String fullName = readRequired("Full Name");
        String sport = readSport();
        SkillLevel skill = readSkillLevel();
        String city = readRequired("City");
        String avail = readRequired("Availability (e.g. Weekends, Evenings)");

        String id = "P" + (playerCount + 1);
        Player p = playerService.signUp(id, username, password, fullName, sport, skill, city, avail);
        if (p != null) {
            playerCount = playerService.getPlayerCount();
            success("Account created! You can now login as " + username);
        }
    }

    static void login() {
        printHeader("LOGIN");
        // give 3 attempts then back to welcome
        int attempts = 0;
        while (attempts < 3) {
            String username = readRequired("Username");
            String password = readRequired("Password");
            currentPlayer = playerService.login(username, password);
            if (currentPlayer != null) {
                break;
            }
            attempts++;
            if (attempts < 3) {
                error("Invalid credentials. " + (3 - attempts) + " attempt(s) remaining.");
            }
        }
        if (currentPlayer == null) {
            error("Too many failed attempts. Returning to main menu.");
            return;
        }
        if (currentPlayer instanceof Admin) {
            success("Admin access granted.");
            showAdminMenu();
        } else {
            success("Login successful!");
            showMainMenu();
        }
    }

    static void updateProfile() {
        printHeader("UPDATE PROFILE");
        info("Current city: " + currentPlayer.getCity()
                + "  |  Availability: " + currentPlayer.getAvailability());
        System.out.println();

        // optional fields — blank = keep current
        prompt("New City (press Enter to keep '" + currentPlayer.getCity() + "')");
        String city = scanner.nextLine().trim();

        prompt("New Availability (press Enter to keep '" + currentPlayer.getAvailability() + "')");
        String avail = scanner.nextLine().trim();

        if (city.isEmpty() && avail.isEmpty()) {
            info("No changes made.");
            return;
        }
        currentPlayer.updateProfile(city, avail);
        success("Profile updated successfully.");
    }

    static void searchPlayers() {
        printHeader("SEARCH PLAYERS");
        String sport = readSport();
        SkillLevel skill = readSkillLevelOptional();

        Player[] results = playerService.searchPlayers(sport, skill);
        if (results.length == 0) {
            return;
        }

        System.out.println();
        printTableHeader("Name", "Sport", "Skill", "City");
        for (Player p : results) {
            printTableRow(p.getFullName(), p.getSport(), p.getSkillLevel().toString(), p.getCity());
        }
        printTableFooter();
    }

    static void sendFriendRequest() {
        printHeader("SEND FRIEND REQUEST");
        // can't send to yourself
        Player target;
        while (true) {
            target = readExistingPlayer("Enter username of player");
            if (!target.getPlayerID().equals(currentPlayer.getPlayerID())) {
                break;
            }
            error("You cannot send a friend request to yourself.");
        }

        System.out.println();
        info("Sending request to: " + target.getFullName()
                + "  (" + target.getSport() + " | " + target.getCity() + ")");

        FriendRequest req = playerService.sendFriendRequest(
                currentPlayer.getPlayerID(), target.getPlayerID(), requestList, requestCount);
        if (req != null) {
            requestList[requestCount++] = req;
            success("Friend request sent to " + target.getFullName() + "!");
        }
    }

    static void viewRequests() {
        printHeader("INCOMING FRIEND REQUESTS");
        boolean found = false;
        for (int i = 0; i < requestCount; i++) {
            if (requestList[i].getToPlayerID().equals(currentPlayer.getPlayerID())
                    && requestList[i].getStatus() == RequestStatus.PENDING) {
                Player from = playerService.findByID(requestList[i].getFromPlayerID());
                String fromName = from != null ? from.getFullName() : requestList[i].getFromPlayerID();
                System.out.println();
                System.out.println("  +------------------------------------------------+");
                System.out.println("  | From: " + fromName + spaces(40 - fromName.length()) + "|");
                System.out.println("  | Date: " + requestList[i].getDateSent() + "                               |");
                System.out.println("  | Status: PENDING                                |");
                System.out.println("  +------------------------------------------------+");

                // validated Accept/Decline
                System.out.println("  Enter A to Accept or D to Decline");
                while (true) {
                    System.out.print("  Your choice (A/D): ");
                    String ans = scanner.nextLine().trim().toUpperCase();
                    if (ans.equals("A")) {
                        requestList[i].accept();
                        success("You are now friends with " + fromName + "!");
                        break;
                    } else if (ans.equals("D")) {
                        requestList[i].decline();
                        info("Request from " + fromName + " declined.");
                        break;
                    } else {
                        error("Invalid input '" + ans + "'. Please enter A or D.");
                    }
                }
                found = true;
            }
        }
        if (!found) {
            info("No pending friend requests.");
        }
    }

    static void openChat() {
   
    }

    static void createTeam() {
  
    }

    static void joinTeam() {
    
    }

    static void scheduleGame() {

    }

    static void markSessionCompleted() {

    }

    static void ratePlayer() {
    
    }

    static void makePayment() {
   
    }

    static void logout() {
        System.out.println();
        System.out.println("  ==================================================");
        System.out.printf("  See you next time, %-30s%n", currentPlayer.getFullName() + "!");
        System.out.println("  ==================================================");
        currentPlayer = null;
    }

    // ── Admin Methods ─────────────────────────────────────────────────────────
    static void viewAllUsers() {
        printHeader("ALL REGISTERED USERS");
        System.out.println();
        System.out.println("  +----------+----------------------+-------------+--------------+--------+");
        System.out.println("  | Role     | Name                 | Sport       | City         | Rating |");
        System.out.println("  +----------+----------------------+-------------+--------------+--------+");
        for (int i = 0; i < playerCount; i++) {
            Player p = playerList[i];
            System.out.printf("  | %-8s | %-20s | %-11s | %-12s | %-6s |%n",
                    p.getRole(), p.getFullName(), p.getSport(),
                    p.getCity(), String.format("%.1f", p.getAverageRating()));
        }
        System.out.println("  +----------+----------------------+-------------+--------------+--------+");
        System.out.println();
        info("Total users: " + playerCount);
    }

    static void cancelSession() {
        printHeader("CANCEL SESSION");
        if (sessionCount == 0) {
            info("No sessions found.");
            return;
        }
        System.out.println();
        for (int i = 0; i < sessionCount; i++) {
            System.out.println("  [" + (i + 1) + "]  "
                    + sessionList[i].getDate() + "  " + sessionList[i].getTime()
                    + "  " + sessionList[i].getVenue()
                    + "  [" + sessionList[i].getStatus() + "]");
        }
        System.out.println();
        int idx = readInt("Select session number", 1, sessionCount) - 1;
        Admin admin = (Admin) currentPlayer;
        admin.cancelSession(sessionList[idx].getSessionID(),
                sessionList, sessionCount, paymentList, paymentCount);
    }
}
