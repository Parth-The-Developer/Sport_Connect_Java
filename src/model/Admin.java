package com.sportconnect.model;

import java.time.LocalDateTime;

public class Admin {

    private Long          adminId;
    private String        username;
    private String        password;
    private String        email;
    private String        fullName;
    private String        role;          // "SUPER_ADMIN" | "ADMIN" | "MODERATOR"
    private boolean       active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Admin() {
        this.active    = true;
        this.role      = "ADMIN";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Admin(String username, String password, String email) {
        this();
        this.username = username;
        this.password = password;
        this.email    = email;
    }

    public Admin(Long adminId, String username, String password,
                 String email, String fullName) {
        this();
        this.adminId  = adminId;
        this.username = username;
        this.password = password;
        this.email    = email;
        this.fullName = fullName;
    }

    public Admin(Long adminId, String username, String password,
                 String email, String fullName, String role) {
        this(adminId, username, password, email, fullName);
        this.role = role;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long   getAdminId()               { return adminId; }
    public void   setAdminId(Long v)         { this.adminId = v; }

    public String getUsername()              { return username; }
    public void   setUsername(String v)      { this.username = v; }

    public String getPassword()              { return password; }
    public void   setPassword(String v)      { this.password = v; }

    public String getEmail()                 { return email; }
    public void   setEmail(String v)         { this.email = v; }

    public String getFullName()              { return fullName; }
    public void   setFullName(String v)      { this.fullName = v; }

    public String getRole()                  { return role; }
    public void   setRole(String v)          { this.role = v; }

    public boolean       isActive()          { return active; }
    public void          setActive(boolean v){ this.active = v; }

    public LocalDateTime getCreatedAt()      { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public LocalDateTime getLastLoginAt()    { return lastLoginAt; }
    public void          setLastLoginAt(LocalDateTime v){ this.lastLoginAt = v; }

    public LocalDateTime getUpdatedAt()      { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }

    // ── Business methods ──────────────────────────────────────────────────────

    public boolean hasPermission(String permission) {
        if (!active) return false;
        switch (role.toUpperCase()) {
            case "SUPER_ADMIN": return true;
            case "ADMIN":
                return !permission.equals("DELETE_ADMIN")
                    && !permission.equals("MANAGE_SYSTEM_SETTINGS");
            case "MODERATOR":
                return permission.equals("VIEW_PLAYERS")
                    || permission.equals("VIEW_EVENTS")
                    || permission.equals("MANAGE_EVENTS");
            default: return false;
        }
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equalsIgnoreCase(role);
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format(
            "Admin { id=%d, username='%s', email='%s', role='%s', active=%b }",
            adminId, username, email, role, active);
    }
}
