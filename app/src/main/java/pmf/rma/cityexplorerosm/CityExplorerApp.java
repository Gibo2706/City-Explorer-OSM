package pmf.rma.cityexplorerosm;

import android.app.Application;

import java.util.Arrays;

import androidx.room.Room;

import dagger.hilt.android.HiltAndroidApp;
import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.data.local.entities.Place;

@HiltAndroidApp
public class CityExplorerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(
                            getApplicationContext(),
                            AppDatabase.class,
                            "cityexplorer.db"
                    )
                    .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build();

            if (db.placeDao().countSync() == 0) {
                db.placeDao().insertAll(Arrays.asList(
                        // 1) GPS verifikacija (atrakcija)
                        new Place(1, "Kalemegdan", "...", 44.8231, 20.4506,
                                "Kultura", "...", "08:00 - 22:00",
                                "GPS", null, 120, 60),
                        // 2) Bez verifikacije (odmah se priznaje)
                        new Place(
                                2, "Trg Republike", "Centralni trg u Beogradu",
                                44.8176, 20.4569, "Kultura",
                                "https://upload.wikimedia.org/wikipedia/commons/a/ac/Trg_republike_2021.jpg",
                                "00:00 - 24:00",
                                "NONE", null, null, null
                        ),
                        // 3) QR (lokal/biznis)
                        new Place(
                                3, "Kafeterija XYZ", "Specijalitet: single origin kafa",
                                44.8120, 20.4600, "Kafić",
                                "https://upload.wikimedia.org/wikipedia/commons/4/45/A_small_cafe_in_Tokyo.jpg",
                                "08:00 - 22:00",
                                "QR", "KAFETERIJA-XYZ-2025", null, null
                        )
                ));
            }
        }).start();
    }
}
