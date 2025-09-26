package pmf.rma.cityexplorerosm.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import pmf.rma.cityexplorerosm.data.local.entities.Visit;

@Dao
public interface VisitDao {
    @Insert
    long insert(Visit visit);

    @Query("SELECT COUNT(DISTINCT placeId) FROM visits")
    int countDistinctPlacesSync();
}
