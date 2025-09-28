package pmf.rma.cityexplorerosm.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import pmf.rma.cityexplorerosm.data.local.entities.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> observeUser(String id);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserSync(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Query("UPDATE users SET points = :points WHERE id = :id")
    void updatePoints(String id, int points);

    @Query("UPDATE users SET displayName=:displayName, firstName=:firstName, lastName=:lastName, username=:username WHERE id=:id")
    void updateProfile(String id, String displayName, String firstName, String lastName, String username);

}
