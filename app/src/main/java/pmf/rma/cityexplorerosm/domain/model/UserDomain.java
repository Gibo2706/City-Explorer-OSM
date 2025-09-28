package pmf.rma.cityexplorerosm.domain.model;

public class UserDomain {
    private final String id;
    private final String displayName;
    private final int points;

    public String firstName;
    public String lastName;
    public String username;
    public UserDomain(String id, String displayName, int points) {
        this.id = id;
        this.displayName = displayName;
        this.points = points;
    }

    public UserDomain(String id, String displayName, int points, String firstName, String lastName, String username) {
        this.id = id;
        this.displayName = displayName;
        this.points = points;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPoints() {
        return points;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }
}
