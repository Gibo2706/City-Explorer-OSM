package pmf.rma.cityexplorerosm.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import pmf.rma.cityexplorerosm.data.local.entities.Badge;

@Dao
public interface BadgeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Badge badge);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Badge> list);

    @Query("SELECT * FROM badges WHERE userId=:userId")
    LiveData<List<Badge>> observeBadges(String userId);

    @Query("SELECT COUNT(*) FROM badges WHERE userId=:userId AND id = :id")
    int hasBadgeSync(String userId, String id);

    @Query("SELECT * FROM badges WHERE userId=:userId")
    List<Badge> getAllSyncForUser(String userId);
}
