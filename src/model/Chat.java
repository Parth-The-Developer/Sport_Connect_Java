package model;
import java.util.ArrayList;

public class Chat {

    private String chatID;
    private String player1ID;
    private String player2ID;
    private ArrayList<String> messageList;

    public Chat(String chatID, String player1ID, String player2ID) {
        this.chatID = chatID;
        this.player1ID = player1ID;
        this.player2ID = player2ID;
        this.messageList = new ArrayList<>();
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
        if (senderID == null || senderID.isBlank()) return;
        if (message == null || message.isBlank()) return;
        messageList.add(senderID + ": " + message.trim());
    }

    public boolean involves(String playerID) {
        if (playerID == null) return false;
        return playerID.equals(player1ID) || playerID.equals(player2ID);
    }
}
