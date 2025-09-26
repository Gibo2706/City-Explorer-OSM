package pmf.rma.cityexplorerosm.domain.model;

public class PlaceDomain {
    private final int id;
    private final String name;
    private final String description;
    private final double latitude;
    private final double longitude;
    private final String category;
    private final String imageUrl;
    private final String workingHours;

    public PlaceDomain(int id, String name, String description,
                       double latitude, double longitude,
                       String category, String imageUrl, String workingHours) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.imageUrl = imageUrl;
        this.workingHours = workingHours;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public String getWorkingHours() { return workingHours; }
}
