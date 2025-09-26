package pmf.rma.cityexplorerosm.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "visits")
public class Visit {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public int placeId;
    public long visitedAt;

    public Visit(long id, int placeId, long visitedAt) {
        this.id = id;
        this.placeId = placeId;
        this.visitedAt = visitedAt;
    }
}
