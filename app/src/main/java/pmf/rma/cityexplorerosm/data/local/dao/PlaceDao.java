package pmf.rma.cityexplorerosm.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import pmf.rma.cityexplorerosm.data.local.entities.Place;

@Dao
public interface PlaceDao {

    @Query("SELECT * FROM places")
    LiveData<List<Place>> getAllPlaces();

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    LiveData<Place> getPlaceById(int id);

    // 🔹 sync varijanta za repo
    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    Place getPlaceByIdSync(int id);

    @Query("SELECT COUNT(*) FROM places")
    int countSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Place> places);
}
