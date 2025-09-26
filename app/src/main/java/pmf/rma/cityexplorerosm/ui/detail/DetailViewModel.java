package pmf.rma.cityexplorerosm.ui.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;

@HiltViewModel
public class DetailViewModel extends ViewModel {
    private final PlaceRepository repository;

    @Inject
    public DetailViewModel(PlaceRepository repository) {
        this.repository = repository;
    }

    public LiveData<PlaceDomain> getPlaceById(int id) {
        return repository.getPlaceById(id);
    }
}
