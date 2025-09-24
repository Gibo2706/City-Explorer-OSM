package pmf.rma.cityexplorerosm.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class Favorite {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int placeId;
    public long timestamp;

    public Favorite(int placeId, long timestamp) {
        this.placeId = placeId;
        this.timestamp = timestamp;
    }
}
