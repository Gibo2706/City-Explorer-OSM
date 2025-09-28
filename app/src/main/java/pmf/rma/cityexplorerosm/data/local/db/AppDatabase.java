package pmf.rma.cityexplorerosm.data.local.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import pmf.rma.cityexplorerosm.data.local.dao.BadgeDao;
import pmf.rma.cityexplorerosm.data.local.dao.FavoriteDao;
import pmf.rma.cityexplorerosm.data.local.dao.PlaceDao;
import pmf.rma.cityexplorerosm.data.local.dao.UserDao;
import pmf.rma.cityexplorerosm.data.local.dao.VisitDao;
import pmf.rma.cityexplorerosm.data.local.entities.Badge;
import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.data.local.entities.Favorite;
import pmf.rma.cityexplorerosm.data.local.entities.User;
import pmf.rma.cityexplorerosm.data.local.entities.Visit;

@Database(
        entities = { Place.class, User.class, Visit.class, Badge.class, Favorite.class },
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PlaceDao placeDao();

    public abstract FavoriteDao favoriteDao();

    public abstract UserDao userDao();

    public abstract BadgeDao badgeDao();

    public abstract VisitDao visitDao();

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

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `displayName` TEXT, `points` INTEGER NOT NULL, PRIMARY KEY(`id`))");
            db.execSQL("CREATE TABLE IF NOT EXISTS `badges` (`id` TEXT NOT NULL, `title` TEXT, `description` TEXT, `unlockedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
            db.execSQL("CREATE TABLE IF NOT EXISTS `visits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `placeId` INTEGER NOT NULL, `visitedAt` INTEGER NOT NULL)");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            // visits: novi stubci
            db.execSQL("ALTER TABLE visits ADD COLUMN status TEXT DEFAULT 'PENDING'");
            db.execSQL("ALTER TABLE visits ADD COLUMN proofType TEXT");
            db.execSQL("ALTER TABLE visits ADD COLUMN proofValue TEXT");

            // places: pravila verifikacije
            db.execSQL("ALTER TABLE places ADD COLUMN verificationType TEXT");
            db.execSQL("ALTER TABLE places ADD COLUMN verificationSecret TEXT");
            db.execSQL("ALTER TABLE places ADD COLUMN verificationRadiusM INTEGER");
            db.execSQL("ALTER TABLE places ADD COLUMN verificationDwellSec INTEGER");
        }
    };
}
