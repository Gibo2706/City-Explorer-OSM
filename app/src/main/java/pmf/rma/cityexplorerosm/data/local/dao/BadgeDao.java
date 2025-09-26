package pmf.rma.cityexplorerosm.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import pmf.rma.cityexplorerosm.data.local.entities.Badge;

@Dao
public interface BadgeDao {
    @Query("SELECT * FROM badges ORDER BY unlockedAt DESC")
    LiveData<List<Badge>> observeBadges();

    @Query("SELECT COUNT(*) FROM badges WHERE id = :id")
    int hasBadgeSync(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Badge badge);
}
