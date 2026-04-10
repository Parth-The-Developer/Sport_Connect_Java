
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    // ── In-memory data stores ─────────────────────────────────────────────────
    static ArrayList<Player> playerList = new ArrayList<>();
    static ArrayList<FriendRequest> requestList = new ArrayList<>();
    static ArrayList<Chat> chatList = new ArrayList<>();
    static ArrayList<Team> teamList = new ArrayList<>();
    static ArrayList<GameSession> sessionList = new ArrayList<>();
    static ArrayList<Rating> ratingList = new ArrayList<>();
    static ArrayList<Payment> paymentList = new ArrayList<>();
    static ArrayList<SportCategory> categoryList = new ArrayList<>();

    static Player currentPlayer = null;
    static Scanner scanner = new Scanner(System.in);
    static PlayerService playerService = new PlayerService(playerList);

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        loadCategories();
        showWelcomeMenu();
    }

    static void loadCategories() {
    }

    // ── Menus ─────────────────────────────────────────────────────────────────
    static void showWelcomeMenu() {
        System.out.println("==============================");
        System.out.println("   Welcome to SportConnect!  ");
        System.out.println("==============================");
        System.out.println("1. Sign Up");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }

    static void showMainMenu() {
        System.out.println("\n==============================");
        System.out.println(" Main Menu — " + currentPlayer.getFullName());
        System.out.println("==============================");
        System.out.println(" 1.  Update Profile");
        System.out.println(" 2.  Search Players");
        System.out.println(" 3.  Send Friend Request");
        System.out.println(" 4.  View Incoming Requests");
        System.out.println(" 5.  Open Chat");
        System.out.println(" 6.  Create Team");
        System.out.println(" 7.  Join Team");
        System.out.println(" 8.  Schedule Game");
        System.out.println(" 9.  Mark Session Completed");
        System.out.println("10.  Rate a Player");
        System.out.println("11.  Make Payment");
        System.out.println("12.  Logout");
        System.out.print("Enter your choice: ");
    }

    static void showAdminMenu() {
        System.out.println("\n==============================");
        System.out.println(" Admin Menu — " + currentPlayer.getFullName());
        System.out.println("==============================");
        System.out.println("1. View All Users");
        System.out.println("2. Cancel Session");
        System.out.println("3. Logout");
        System.out.print("Enter your choice: ");
    }

    // ── Feature stubs ─────────────────────────────────────────────────────────
    static void signUp() {
    }

    static void login() {
    }

    static void updateProfile() {
    }

    static void searchPlayers() {
    }

    static void sendFriendRequest() {
    }

    static void viewRequests() {
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
        currentPlayer = null;
    }

    // ── Admin stubs ───────────────────────────────────────────────────────────
    static void viewAllUsers() {
    }

    static void cancelSession() {
    }
}
