
import java.util.ArrayList;

public class Team {

    private String teamID;
    private String teamName;
    private String sport;
    private String captainID;
    private ArrayList<String> memberIDs;

    public Team(String teamID, String teamName, String sport, String captainID) {
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
    }

    public void removeMember(String playerID) {
    }

    @Override
    public String toString() {
        return "";
    }
}
