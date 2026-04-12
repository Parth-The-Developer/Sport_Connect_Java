package model;

public class Rating {

    private String ratingID;
    private String raterID;
    private String ratedID;
    private String sessionID;
    private int stars;
    private String comment;
    private boolean submitted;

    public Rating(String ratingID, String raterID, String ratedID,
            String sessionID, int stars, String comment) {
        this.ratingID = ratingID;
        this.raterID = raterID;
        this.ratedID = ratedID;
        this.sessionID = sessionID;
        this.stars = stars;
        this.comment = comment;
        this.submitted = false;
    }

    public String getRatingID() {
        return ratingID;
    }

    public String getRaterID() {
        return raterID;
    }

    public String getRatedID() {
        return ratedID;
    }

    public String getSessionID() {
        return sessionID;
    }

    public int getStars() {
        return stars;
    }

    public String getComment() {
        return comment;
    }

    public boolean validate() {
        return stars >= 1 && stars <= 5;
    }

    public void submit() {
        this.submitted = true;
    }

    @Override
    public String toString() {
        return ratingID + " | " + stars + "★ — " + comment;
    }
}
