package pmf.rma.cityexplorerosm.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;

public class DetailViewModel extends AndroidViewModel {

    private final PlaceRepository repository;

    public DetailViewModel(@NonNull Application application) {
        super(application);
        repository = new PlaceRepository(application);
    }

    // Na brzinu filtriramo iz liste dok ne dodamo pravi DAO getById
    public LiveData<Place> getPlaceById(long id) {
        return Transformations.map(repository.getAllPlaces(), places -> {
            for (Place p : places) {
                if (p.id == id) return p;
            }
            return null;
        });
    }
}
