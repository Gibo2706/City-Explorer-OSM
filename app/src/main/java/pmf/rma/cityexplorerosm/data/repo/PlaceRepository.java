package pmf.rma.cityexplorerosm.data.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pmf.rma.cityexplorerosm.data.local.dao.PlaceDao;
import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.data.remote.ApiService;
import pmf.rma.cityexplorerosm.data.remote.model.PlaceDto;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceRepository {
    private final PlaceDao placeDao;
    private final ApiService apiService;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public PlaceRepository(PlaceDao placeDao, ApiService apiService) {
        this.placeDao = placeDao;
        this.apiService = apiService;
    }

    public LiveData<List<PlaceDomain>> getAllPlaces() {
        return Transformations.map(placeDao.getAllPlaces(), localPlaces -> {
            List<PlaceDomain> domainPlaces = new ArrayList<>();
            for (Place place : localPlaces) {
                domainPlaces.add(new PlaceDomain(
                        place.id,
                        place.name,
                        place.description,
                        place.latitude,
                        place.longitude,
                        place.category,
                        place.imageUrl,
                        place.workingHours,
                        place.verificationType,
                        place.verificationSecret,
                        place.verificationRadiusM,
                        place.verificationDwellSec
                ));
            }
            return domainPlaces;
        });
    }


    public LiveData<PlaceDomain> getPlaceById(int id) {
        return Transformations.map(placeDao.getPlaceById(id), place -> {
            if (place == null) return null;
            return new PlaceDomain(
                    place.id,
                    place.name,
                    place.description,
                    place.latitude,
                    place.longitude,
                    place.category,
                    place.imageUrl,
                    place.workingHours,
                    place.verificationType,
                    place.verificationSecret,
                    place.verificationRadiusM,
                    place.verificationDwellSec
            );
        });
    }

    public interface RefreshCallback { void onSuccess(int count); void onError(Throwable t); }

    public void refreshFromRemote(RefreshCallback cb) {
        apiService.getPlaces().enqueue(new Callback<List<PlaceDto>>() {
            @Override public void onResponse(Call<List<PlaceDto>> call, Response<List<PlaceDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    if (cb != null) cb.onError(new RuntimeException("HTTP " + response.code()));
                    return;
                }
                List<PlaceDto> dtos = response.body();
                List<Place> entities = new ArrayList<>();
                for (PlaceDto d : dtos) {
                    // Map minimal DTO to full entity with defaults
                    entities.add(new Place(
                            d.id,
                            d.name == null ? ("Mesto " + d.id) : d.name,
                            d.description,
                            d.latitude,
                            d.longitude,
                            "Kultura", // default category (server doesn’t send)
                            null,       // imageUrl placeholder
                            null,       // workingHours placeholder
                            "NONE",
                            null,
                            null,
                            null
                    ));
                }
                io.execute(() -> {
                    try {
                        placeDao.insertAll(entities);
                        if (cb != null) cb.onSuccess(entities.size());
                    } catch (Exception e) {
                        if (cb != null) cb.onError(e);
                    }
                });
            }
            @Override public void onFailure(Call<List<PlaceDto>> call, Throwable t) {
                if (cb != null) cb.onError(t);
            }
        });
    }

    public int refreshFromRemoteBlocking() throws IOException {
        Response<List<PlaceDto>> resp = apiService.getPlaces().execute();
        if (!resp.isSuccessful() || resp.body() == null) {
            throw new IOException("HTTP " + resp.code());
        }
        List<PlaceDto> dtos = resp.body();
        List<Place> entities = new ArrayList<>();
        for (PlaceDto d : dtos) {
            entities.add(new Place(
                    d.id,
                    d.name == null ? ("Mesto " + d.id) : d.name,
                    d.description,
                    d.latitude,
                    d.longitude,
                    "Kultura",
                    null,
                    null,
                    "NONE",
                    null,
                    null,
                    null
            ));
        }
        placeDao.insertAll(entities);
        return entities.size();
    }

    public static class RefreshStats {
        public final int newCount; public final int updatedCount; public final int orphanCount; public final int totalRemote;
        public RefreshStats(int n,int u,int o,int t){ newCount=n; updatedCount=u; orphanCount=o; totalRemote=t; }
    }

    public interface RefreshStatsCallback { void onComplete(RefreshStats stats); void onError(Throwable t); }

    public void refreshFromRemoteWithStats(RefreshStatsCallback cb) {
        apiService.getPlaces().enqueue(new Callback<List<PlaceDto>>() {
            @Override public void onResponse(Call<List<PlaceDto>> call, Response<List<PlaceDto>> response) {
                if (!response.isSuccessful() || response.body()==null) { if (cb!=null) cb.onError(new IOException("HTTP "+response.code())); return; }
                List<PlaceDto> remote = response.body();
                io.execute(() -> {
                    try {
                        List<Place> local = placeDao.getAllSync();
                        // build maps
                        java.util.Map<Integer, Place> localMap = new java.util.HashMap<>();
                        for (Place p: local) localMap.put(p.id, p);
                        java.util.Set<Integer> remoteIds = new java.util.HashSet<>();
                        int newCnt=0, updCnt=0;
                        List<Place> entities = new ArrayList<>();
                        for (PlaceDto d: remote) {
                            remoteIds.add(d.id);
                            boolean existed = localMap.containsKey(d.id);
                            if (existed) updCnt++; else newCnt++;
                            entities.add(new Place(
                                    d.id,
                                    d.name==null?("Mesto "+d.id):d.name,
                                    d.description,
                                    d.latitude,
                                    d.longitude,
                                    localMap.get(d.id)!=null && localMap.get(d.id).category!=null ? localMap.get(d.id).category : "Kultura",
                                    localMap.get(d.id)!=null ? localMap.get(d.id).imageUrl : null,
                                    localMap.get(d.id)!=null ? localMap.get(d.id).workingHours : null,
                                    localMap.get(d.id)!=null ? localMap.get(d.id).verificationType : "NONE",
                                    localMap.get(d.id)!=null ? localMap.get(d.id).verificationSecret : null,
                                    localMap.get(d.id)!=null ? localMap.get(d.id).verificationRadiusM : null,
                                    localMap.get(d.id)!=null ? localMap.get(d.id).verificationDwellSec : null
                            ));
                        }
                        int orphanCnt=0;
                        for (Place p: local) if (!remoteIds.contains(p.id)) orphanCnt++;
                        placeDao.insertAll(entities);
                        if (cb!=null) cb.onComplete(new RefreshStats(newCnt, updCnt, orphanCnt, remote.size()));
                    } catch (Exception e) { if (cb!=null) cb.onError(e); }
                });
            }
            @Override public void onFailure(Call<List<PlaceDto>> call, Throwable t) { if (cb!=null) cb.onError(t); }
        });
    }
}
