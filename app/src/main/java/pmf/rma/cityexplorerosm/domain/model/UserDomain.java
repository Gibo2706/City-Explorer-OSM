package pmf.rma.cityexplorerosm.domain.model;

public class UserDomain {
    private final String id;
    private final String displayName;
    private final int points;

    public UserDomain(String id, String displayName, int points) {
        this.id = id;
        this.displayName = displayName;
        this.points = points;
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
}
