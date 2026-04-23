package service;

import model.Chat;
import model.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChatService {

    private static final Path STORAGE_FILE = Paths.get("data", "chats.txt");
    private final List<Chat> chatRepo = new ArrayList<>();

    public ChatService() {
        loadFromFile();
    }

    public void sendMessage(Player fromPlayer, Player toPlayer, String message) {
        if (fromPlayer == null || toPlayer == null) {
            throw new IllegalArgumentException("Both players are required");
        }
        if (fromPlayer.getPlayerId() == null || toPlayer.getPlayerId() == null) {
            throw new IllegalArgumentException("Player ID missing");
        }
        if (fromPlayer.getPlayerId().equals(toPlayer.getPlayerId())) {
            throw new IllegalArgumentException("Cannot chat with yourself");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        String fromId = String.valueOf(fromPlayer.getPlayerId());
        String toId = String.valueOf(toPlayer.getPlayerId());

        Chat chat = getOrCreateChat(fromId, toId);
        chat.sendMessage(fromId, message);
        saveToFile();
    }

    public List<String> getConversation(Long playerAId, Long playerBId) {
        String a = String.valueOf(playerAId);
        String b = String.valueOf(playerBId);
        Optional<Chat> chat = findChatBetween(a, b);
        return chat.map(Chat::getHistory).orElseGet(ArrayList::new);
    }

    public int countMessagesFromTo(Long fromPlayerId, Long toPlayerId) {
        if (fromPlayerId == null || toPlayerId == null) return 0;
        String from = String.valueOf(fromPlayerId);
        String to = String.valueOf(toPlayerId);
        Optional<Chat> chat = findChatBetween(from, to);
        if (chat.isEmpty()) return 0;

        int count = 0;
        for (String msg : chat.get().getHistory()) {
            if (msg.startsWith(from + ": ")) {
                count++;
            }
        }
        return count;
    }

    public Map<Long, Integer> countIncomingMessagesBySender(Long playerId) {
        Map<Long, Integer> counts = new HashMap<>();
        if (playerId == null) return counts;
        String me = String.valueOf(playerId);

        for (Chat chat : chatRepo) {
            if (!chat.involves(me)) continue;
            String other = chat.getPlayer1ID().equals(me) ? chat.getPlayer2ID() : chat.getPlayer1ID();
            int msgCount = 0;
            for (String message : chat.getHistory()) {
                if (message.startsWith(other + ": ")) {
                    msgCount++;
                }
            }
            if (msgCount > 0) {
                try {
                    counts.put(Long.parseLong(other), msgCount);
                } catch (NumberFormatException ignored) {}
            }
        }
        return counts;
    }

    private Chat getOrCreateChat(String playerA, String playerB) {
        Optional<Chat> existing = findChatBetween(playerA, playerB);
        if (existing.isPresent()) return existing.get();

        Chat chat = new Chat(nextChatId(), playerA, playerB);
        chatRepo.add(chat);
        return chat;
    }

    private Optional<Chat> findChatBetween(String playerA, String playerB) {
        return chatRepo.stream().filter(c ->
            (Objects.equals(c.getPlayer1ID(), playerA) && Objects.equals(c.getPlayer2ID(), playerB)) ||
            (Objects.equals(c.getPlayer1ID(), playerB) && Objects.equals(c.getPlayer2ID(), playerA))
        ).findFirst();
    }

    private String nextChatId() {
        return "CH-" + (chatRepo.size() + 1);
    }

    private void loadFromFile() {
        try {
            ensureStorageExists();
            List<String> lines = Files.readAllLines(STORAGE_FILE);
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length != 4) continue;
                Chat chat = new Chat(parts[0], parts[1], parts[2]);
                if (!parts[3].isBlank()) {
                    String[] messages = parts[3].split(";;", -1);
                    for (String m : messages) {
                        String decoded = decode(m);
                        int idx = decoded.indexOf(": ");
                        if (idx > 0) {
                            String sender = decoded.substring(0, idx);
                            String msg = decoded.substring(idx + 2);
                            chat.sendMessage(sender, msg);
                        }
                    }
                }
                chatRepo.add(chat);
            }
        } catch (Exception ignored) {
            chatRepo.clear();
        }
    }

    private void saveToFile() {
        try {
            ensureStorageExists();
            List<String> lines = chatRepo.stream()
                .map(c -> String.join("|",
                    c.getChatID(),
                    c.getPlayer1ID(),
                    c.getPlayer2ID(),
                    c.getHistory().stream().map(this::encode).collect(Collectors.joining(";;"))
                ))
                .collect(Collectors.toList());
            Files.write(STORAGE_FILE, lines);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chats", e);
        }
    }

    private void ensureStorageExists() throws IOException {
        Path parent = STORAGE_FILE.getParent();
        if (parent != null) Files.createDirectories(parent);
        if (!Files.exists(STORAGE_FILE)) Files.createFile(STORAGE_FILE);
    }

    private String encode(String s) {
        return s.replace("\\", "\\\\").replace("|", "\\p").replace(";;", "\\s");
    }

    private String decode(String s) {
        return s.replace("\\s", ";;").replace("\\p", "|").replace("\\\\", "\\");
    }
}
