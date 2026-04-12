package model;

import enums.SkillLevel;

public class Player {

    // ── Encapsulation: private fields ─────────────────────────────────────────
    private String playerID;
    private String username;
    private String password;
    private String fullName;
    private String sport;
    private SkillLevel skillLevel;
    private String city;
    private String availability;
    private double averageRating;
    private int ratingCount;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Player(String playerID, String username, String password,
            String fullName, String sport, SkillLevel skillLevel,
            String city, String availability) {
        this.playerID = playerID;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.sport = sport;
        this.skillLevel = skillLevel;
        this.city = city;
        this.availability = availability;
        this.averageRating = 0.0;
        this.ratingCount = 0;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
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

    public int getRatingCount() {
        return ratingCount;
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setCity(String city) {
        this.city = city;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    // ── Update profile ────────────────────────────────────────────────────────
    public void updateProfile(String newCity, String newAvailability) {
        if (newCity != null && !newCity.isEmpty()) {
            this.city = newCity;
        }
        if (newAvailability != null && !newAvailability.isEmpty()) {
            this.availability = newAvailability;
        }
        System.out.println("Profile updated! City: " + this.city
                + " | Availability: " + this.availability);
    }

    // ── Add a new rating and recalculate average ──────────────────────────────
    public void addRating(int stars) {
        this.averageRating = ((this.averageRating * this.ratingCount) + stars)
                / (this.ratingCount + 1);
        this.ratingCount++;
    }

    // ── Abstraction: role contract overridden by Admin ────────────────────────
    public String getRole() {
        return "PLAYER";
    }

    // ── toString ──────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return fullName + " | " + sport + " | " + skillLevel
                + " | " + city + " | " + availability
                + " | Rating: " + String.format("%.1f", averageRating);
    }
}
