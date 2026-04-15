package service;

import enums.RequestStatus;
import model.FriendRequest;
import model.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class FriendRequestService {

    private static final Path STORAGE_FILE = Paths.get("data", "friend_requests.txt");
    private final List<FriendRequest> requests = new ArrayList<>();
    private final EmailService emailService = new EmailService();

    public FriendRequestService() {
        loadFromFile();
    }

    public FriendRequest sendRequest(Player fromPlayer, Player toPlayer) {
        if (fromPlayer == null || toPlayer == null) {
            throw new IllegalArgumentException("Both players are required");
        }
        if (fromPlayer.getPlayerId() == null || toPlayer.getPlayerId() == null) {
            throw new IllegalArgumentException("Player IDs are missing");
        }

        String fromId = String.valueOf(fromPlayer.getPlayerId());
        String toId = String.valueOf(toPlayer.getPlayerId());

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("You cannot send request to yourself");
        }

        boolean duplicatePending = requests.stream().anyMatch(r ->
            r.getStatus() == RequestStatus.PENDING &&
            ((Objects.equals(r.getFromPlayerID(), fromId) && Objects.equals(r.getToPlayerID(), toId)) ||
             (Objects.equals(r.getFromPlayerID(), toId) && Objects.equals(r.getToPlayerID(), fromId)))
        );
        if (duplicatePending) {
            throw new IllegalArgumentException("A pending request already exists between these players");
        }

        FriendRequest request = new FriendRequest(nextRequestId(), fromId, toId);
        requests.add(request);
        saveToFile();

        // Best-effort email notification: request should still succeed if email fails.
        emailService.sendFriendRequestEmail(
            toPlayer.getEmail(),
            toPlayer.getName(),
            fromPlayer.getName()
        );
        return request;
    }

    public List<FriendRequest> getIncomingRequests(Long playerId) {
        String id = String.valueOf(playerId);
        return requests.stream()
            .filter(r -> id.equals(r.getToPlayerID()) && r.getStatus() == RequestStatus.PENDING)
            .collect(Collectors.toList());
    }

    public List<FriendRequest> getAllRequests() {
        return new ArrayList<>(requests);
    }

    public FriendRequest respondToRequest(String requestId, Long currentPlayerId, boolean accept) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request ID is required");
        }
        if (currentPlayerId == null) {
            throw new IllegalArgumentException("Current player ID is required");
        }

        FriendRequest request = requests.stream()
            .filter(r -> requestId.equalsIgnoreCase(r.getRequestID()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        if (!String.valueOf(currentPlayerId).equals(request.getToPlayerID())) {
            throw new IllegalArgumentException("You can only respond to your own incoming requests");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is already " + request.getStatus());
        }

        if (accept) request.accept();
        else request.decline();

        saveToFile();
        return request;
    }

    public boolean isFriendshipAccepted(Long playerAId, Long playerBId) {
        if (playerAId == null || playerBId == null) return false;
        String a = String.valueOf(playerAId);
        String b = String.valueOf(playerBId);

        return requests.stream().anyMatch(r ->
            r.getStatus() == RequestStatus.ACCEPTED &&
            ((Objects.equals(r.getFromPlayerID(), a) && Objects.equals(r.getToPlayerID(), b)) ||
             (Objects.equals(r.getFromPlayerID(), b) && Objects.equals(r.getToPlayerID(), a)))
        );
    }

    public void sendAcceptanceEmail(Player acceptedByPlayer, Player requesterPlayer) {
        if (acceptedByPlayer == null || requesterPlayer == null) return;
        emailService.sendRequestAcceptedEmail(
            requesterPlayer.getEmail(),
            requesterPlayer.getName(),
            acceptedByPlayer.getName()
        );
    }

    private String nextRequestId() {
        return "FR-" + (requests.size() + 1);
    }

    private void loadFromFile() {
        try {
            ensureStorageExists();
            List<String> lines = Files.readAllLines(STORAGE_FILE);
            for (String line : lines) {
                if (line == null || line.isBlank()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length != 5) continue;
                RequestStatus status = RequestStatus.valueOf(parts[3].toUpperCase(Locale.ROOT));
                LocalDate date = LocalDate.parse(parts[4]);
                requests.add(new FriendRequest(parts[0], parts[1], parts[2], status, date));
            }
        } catch (Exception ignored) {
            requests.clear();
        }
    }

    private void saveToFile() {
        try {
            ensureStorageExists();
            List<String> lines = requests.stream()
                .map(r -> String.join("|",
                    r.getRequestID(),
                    r.getFromPlayerID(),
                    r.getToPlayerID(),
                    r.getStatus().name(),
                    r.getDateSent().toString()))
                .collect(Collectors.toList());
            Files.write(STORAGE_FILE, lines);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save friend requests", e);
        }
    }

    private void ensureStorageExists() throws IOException {
        Path parent = STORAGE_FILE.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(STORAGE_FILE)) {
            Files.createFile(STORAGE_FILE);
        }
    }
}
