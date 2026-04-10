
public class Admin extends Player {

    // ── Fields ────────────────────────────────────────────────────────────────
    private int adminLevel;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Admin(String playerID, String username, String password,
            String fullName, String sport, Player.SkillLevel skillLevel,
            String city, String availability, int adminLevel) {
        super(playerID, username, password, fullName, sport, skillLevel, city, availability);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int getAdminLevel() {
        return adminLevel;
    }

    // ── Methods ───────────────────────────────────────────────────────────────
    public void cancelSession(String id) {
    }

    public void viewAllUsers() {
    }

    public void processRefund(String id) {
    }

    @Override
    public String toString() {
        return "";
    }
}
