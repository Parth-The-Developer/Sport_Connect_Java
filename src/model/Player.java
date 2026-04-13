
public class Player {

    // ── Enum (lives here) ─────────────────────────────────────────────────────
    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    protected String playerID;
    protected String username;
    protected String password;
    protected String fullName;
    protected String sport;
    protected SkillLevel skillLevel;
    protected String city;
    protected String availability;
    protected double averageRating;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Player(String playerID, String username, String password,
            String fullName, String sport, SkillLevel skillLevel,
            String city, String availability) {
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String getPlayerID() {
        return playerID;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSport() {
        return sport;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public String getCity() {
        return city;
    }

    public String getAvailability() {
        return availability;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double r) {
    }

    // ── Methods ───────────────────────────────────────────────────────────────
    public void updateProfile(String newCity, String newAvailability) {
    }

    @Override
    public String toString() {
        return "";
    }
}
