package pmf.rma.cityexplorerosm.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(
        tableName = "badges",
        primaryKeys = {"userId", "id"}
)
public class Badge {
    @NonNull public String userId;
    @NonNull public String id;
    public String title;
    public String description;
    public long unlockedAt;

    public Badge(@NonNull String userId, @NonNull String id, String title, String description, long unlockedAt) {
        this.userId = userId;
        this.id = id;
        this.title = title;
        this.description = description;
        this.unlockedAt = unlockedAt;
    }
}
