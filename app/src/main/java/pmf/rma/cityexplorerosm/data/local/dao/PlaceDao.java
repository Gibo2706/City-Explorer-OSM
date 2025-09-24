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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Place place);

    @Query("SELECT * FROM places")
    LiveData<List<Place>> getAllPlaces();

    @Query("DELETE FROM places")
    void clearAll();
}
