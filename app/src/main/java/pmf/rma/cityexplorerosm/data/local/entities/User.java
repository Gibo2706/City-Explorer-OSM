package pmf.rma.cityexplorerosm.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String id;
    public String displayName;
    public int points;

    // 🔹 novo za profil
    public String firstName;
    public String lastName;
    public String username; // unique na Firestore

    @Ignore
    public User(@NonNull String id, String displayName, int points) {
        this.id = id;
        this.displayName = displayName;
        this.points = points;
    }

    public User(@NonNull String id, String displayName, int points,
                String firstName, String lastName, String username) {
        this.id = id;
        this.displayName = displayName;
        this.points = points;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }
}
