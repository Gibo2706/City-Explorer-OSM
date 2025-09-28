package pmf.rma.cityexplorerosm.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import pmf.rma.cityexplorerosm.data.local.entities.Visit;

@Dao
public interface VisitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Visit visit);

    @Update
    int update(Visit visit);

    @Query("SELECT * FROM visits WHERE placeId = :placeId LIMIT 1")
    Visit getByPlaceIdSync(int placeId);

    @Query("SELECT * FROM visits WHERE placeId = :placeId LIMIT 1")
    LiveData<Visit> observeByPlaceId(int placeId);

    @Query("SELECT COUNT(DISTINCT placeId) FROM visits WHERE status = 'VERIFIED'")
    int countDistinctVerifiedPlacesSync();

    // 🔹 za sync
    @Query("SELECT * FROM visits")
    List<Visit> getAllSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Visit> visits);
}
