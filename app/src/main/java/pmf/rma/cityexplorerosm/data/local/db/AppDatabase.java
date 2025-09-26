package pmf.rma.cityexplorerosm.data.local.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pmf.rma.cityexplorerosm.data.local.dao.FavoriteDao;
import pmf.rma.cityexplorerosm.data.local.dao.PlaceDao;
import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.data.local.entities.Favorite;

@Database(
        entities = {Place.class, Favorite.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PlaceDao placeDao();

    public abstract FavoriteDao favoriteDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "cityexplorer.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
