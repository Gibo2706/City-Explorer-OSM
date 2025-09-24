package pmf.rma.cityexplorerosm.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import pmf.rma.cityexplorerosm.data.local.entities.Favorite;

@Dao
public interface FavoriteDao {
    @Insert
    void insert(Favorite favorite);

    @Query("SELECT * FROM favorites")
    List<Favorite> getAllFavorites();

    @Query("DELETE FROM favorites")
    void clearAll();
}
