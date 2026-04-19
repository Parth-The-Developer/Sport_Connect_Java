package service;


import model.Admin;
import model.Player;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class AuthService {

    private final Map<String, Admin>  adminRepo  = new HashMap<>();
    private final Map<String, Player> playerRepo = new HashMap<>();
    private final Map<String, String> tokenRepo  = new HashMap<>();
    private Long adminIdCounter = 1L;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    // ── Admin registration ────────────────────────────────────────────────────

    public Admin registerAdmin(String username, String password,
                               String email, String fullName) {
        validateCredentials(username, password, email);
        if (adminRepo.containsKey(username.toLowerCase()))
            throw new IllegalArgumentException("Username already exists");
        if (adminRepo.values().stream()
                .anyMatch(a -> a.getEmail().equalsIgnoreCase(email)))
            throw new IllegalArgumentException("Email already registered");

        Admin admin = new Admin(adminIdCounter++, username, password, email, fullName);
        adminRepo.put(username.toLowerCase(), admin);
        return admin;
    }

    public Admin registerSuperAdmin(String username, String password,
                                    String email, String fullName) {
        validateCredentials(username, password, email);
        if (adminRepo.containsKey(username.toLowerCase()))
            throw new IllegalArgumentException("Username already exists");

        Admin admin = new Admin(adminIdCounter++, username, password, email, fullName, 2);
        adminRepo.put(username.toLowerCase(), admin);
        return admin;
    }

    // ── Admin login ───────────────────────────────────────────────────────────

    public String adminLogin(String username, String password) {
        if (!isSet(username) || !isSet(password))
            throw new IllegalArgumentException("Username and password cannot be empty");

        Admin admin = adminRepo.get(username.toLowerCase());
        if (admin == null || !admin.getPasswordHash().equals(password))
            throw new IllegalArgumentException("Invalid username or password");
        if (!admin.isActive())
            throw new IllegalArgumentException("Admin account is deactivated");

        admin.updateLastLogin();
        String token = generateToken(username, "ADMIN");
        tokenRepo.put(token, username);
        return token;
    }

    // ── Player registration ───────────────────────────────────────────────────

    public Player registerPlayer(String email, String name,
                                 String phone, String sport) {
        if (!isSet(email) || !isValidEmail(email))
            throw new IllegalArgumentException("Invalid email format");
        if (!isSet(name))
            throw new IllegalArgumentException("Name cannot be empty");
        if (playerRepo.containsKey(email.toLowerCase()))
            throw new IllegalArgumentException("Email already registered");

        Player player = new Player(name, email, phone, sport, 0);
        playerRepo.put(email.toLowerCase(), player);
        return player;
    }

    // ── Player login ──────────────────────────────────────────────────────────

    public String playerLogin(String email, String password) {
        if (!isSet(email) || !isSet(password))
            throw new IllegalArgumentException("Email and password cannot be empty");

        Player player = playerRepo.get(email.toLowerCase());
        if (player == null || !password.equals(player.getPasswordHash()))
            throw new IllegalArgumentException("Invalid email or password");
        if (!player.isActive())
            throw new IllegalArgumentException("Player account is deactivated");

        String token = generateToken(email, "PLAYER");
        tokenRepo.put(token, email);
        return token;
    }

    // ── Token management ──────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        return token != null && tokenRepo.containsKey(token);
    }

    public void logout(String token) {
        if (token != null) tokenRepo.remove(token);
    }

    // ── Lookups ───────────────────────────────────────────────────────────────

    public Admin getAdminByUsername(String username) {
        if (!isSet(username))
            throw new IllegalArgumentException("Username cannot be empty");
        Admin a = adminRepo.get(username.toLowerCase());
        if (a == null) throw new NoSuchElementException("Admin not found: " + username);
        return a;
    }

    public Player getPlayerByEmail(String email) {
        if (!isSet(email))
            throw new IllegalArgumentException("Email cannot be empty");
        Player p = playerRepo.get(email.toLowerCase());
        if (p == null) throw new NoSuchElementException("Player not found: " + email);
        return p;
    }

    public List<Admin>  getAllAdmins()  { return new ArrayList<>(adminRepo.values()); }
    public List<Player> getAllPlayers() { return new ArrayList<>(playerRepo.values()); }

    public boolean adminExists(String username) {
        return adminRepo.containsKey(username.toLowerCase());
    }

    public boolean playerExists(String email) {
        return playerRepo.containsKey(email.toLowerCase());
    }

    /** Restores a player from disk into the login map (skips if email already present). */
    public void restoreRegisteredPlayer(Player player) {
        if (player == null || !isSet(player.getEmail())) return;
        String key = player.getEmail().toLowerCase();
        if (playerRepo.containsKey(key)) return;
        playerRepo.put(key, player);
    }

    // ── Password change ───────────────────────────────────────────────────────

    public void updateAdminPassword(String username, String oldPwd, String newPwd) {
        if (!isSet(newPwd) || newPwd.length() < MIN_PASSWORD_LENGTH)
            throw new IllegalArgumentException(
                "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        Admin admin = getAdminByUsername(username);
        if (!admin.getPasswordHash().equals(oldPwd))
            throw new IllegalArgumentException("Old password is incorrect");
        admin.setPasswordHash(newPwd);
        admin.setUpdatedAt(LocalDateTime.now());
    }

    // ── Admin activation ──────────────────────────────────────────────────────

    public void deactivateAdmin(String username) {
        Admin a = getAdminByUsername(username);
        a.setActive(false);
        a.setUpdatedAt(LocalDateTime.now());
    }

    public void activateAdmin(String username) {
        Admin a = getAdminByUsername(username);
        a.setActive(true);
        a.setUpdatedAt(LocalDateTime.now());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateCredentials(String username, String password, String email) {
        if (!isSet(username))
            throw new IllegalArgumentException("Username cannot be empty");
        if (!isSet(password))
            throw new IllegalArgumentException("Password cannot be empty");
        if (password.length() < MIN_PASSWORD_LENGTH)
            throw new IllegalArgumentException(
                "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        if (!isSet(email) || !isValidEmail(email))
            throw new IllegalArgumentException("Invalid email format");
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isSet(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String generateToken(String identifier, String type) {
        return type + "_" + UUID.randomUUID() + "_" + System.currentTimeMillis();
    }

    public void clearAllData() {
        adminRepo.clear();
        playerRepo.clear();
        tokenRepo.clear();
        adminIdCounter = 1L;
    }
}
