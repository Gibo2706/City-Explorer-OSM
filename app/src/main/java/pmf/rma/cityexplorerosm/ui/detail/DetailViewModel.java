package pmf.rma.cityexplorerosm.ui.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;
import pmf.rma.cityexplorerosm.domain.usecase.AddVisitUseCase;

@HiltViewModel
public class DetailViewModel extends ViewModel {
    private final PlaceRepository repository;
    private final AddVisitUseCase addVisitUseCase;

    @Inject
    public DetailViewModel(PlaceRepository placeRepository, AddVisitUseCase addVisitUseCase) {
        this.repository = placeRepository;
        this.addVisitUseCase = addVisitUseCase;
    }

    public LiveData<PlaceDomain> getPlaceById(int id) {
        return repository.getPlaceById(id);
    }

    public void markVisited(int placeId) {
        addVisitUseCase.execute(placeId);
    }
}
