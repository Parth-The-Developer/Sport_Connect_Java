package model;

import java.util.ArrayList;

public class Team {

    private String teamID;
    private String teamName;
    private String sport;
    private String captainID;
    private ArrayList<String> memberIDs;

    public static final int MAX_MEMBERS = 11;
    

    public Team(String teamID, String teamName, String sport, String captainID) {
        this.teamID = teamID;
        this.teamName = teamName;
        this.sport = sport;
        this.captainID = captainID;
        this.memberIDs = new ArrayList<>();

        if (captainID != null && !captainID.trim().isEmpty()) {
            memberIDs.add(captainID);
        }
    }

    public String getTeamID() { return teamID; }
    public String getTeamName() { return teamName; }
    public String getSport() { return sport; }
    public String getCaptainID() { return captainID; }
    public ArrayList<String> getMemberIDs() { return memberIDs; }
    

    public void setCaptain(String newCaptainID) {
    this.captainID = newCaptainID;
    }

    public boolean addMember(String playerID) {
        if (playerID == null || playerID.isEmpty()) return false;
        if (memberIDs.contains(playerID)) return false;

        memberIDs.add(playerID);
        return true;
    }

    public boolean removeMember(String playerID) {
        if (playerID == null || playerID.isEmpty()) return false;
        if (playerID.equals(captainID)) return false;

        return memberIDs.remove(playerID);
    }

    @Override
    public String toString() {
        return "\nTEAM [" + teamID + "]"
                + "\nName: " + teamName
                + "\nSport: " + sport
                + "\nCaptain: " + captainID
                + "\nMembers: " + memberIDs;
    }
}