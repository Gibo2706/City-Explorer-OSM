package pmf.rma.cityexplorerosm.domain.model;

public class BadgeDomain {
    private final String id;
    private final String title;
    private final String description;
    private final long unlockedAt;

    public BadgeDomain(String id, String title, String description, long unlockedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.unlockedAt = unlockedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getUnlockedAt() {
        return unlockedAt;
    }
}
