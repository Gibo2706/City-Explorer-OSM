package pmf.rma.cityexplorerosm.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;

@HiltViewModel
public class MapViewModel extends ViewModel {
    private final PlaceRepository repository;

    @Inject
    public MapViewModel(PlaceRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<PlaceDomain>> getPlaces() {
        return repository.getAllPlaces();
    }

    public LiveData<List<PlaceDomain>> getFilteredPlaces(String category) {
        return Transformations.map(repository.getAllPlaces(), places -> {
            if (category == null || category.isEmpty()) return places;
            List<PlaceDomain> filtered = new ArrayList<>();
            for (PlaceDomain p : places) {
                if (p.getCategory().equalsIgnoreCase(category)) {
                    filtered.add(p);
                }
            }
            return filtered;
        });
    }

}
