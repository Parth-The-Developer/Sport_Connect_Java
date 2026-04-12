package service;

import model.Player;
import model.Admin;
import model.FriendRequest;
import enums.SkillLevel;
import enums.RequestStatus;

public class PlayerService {

    // ── Encapsulation ─────────────────────────────────────────────────────────
    private Player[] playerList;
    private int playerCount;

    // ── Constructor ───────────────────────────────────────────────────────────
    public PlayerService(Player[] playerList, int initialCount) {
        this.playerList = playerList;
        this.playerCount = initialCount;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int getPlayerCount() {
        return playerCount;
    }

    // ── Sign up ───────────────────────────────────────────────────────────────
    public Player signUp(String playerID, String username, String password,
            String fullName, String sport, SkillLevel skillLevel,
            String city, String availability) {

        for (int i = 0; i < playerCount; i++) {
            if (playerList[i].getUsername().equalsIgnoreCase(username)) {
                System.out.println("Username '" + username + "' is already taken.");
                return null;
            }
        }

        Player newPlayer;
        if (username.equalsIgnoreCase("admin")) {
            newPlayer = new Admin(playerID, username, password,
                    fullName, sport, skillLevel,
                    city, availability, 1);
        } else {
            newPlayer = new Player(playerID, username, password,
                    fullName, sport, skillLevel,
                    city, availability);
        }

        playerList[playerCount++] = newPlayer;
        System.out.println("Sign up successful! Welcome, " + fullName + ".");
        return newPlayer;
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    public Player login(String username, String password) {
        for (int i = 0; i < playerCount; i++) {
            if (playerList[i].getUsername().equalsIgnoreCase(username)
                    && playerList[i].getPassword().equals(password)) {
                System.out.println("Login successful! Hello, " + playerList[i].getFullName() + ".");
                return playerList[i];
            }
        }
        System.out.println("Invalid username or password. Please try again.");
        return null;
    }

    // ── Search players by sport and skill level ───────────────────────────────
    public Player[] searchPlayers(String sport, SkillLevel skillLevel) {
        Player[] results = new Player[playerCount];
        int count = 0;

        for (int i = 0; i < playerCount; i++) {
            boolean sportMatch = playerList[i].getSport().equalsIgnoreCase(sport);
            boolean skillMatch = skillLevel == null
                    || playerList[i].getSkillLevel() == skillLevel;
            if (sportMatch && skillMatch) {
                results[count++] = playerList[i];
            }
        }

        if (count == 0) {
            System.out.println("No players found for " + sport + " | " + skillLevel);
            return new Player[0];
        }

        Player[] trimmed = new Player[count];
        for (int i = 0; i < count; i++) {
            trimmed[i] = results[i];
        }

        System.out.println("Players found (" + count + "):");
        for (int i = 0; i < count; i++) {
            System.out.println((i + 1) + ". " + trimmed[i]);
        }
        return trimmed;
    }

    // ── Find by username ──────────────────────────────────────────────────────
    public Player findByUsername(String username) {
        for (int i = 0; i < playerCount; i++) {
            if (playerList[i].getUsername().equalsIgnoreCase(username)) {
                return playerList[i];
            }
        }
        return null;
    }

    // ── Find by playerID ──────────────────────────────────────────────────────
    public Player findByID(String playerID) {
        for (int i = 0; i < playerCount; i++) {
            if (playerList[i].getPlayerID().equals(playerID)) {
                return playerList[i];
            }
        }
        return null;
    }

    // ── Send friend request ───────────────────────────────────────────────────
    public FriendRequest sendFriendRequest(String fromID, String toID,
            FriendRequest[] requestList,
            int requestCount) {
        for (int i = 0; i < requestCount; i++) {
            FriendRequest r = requestList[i];
            boolean sameCouple = (r.getFromPlayerID().equals(fromID) && r.getToPlayerID().equals(toID))
                    || (r.getFromPlayerID().equals(toID) && r.getToPlayerID().equals(fromID));
            if (sameCouple && (r.getStatus() == RequestStatus.PENDING
                    || r.getStatus() == RequestStatus.ACCEPTED)) {
                System.out.println("Request already sent or already friends.");
                return null;
            }
        }

        Player target = findByID(toID);
        if (target == null) {
            System.out.println("Player not found.");
            return null;
        }

        String reqID = "REQ" + (requestCount + 1);
        FriendRequest req = new FriendRequest(reqID, fromID, toID, getTodayString());
        System.out.println("Friend request sent to " + target.getFullName() + "!");
        return req;
    }

    // ── Check if two players are friends ─────────────────────────────────────
    public boolean areFriends(String playerID1, String playerID2,
            FriendRequest[] requestList, int requestCount) {
        for (int i = 0; i < requestCount; i++) {
            FriendRequest r = requestList[i];
            boolean sameCouple = (r.getFromPlayerID().equals(playerID1)
                    && r.getToPlayerID().equals(playerID2))
                    || (r.getFromPlayerID().equals(playerID2)
                    && r.getToPlayerID().equals(playerID1));
            if (sameCouple && r.getStatus() == RequestStatus.ACCEPTED) {
                return true;
            }
        }
        return false;
    }

    // ── Today string helper (no imports) ──────────────────────────────────────
    private String getTodayString() {
        long millis = System.currentTimeMillis();
        long totalDays = millis / (1000L * 60 * 60 * 24);
        int[] dim = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int year = 1970;
        long remaining = totalDays;

        while (true) {
            int diy = (year % 4 == 0) ? 366 : 365;
            if (remaining < diy) {
                break;
            }
            remaining -= diy;
            year++;
        }

        int month = 0;
        while (month < 12) {
            int d = dim[month];
            if (month == 1 && year % 4 == 0) {
                d = 29;
            }
            if (remaining < d) {
                break;
            }
            remaining -= d;
            month++;
        }

        int day = (int) remaining + 1;
        return year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
    }
}
