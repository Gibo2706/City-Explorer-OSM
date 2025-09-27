package pmf.rma.cityexplorerosm.data.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import pmf.rma.cityexplorerosm.data.local.dao.PlaceDao;
import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.data.remote.ApiService;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;

public class PlaceRepository {
    private final PlaceDao placeDao;
    private final ApiService apiService;

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
}
