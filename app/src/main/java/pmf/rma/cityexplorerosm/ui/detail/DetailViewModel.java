package pmf.rma.cityexplorerosm.ui.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pmf.rma.cityexplorerosm.data.repo.GamificationRepository;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.domain.model.VisitStatus;

@HiltViewModel
public class DetailViewModel extends ViewModel {
    private final PlaceRepository placeRepository;
    private final GamificationRepository gamificationRepository;

    @Inject
    public DetailViewModel(PlaceRepository placeRepository, GamificationRepository gamificationRepository) {
        this.placeRepository = placeRepository;
        this.gamificationRepository = gamificationRepository;
    }

    public LiveData<PlaceDomain> getPlaceById(int id) {
        return placeRepository.getPlaceById(id);
    }

    public LiveData<VisitStatus> observeVisitStatus(int placeId) {
        return gamificationRepository.observeVisitStatus(placeId);
    }

    public void markVisited(int placeId) {
        gamificationRepository.markVisited(placeId);
    }

    public boolean verifyWithQr(int placeId, String payload) {
        return gamificationRepository.verifyWithQr(placeId, payload);
    }

    public boolean verifyWithGps(int placeId, double lat, double lon) {
        return gamificationRepository.verifyWithGps(placeId, lat, lon);
    }
}
