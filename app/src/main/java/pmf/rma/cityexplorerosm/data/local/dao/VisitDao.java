package pmf.rma.cityexplorerosm.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

import pmf.rma.cityexplorerosm.data.local.entities.Visit;

@Dao
public interface VisitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Visit visit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Visit> visits);

    @Update
    int update(Visit visit);

    @Query("SELECT * FROM visits WHERE userId=:userId AND placeId = :placeId LIMIT 1")
    Visit getByPlaceIdSync(String userId, int placeId);

    @Query("SELECT * FROM visits WHERE userId=:userId AND placeId = :placeId LIMIT 1")
    LiveData<Visit> observeByPlaceId(String userId, int placeId);

    @Query("SELECT COUNT(DISTINCT placeId) FROM visits WHERE userId=:userId AND status = 'VERIFIED'")
    int countDistinctVerifiedPlacesSync(String userId);

    @Query("SELECT * FROM visits WHERE userId=:userId")
    List<Visit> getAllSyncForUser(String userId);
}
