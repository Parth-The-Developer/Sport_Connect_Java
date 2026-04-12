package com.sportconnect.service;

import com.sportconnect.model.Player;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerService {

    private final Map<Long, Player> playerRepo = new HashMap<>();
    private Long idCounter = 1L;

    // ── Create ────────────────────────────────────────────────────────────────

    public Player addPlayer(Player player) {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null");
        if (player.getName() == null || player.getName().trim().isEmpty())
            throw new IllegalArgumentException("Player name cannot be empty");
        if (player.getEmail() == null || player.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Player email cannot be empty");
        if (playerExistsByEmail(player.getEmail()))
            throw new IllegalArgumentException("Email already registered: " + player.getEmail());

        player.setPlayerId(idCounter++);
        player.setCreatedAt(LocalDateTime.now());
        player.setUpdatedAt(LocalDateTime.now());
        playerRepo.put(player.getPlayerId(), player);
        return player;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<Player> getAllPlayers() {
        return new ArrayList<>(playerRepo.values());
    }

    public List<Player> getAllActivePlayers() {
        return playerRepo.values().stream()
                .filter(Player::isActive)
                .collect(Collectors.toList());
    }

    public Player getPlayerById(Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("Invalid player ID");
        Player p = playerRepo.get(id);
        if (p == null)
            throw new NoSuchElementException("Player not found: ID " + id);
        return p;
    }

    public Player getPlayerByEmail(String email) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be empty");
        return playerRepo.values().stream()
                .filter(p -> email.equalsIgnoreCase(p.getEmail()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Player not found: " + email));
    }

    public List<Player> getPlayersBySport(String sport) {
        if (sport == null || sport.trim().isEmpty())
            throw new IllegalArgumentException("Sport cannot be empty");
        return playerRepo.values().stream()
                .filter(p -> sport.equalsIgnoreCase(p.getSport()) && p.isActive())
                .collect(Collectors.toList());
    }

    public List<Player> getPlayersBySkillLevel(String skillLevel) {
        if (skillLevel == null || skillLevel.trim().isEmpty())
            throw new IllegalArgumentException("Skill level cannot be empty");
        return playerRepo.values().stream()
                .filter(p -> skillLevel.equalsIgnoreCase(p.getSkill_level()) && p.isActive())
                .collect(Collectors.toList());
    }

    public List<Player> getPlayersByCity(String city) {
        if (city == null || city.trim().isEmpty())
            throw new IllegalArgumentException("City cannot be empty");
        return playerRepo.values().stream()
                .filter(p -> city.equalsIgnoreCase(p.getCity()) && p.isActive())
                .collect(Collectors.toList());
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public Player updatePlayer(Long id, Player updated) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("Invalid player ID");
        if (updated == null)
            throw new IllegalArgumentException("Updated player data cannot be null");

        Player existing = playerRepo.get(id);
        if (existing == null)
            throw new NoSuchElementException("Player not found: ID " + id);

        if (isSet(updated.getName()))        existing.setName(updated.getName());
        if (isSet(updated.getEmail()))       existing.setEmail(updated.getEmail());
        if (isSet(updated.getPhone()))       existing.setPhone(updated.getPhone());
        if (isSet(updated.getSport()))       existing.setSport(updated.getSport());
        if (updated.getAge() > 0)            existing.setAge(updated.getAge());
        if (isSet(updated.getPosition()))    existing.setPosition(updated.getPosition());
        if (updated.getExperience() >= 0)    existing.setExperience(updated.getExperience());
        if (isSet(updated.getSkill_level())) existing.setSkill_level(updated.getSkill_level());
        if (isSet(updated.getCity()))        existing.setCity(updated.getCity());
        if (isSet(updated.getState()))       existing.setState(updated.getState());
        if (isSet(updated.getCountry()))     existing.setCountry(updated.getCountry());
        if (isSet(updated.getBio()))         existing.setBio(updated.getBio());
        if (isSet(updated.getAvailability())) existing.setAvailability(updated.getAvailability());

        existing.setUpdatedAt(LocalDateTime.now());
        return existing;
    }

    // ── Delete / Activate ─────────────────────────────────────────────────────

    public void deactivatePlayer(Long id) {
        Player p = getPlayerById(id);
        p.setActive(false);
        p.setUpdatedAt(LocalDateTime.now());
    }

    public void activatePlayer(Long id) {
        Player p = getPlayerById(id);
        p.setActive(true);
        p.setUpdatedAt(LocalDateTime.now());
    }

    public void deletePlayer(Long id) {
        if (!playerRepo.containsKey(id))
            throw new NoSuchElementException("Player not found: ID " + id);
        playerRepo.remove(id);
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public int getTotalPlayers()       { return playerRepo.size(); }
    public int getTotalActivePlayers() {
        return (int) playerRepo.values().stream().filter(Player::isActive).count();
    }

    public boolean playerExistsByEmail(String email) {
        return playerRepo.values().stream()
                .anyMatch(p -> email.equalsIgnoreCase(p.getEmail()));
    }

    public void clearAllPlayers() {
        playerRepo.clear();
        idCounter = 1L;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean isSet(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
