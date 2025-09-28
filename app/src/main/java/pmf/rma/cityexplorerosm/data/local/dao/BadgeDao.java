package pmf.rma.cityexplorerosm.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import pmf.rma.cityexplorerosm.data.local.entities.Badge;

@Dao
public interface BadgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Badge badge);

    @Query("SELECT * FROM badges")
    LiveData<List<Badge>> observeBadges();

    @Query("SELECT COUNT(*) FROM badges WHERE id = :id")
    int hasBadgeSync(String id);

    // 🔹 za sync
    @Query("SELECT * FROM badges")
    List<Badge> getAllSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Badge> list);
}
