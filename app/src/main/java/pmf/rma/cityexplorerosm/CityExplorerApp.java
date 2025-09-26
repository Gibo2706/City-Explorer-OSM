package pmf.rma.cityexplorerosm;

import android.app.Application;

import java.util.Arrays;

import dagger.hilt.android.HiltAndroidApp;
import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.data.local.entities.Place;

@HiltAndroidApp
public class CityExplorerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        new Thread(() -> {
            AppDatabase db = androidx.room.Room.databaseBuilder(
                    getApplicationContext(),
                    AppDatabase.class,
                    "cityexplorer.db"
            ).build();

            if (db.placeDao().getAllPlaces().getValue() == null) {
                db.placeDao().insertAll(Arrays.asList(
                        new Place(1, "Kalemegdan", "Tvrđava i park u Beogradu", 44.8231, 20.4506, "Kultura",
                                "https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/20230422.Blick_von_der_Festung.Belgrad.-021.jpg/1280px-20230422.Blick_von_der_Festung.Belgrad.-021.jpg", "08:00 - 22:00"),
                        new Place(2, "Trg Republike", "Centralni trg u Beogradu", 44.8176, 20.4569, "Kultura",
                                "https://upload.wikimedia.org/wikipedia/commons/a/ac/Trg_republike_2021.jpg", "00:00 - 24:00"),
                        new Place(3, "Ada Ciganlija", "Rekreaciona zona na Savi", 44.7922, 20.4094, "Restoran",
                                "https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Ada_Ciganlija_panorama.jpg/1920px-Ada_Ciganlija_panorama.jpg", "09:00 - 20:00")
                ));
            }
        }).start();
    }
}
