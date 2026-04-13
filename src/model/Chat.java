package model;
import java.util.ArrayList;

public class Chat {

    private String chatID;
    private String player1ID;
    private String player2ID;
    private ArrayList<String> messageList;

    public Chat(String chatID, String player1ID, String player2ID) {
    }

    public String getChatID() {
        return chatID;
    }

    public String getPlayer1ID() {
        return player1ID;
    }

    public String getPlayer2ID() {
        return player2ID;
    }

    public ArrayList<String> getHistory() {
        return messageList;
    }

    public void sendMessage(String senderID, String message) {
    }

    public boolean involves(String playerID) {
        return false;
    }
}
