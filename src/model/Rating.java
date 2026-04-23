package model;
public class Rating {

    private String ratingID;
    private String raterID;
    private String ratedID;
    private String sessionID;
    private float stars;
    private String comment;
    private String raterName;
    private String ratedName;

    public Rating(String ratingID, String raterID, String ratedID,
            String sessionID, float stars, String comment) {
        this.ratingID  = ratingID;
        this.raterID   = raterID;
        this.ratedID   = ratedID;
        this.sessionID = sessionID;
        this.stars     = stars;
        this.comment   = comment;
    }

    public String getRatingID()  { return ratingID;  }
    public String getRaterID()   { return raterID;   }
    public String getRatedID()   { return ratedID;   }
    public String getSessionID() { return sessionID; }
    public float  getStars()     { return stars;     }
    public String getComment()   { return comment;   }

    public void   setStars(float v)      { this.stars   = v;   }
    public void   setComment(String v)   { this.comment = v;   }
    public void   setRaterName(String v) { this.raterName = v; }
    public void   setRatedName(String v) { this.ratedName = v; }
    public String getRaterName()         { return raterName;   }
    public String getRatedName()         { return ratedName;   }

    public boolean validate() {
        if (raterID == null || ratedID == null || sessionID == null) return false;
        if (raterID.equals(ratedID)) return false;
        if (stars < 1.0f || stars > 5.0f) return false;
        return true;
    }

    public void submit() {
        if (!validate()) {
            System.out.println("Rating is not valid.");
            return;
        }
        System.out.println("Rating submitted: " + stars + " star(s) for player " + ratedID);
    }

    @Override
    public String toString() {
        return "RatingID: " + ratingID + ", From: " + raterName +
               ", To: " + ratedName + ", Session: " + sessionID +
               ", Stars: " + stars + ", Comment: " + comment;
    }
}