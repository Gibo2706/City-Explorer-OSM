package pmf.rma.cityexplorerosm.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "places")
public class Place {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String description;
    public double latitude;
    public double longitude;
    public String category;

    // 👇 nova polja
    public String imageUrl;
    public String workingHours;

    public Place(int id, String name, String description,
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
}
