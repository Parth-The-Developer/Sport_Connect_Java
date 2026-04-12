package model;

public class SportCategory {

    private String categoryID;
    private String name;

    public SportCategory(String categoryID, String name) {
        this.categoryID = categoryID;
        this.name = name;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return categoryID + ": " + name;
    }
}
