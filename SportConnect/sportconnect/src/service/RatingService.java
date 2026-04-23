package service;

import model.Player;
import model.Rating;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RatingService {

    private List<Rating> ratings = new ArrayList<>();
    private int idCounter = 1;
    private static final String CSV_FILE = "data/ratings.csv";

    public RatingService() {
        loadFromCSV();
    }

    // Reads and validates star input from user
    public float readStars(Scanner sc) {
        while (true) {
            System.out.print("Stars (1-5, one decimal allowed e.g. 4.5): ");
            String input = sc.nextLine().trim();

            int dotCount = 0;
            for (char c : input.toCharArray()) {
                if (c == '.') dotCount++;
            }

            if (dotCount > 1) {
                System.out.println("[Error] Only one decimal point is accepted. Please enter the star value again.");
                continue;
            }

            try {
                float stars = Float.parseFloat(input);
                if (stars < 1.0f || stars > 5.0f) {
                    System.out.println("[Error] Stars must be between 1 and 5. Please enter the star value again.");
                    continue;
                }
                return stars;
            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid input. Please enter a number between 1 and 5.");
            }
        }
    }

    public void addRating(Player rater, Player rated, String sessionID, float stars, String comment) {

        if (rater == null || rated == null) {
            System.out.println("Error: Players cannot be null.");
            return;
        }

        if (rater.getPlayerId().equals(rated.getPlayerId())) {
            System.out.println("Error: A player cannot rate themselves.");
            return;
        }

        if (stars < 1.0f || stars > 5.0f) {
            System.out.println("Error: Stars must be between 1 and 5.");
            return;
        }

        String raterID = String.valueOf(rater.getPlayerId());
        String ratedID = String.valueOf(rated.getPlayerId());

        Rating existing = null;
        for (Rating r : ratings) {
            if (r.getRaterID().equals(raterID) && r.getRatedID().equals(ratedID)) {
                existing = r;
                break;
            }
        }

        if (existing != null) {
            existing.setStars(stars);
            existing.setComment(comment);
            existing.submit();
            System.out.println("Rating updated for player " + rated.getName());
        } else {
            String ratingID = "RT-" + idCounter;
            idCounter++;

            Rating rating = new Rating(ratingID, raterID, ratedID, sessionID, stars, comment);
            rating.setRaterName(rater.getName());
            rating.setRatedName(rated.getName());
            rating.submit();
            ratings.add(rating);
        }

        rated.setRating(getAverageRatingById(rated.getPlayerId()));
        saveToCSV();
    }

    // Get all ratings for a specific player by ID
    public List<Rating> getRatingsForPlayer(Long playerId) {
        List<Rating> result = new ArrayList<>();
        for (Rating r : ratings) {
            if (r.getRatedID().equals(String.valueOf(playerId))) {
                result.add(r);
            }
        }
        return result;
    }

    // Calculate average by player ID
    public double getAverageRatingById(Long playerId) {
        if (playerId == null) return 0.0;
        String id = String.valueOf(playerId);

        float total = 0;
        int count = 0;
        for (Rating r : ratings) {
            if (r.getRatedID().equals(id)) {
                total += r.getStars();
                count++;
            }
        }
        return count == 0 ? 0.0 : (double) total / count;
    }

    // Print all ratings to console
    public void printAllRatings() {
        if (ratings.isEmpty()) {
            System.out.println("No ratings yet.");
            return;
        }
        for (Rating r : ratings) {
            System.out.println(r);
        }
    }

    // Load from CSV on startup
    private void loadFromCSV() {
        try {
            new java.io.File("data").mkdirs();
            java.io.File file = new java.io.File(CSV_FILE);
            if (!file.exists()) return;

            BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE));
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.isBlank()) continue;

                try {
                    String[] parts = line.split(",", 5);
                    if (parts.length < 5) continue;

                    String ratedName = parts[0].trim();
                    String ratedID   = parts[1].trim();
                    float  stars     = Float.parseFloat(parts[2].trim());
                    String comment   = parts[3].replace("\"", "").trim();
                    String raterName = parts[4].trim();

                    String ratingID = "RT-" + idCounter;
                    idCounter++;

                    Rating rating = new Rating(ratingID, "0", ratedID, "LOADED", stars, comment);
                    rating.setRatedName(ratedName);
                    rating.setRaterName(raterName);
                    ratings.add(rating);

                } catch (Exception e) {
                    // skip malformed rows
                }
            }

            reader.close();

        } catch (IOException e) {
            System.out.println("Could not load ratings: " + e.getMessage());
        }
    }

    // Save to CSV
    private void saveToCSV() {
        try {
            new java.io.File("data").mkdirs();
            FileWriter writer = new FileWriter(CSV_FILE);

            writer.write("ratedPlayerName,ratedID,stars,comment,ratedBy\n");

            for (Rating r : ratings) {
                writer.write(
                    r.getRatedName()                            + "," +
                    r.getRatedID()                              + "," +
                    r.getStars()                                + "," +
                    "\"" + r.getComment().replace("\"", "") + "\"" + "," +
                    r.getRaterName()                            + "\n"
                );
            }

            writer.close();
            System.out.println("Ratings saved to " + CSV_FILE);

        } catch (IOException e) {
            System.out.println("Could not save ratings: " + e.getMessage());
        }
    }
}