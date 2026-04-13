
public class Rating {

    private String ratingID;
    private String raterID;
    private String ratedID;
    private String sessionID;
    private int stars;
    private String comment;

    public Rating(String ratingID, String raterID, String ratedID,
            String sessionID, int stars, String comment) {
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
        return false;
    }

    public void submit() {
    }

    @Override
    public String toString() {
        return "";
    }
}
