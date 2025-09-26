package pmf.rma.cityexplorerosm.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "badges")
public class Badge {
    @PrimaryKey @NonNull
    public String id;
    public String title;
    public String description;
    public long unlockedAt;

    public Badge(@NonNull String id, String title, String description, long unlockedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.unlockedAt = unlockedAt;
    }
}
