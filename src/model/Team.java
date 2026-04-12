package model;

import java.util.ArrayList;

public class Team {

    private String teamID;
    private String teamName;
    private String sport;
    private String captainID;
    private ArrayList<String> memberIDs;

    public Team(String teamID, String teamName, String sport, String captainID) {
        this.teamID = teamID;
        this.teamName = teamName;
        this.sport = sport;
        this.captainID = captainID;
        this.memberIDs = new ArrayList<>();
        this.memberIDs.add(captainID);
    }

    public String getTeamID() {
        return teamID;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getSport() {
        return sport;
    }

    public String getCaptainID() {
        return captainID;
    }

    public ArrayList<String> getMemberIDs() {
        return memberIDs;
    }

    public void addMember(String playerID) {
        if (!memberIDs.contains(playerID)) {
            memberIDs.add(playerID);
        }
    }

    public void removeMember(String playerID) {
        memberIDs.remove(playerID);
    }

    @Override
    public String toString() {
        return teamName + " (" + sport + ") — captain " + captainID;
    }
}
