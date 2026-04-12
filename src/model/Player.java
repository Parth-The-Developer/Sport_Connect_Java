package com.sportconnect.model;

import java.time.LocalDateTime;

public class Player {

    private Long          playerId;
    private String        name;
    private String        email;
    private String        phone;
    private String        passwordHash;
    private String        sport;
    private String        skill_level;   // "BEGINNER" | "INTERMEDIATE" | "ADVANCED"
    private String        position;
    private int           experience;
    private double        rating;
    private String        availability;
    private int           age;
    private String        bio;
    private String        city;
    private String        state;
    private String        country;
    private boolean       active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Player() {
        this.active    = true;
        this.rating    = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Used by AuthService.registerPlayer(name, email, phone, sport, experience) */
    public Player(String name, String email, String phone, String sport, int experience) {
        this();
        this.name       = name;
        this.email      = email;
        this.phone      = phone;
        this.sport      = sport;
        this.experience = experience;
    }

    public Player(Long playerId, String name, String email, String phone,
                  String sport, String skill_level, String position,
                  int experience, int age, String city, String state, String country) {
        this();
        this.playerId    = playerId;
        this.name        = name;
        this.email       = email;
        this.phone       = phone;
        this.sport       = sport;
        this.skill_level = skill_level;
        this.position    = position;
        this.experience  = experience;
        this.age         = age;
        this.city        = city;
        this.state       = state;
        this.country     = country;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long   getPlayerId()               { return playerId; }
    public void   setPlayerId(Long v)         { this.playerId = v; }

    public String getName()                   { return name; }
    public void   setName(String v)           { this.name = v; }

    public String getEmail()                  { return email; }
    public void   setEmail(String v)          { this.email = v; }

    public String getPhone()                  { return phone; }
    public void   setPhone(String v)          { this.phone = v; }

    public String getPasswordHash()           { return passwordHash; }
    public void   setPasswordHash(String v)   { this.passwordHash = v; }

    public String getSport()                  { return sport; }
    public void   setSport(String v)          { this.sport = v; }

    public String getSkill_level()            { return skill_level; }
    public void   setSkill_level(String v)    { this.skill_level = v; }

    public String getPosition()               { return position; }
    public void   setPosition(String v)       { this.position = v; }

    public int    getExperience()             { return experience; }
    public void   setExperience(int v)        { this.experience = v; }

    public double getRating()                 { return rating; }
    public void   setRating(double v)         { this.rating = v; }

    public String getAvailability()           { return availability; }
    public void   setAvailability(String v)   { this.availability = v; }

    public int    getAge()                    { return age; }
    public void   setAge(int v)               { this.age = v; }

    public String getBio()                    { return bio; }
    public void   setBio(String v)            { this.bio = v; }

    public String getCity()                   { return city; }
    public void   setCity(String v)           { this.city = v; }

    public String getState()                  { return state; }
    public void   setState(String v)          { this.state = v; }

    public String getCountry()                { return country; }
    public void   setCountry(String v)        { this.country = v; }

    public boolean       isActive()           { return active; }
    public void          setActive(boolean v) { this.active = v; }

    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()       { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }

    // ── Utility ───────────────────────────────────────────────────────────────

    public String getDisplayName() {
        return name != null ? name : "Player#" + playerId;
    }

    @Override
    public String toString() {
        return String.format(
            "Player { id=%d, name='%s', email='%s', sport='%s', skill='%s'," +
            " city='%s', rating=%.1f, active=%b }",
            playerId, name, email, sport, skill_level, city, rating, active);
    }
}
