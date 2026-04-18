package service;

import model.Player;
import model.Team;
import java.util.ArrayList;

public class TeamService {

    private ArrayList<Team> teams = new ArrayList<>();
    private int teamCounter = 1;
    private PlayerService playerService;

    public TeamService(PlayerService playerService) {
    this.playerService = playerService;
    }

    public Team createTeam(String name, String sport, String captainID) {

        String id = "T" + teamCounter++;

        Team team = new Team(id, name, sport, captainID);
        teams.add(team);

        return team;
    }

    /** Creates a team only if the player's sport matches the team sport. */
    public Team createTeamForPlayerSport(String playerSport, String name, String sport, String captainID) {
        if (!isSportCompatible(playerSport, sport))
            return null;
        return createTeam(name, sport, captainID);
    }

    public ArrayList<Team> getAllTeams() {
        return teams;
    }

    public Team getTeamByID(String id) {
        for (Team t : teams) {
            if (t.getTeamID().equalsIgnoreCase(id)) {
                return t;
            }
        }
        return null;
    }

    // ================= JOIN TEAM =================
    public String joinTeam(String playerId, String teamId) {

        Team team = getTeamByID(teamId);
        Player player = playerService.getPlayerById(Long.parseLong(playerId));

        if (team == null) return "TEAM_NOT_FOUND";
        if (player == null) return "PLAYER_NOT_FOUND";

        if (!player.getSport().equalsIgnoreCase(team.getSport())) {
            return "SPORT_MISMATCH";
        }

        if (team.getMemberIDs().contains(playerId)) {
            return "ALREADY_MEMBER";
        }

        if (team.getMemberIDs().size() >= Team.MAX_MEMBERS) {
            return "TEAM_FULL";
        }

        team.addMember(playerId);
        return "SUCCESS";
    }

    public boolean isSportCompatible(String playerSport, String teamSport) {
        return playerSport.equalsIgnoreCase(teamSport);
    }

    // ================= LEAVE TEAM =================
    public boolean leaveTeam(String playerId, String teamID) {

        Team team = getTeamByID(teamID);

        if (team == null) return false;

        String captainId = team.getCaptainID();

        // IF CAPTAIN LEAVES
        if (captainId.equals(playerId)) {

            team.getMemberIDs().remove(playerId);

            if (!team.getMemberIDs().isEmpty()) {
                String newCaptain = team.getMemberIDs().get(0);
                team.setCaptain(newCaptain);
                System.out.println("  Captain left. New captain assigned: " + newCaptain);
            } else {
                System.out.println("  Team is now empty after captain left.");
            }

            return true;
        }

        // NORMAL PLAYER LEAVE
        if (!team.getMemberIDs().contains(playerId)) return false;

        team.getMemberIDs().remove(playerId);
        return true;
    }

    // ================= ADMIN FEATURE =================
    public boolean deleteTeam(String teamID) {
        return teams.removeIf(t -> t.getTeamID().equalsIgnoreCase(teamID));
    }

    public enum RemoveMemberResult {
        TEAM_NOT_FOUND,
        NOT_CAPTAIN,
        CAPTAIN_CANNOT_REMOVE_SELF,
        PLAYER_NOT_IN_TEAM,
        SUCCESS
    }

    /** Captain removes another member (cannot remove self). */
    public RemoveMemberResult removeMemberAsCaptain(String teamId, String captainPlayerId, String targetPlayerId) {
        Team team = getTeamByID(teamId);
        if (team == null)
            return RemoveMemberResult.TEAM_NOT_FOUND;
        if (!team.getCaptainID().equals(captainPlayerId))
            return RemoveMemberResult.NOT_CAPTAIN;
        if (captainPlayerId.equals(targetPlayerId))
            return RemoveMemberResult.CAPTAIN_CANNOT_REMOVE_SELF;
        if (!team.getMemberIDs().contains(targetPlayerId))
            return RemoveMemberResult.PLAYER_NOT_IN_TEAM;
        return team.removeMember(targetPlayerId) ? RemoveMemberResult.SUCCESS : RemoveMemberResult.PLAYER_NOT_IN_TEAM;
    }

    /** Admin removes a player from a team (same rules as {@link Team#removeMember}). */
    public boolean removeMemberAsAdmin(String teamId, String playerId) {
        Team team = getTeamByID(teamId);
        if (team == null)
            return false;
        if (!team.getMemberIDs().contains(playerId))
            return false;
        return team.removeMember(playerId);
    }

    public enum JoinTeamResult { SUCCESS, TEAM_NOT_FOUND, PLAYER_NOT_FOUND, ALREADY_MEMBER, TEAM_FULL, SPORT_MISMATCH
    }
}
