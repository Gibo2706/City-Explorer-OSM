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

    public Place(String name, String description, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
