package pmf.rma.cityexplorerosm.data.repo;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pmf.rma.cityexplorerosm.data.local.dao.PlaceDao;
import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.data.remote.ApiService;
import pmf.rma.cityexplorerosm.data.remote.RetrofitClient;
import pmf.rma.cityexplorerosm.data.remote.model.PlaceDto;
import retrofit2.Call;
import retrofit2.Response;

public class PlaceRepository {

    private static final String TAG = "CityExplorerRepo";

    private final PlaceDao placeDao;
    private final ApiService apiService;
    private final ExecutorService executor;

    public PlaceRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.placeDao = db.placeDao();
        this.apiService = RetrofitClient.getApiService();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void insertPlace(Place place) {
        executor.execute(() -> placeDao.insert(place));
    }

    public List<Place> getAllPlaces() {
        return placeDao.getAllPlaces();
    }

    public void syncPlacesFromApi() {
        executor.execute(() -> {
            try {
                Call<List<PlaceDto>> call = apiService.getPlaces();
                Response<List<PlaceDto>> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    placeDao.clearAll();
                    for (PlaceDto dto : response.body()) {
                        Place place = new Place(dto.name, dto.description, dto.latitude, dto.longitude);
                        placeDao.insert(place);
                    }
                    Log.d(TAG, "Sync complete: " + response.body().size() + " places");
                } else {
                    Log.e(TAG, "Sync failed: " + response.code());
                }
            } catch (IOException e) {
                Log.e(TAG, "Network error during sync", e);
            }
        });
    }
}
