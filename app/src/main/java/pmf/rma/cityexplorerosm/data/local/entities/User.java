package pmf.rma.cityexplorerosm.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey @NonNull
    public String id;
    public String displayName;
    public int points;

    public User(@NonNull String id, String displayName, int points) {
        this.id = id;
        this.displayName = displayName;
        this.points = points;
    }
}
