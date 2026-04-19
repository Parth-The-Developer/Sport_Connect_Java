package service;

import model.Player;
import model.Team;
import java.util.ArrayList;

/**
 * Team operations. Result codes for richer APIs are {@code String} constants (no nested enums)
 * so compilation produces a single {@code TeamService.class} file.
 */
public class TeamService {

    public static final String R_ADD_TEAM_NOT_FOUND = "TEAM_NOT_FOUND";
    public static final String R_ADD_NOT_CAPTAIN = "ACTOR_NOT_CAPTAIN";
    public static final String R_ADD_PLAYER_NOT_FOUND = "PLAYER_NOT_FOUND";
    public static final String R_ADD_ALREADY_MEMBER = "ALREADY_MEMBER";
    public static final String R_ADD_TEAM_FULL = "TEAM_FULL";
    public static final String R_ADD_SPORT_MISMATCH = "SPORT_MISMATCH";
    public static final String R_ADD_SUCCESS = "SUCCESS";

    public static final String R_REMOVE_TEAM_NOT_FOUND = "TEAM_NOT_FOUND";
    public static final String R_REMOVE_NOT_CAPTAIN = "NOT_CAPTAIN";
    public static final String R_REMOVE_CAPTAIN_SELF = "CAPTAIN_CANNOT_REMOVE_SELF";
    public static final String R_REMOVE_NOT_IN_TEAM = "PLAYER_NOT_IN_TEAM";
    public static final String R_REMOVE_SUCCESS = "SUCCESS";

    public static final String R_LEAVE_TEAM_NOT_FOUND = "TEAM_NOT_FOUND";
    public static final String R_LEAVE_NOT_IN_TEAM = "PLAYER_NOT_IN_TEAM";
    public static final String R_LEAVE_SUCCESS = "SUCCESS";

    public static final String R_DELETE_TEAM_NOT_FOUND = "TEAM_NOT_FOUND";
    public static final String R_DELETE_NOT_CAPTAIN = "NOT_CAPTAIN";
    public static final String R_DELETE_SUCCESS = "SUCCESS";

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

    public ArrayList<Team> getTeamsForPlayer(String playerId) {
        ArrayList<Team> result = new ArrayList<>();
        for (Team t : teams) {
            if (t.getMemberIDs().contains(playerId)) {
                result.add(t);
            }
        }
        return result;
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

    public String addPlayerToTeam(String teamId, String actorCaptainId, String targetPlayerId) {
        Team team = getTeamByID(teamId);
        if (team == null) return R_ADD_TEAM_NOT_FOUND;
        if (!team.getCaptainID().equals(actorCaptainId)) return R_ADD_NOT_CAPTAIN;

        Long targetIdLong = parsePlayerId(targetPlayerId);
        if (targetIdLong == null) return R_ADD_PLAYER_NOT_FOUND;
        Player player = playerService.getPlayerById(targetIdLong);
        if (player == null) return R_ADD_PLAYER_NOT_FOUND;
        if (team.getMemberIDs().contains(targetPlayerId)) return R_ADD_ALREADY_MEMBER;
        if (team.getMemberIDs().size() >= Team.MAX_MEMBERS) return R_ADD_TEAM_FULL;
        if (!isSportCompatible(player.getSport(), team.getSport())) return R_ADD_SPORT_MISMATCH;

        team.addMember(targetPlayerId);
        return R_ADD_SUCCESS;
    }

    /** Captain removes another member (cannot remove self). */
    public String removeMemberAsCaptain(String teamId, String captainPlayerId, String targetPlayerId) {
        Team team = getTeamByID(teamId);
        if (team == null)
            return R_REMOVE_TEAM_NOT_FOUND;
        if (!team.getCaptainID().equals(captainPlayerId))
            return R_REMOVE_NOT_CAPTAIN;
        if (captainPlayerId.equals(targetPlayerId))
            return R_REMOVE_CAPTAIN_SELF;
        if (!team.getMemberIDs().contains(targetPlayerId))
            return R_REMOVE_NOT_IN_TEAM;
        return team.removeMember(targetPlayerId) ? R_REMOVE_SUCCESS : R_REMOVE_NOT_IN_TEAM;
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

    public String leaveTeamSafe(String playerId, String teamID) {
        Team team = getTeamByID(teamID);
        if (team == null) return R_LEAVE_TEAM_NOT_FOUND;
        if (!team.getMemberIDs().contains(playerId)) return R_LEAVE_NOT_IN_TEAM;
        return leaveTeam(playerId, teamID) ? R_LEAVE_SUCCESS : R_LEAVE_NOT_IN_TEAM;
    }

    public String deleteTeamAsCaptain(String teamId, String captainPlayerId) {
        Team team = getTeamByID(teamId);
        if (team == null) return R_DELETE_TEAM_NOT_FOUND;
        if (!team.getCaptainID().equals(captainPlayerId)) return R_DELETE_NOT_CAPTAIN;
        return deleteTeam(teamId) ? R_DELETE_SUCCESS : R_DELETE_TEAM_NOT_FOUND;
    }

    private Long parsePlayerId(String text) {
        try {
            return Long.parseLong(text);
        } catch (Exception e) {
            return null;
        }
    }
}
