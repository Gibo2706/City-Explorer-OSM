package pmf.rma.cityexplorerosm.domain.usecase;

import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;

public class GetPlaceDetailsUseCase {
    private final PlaceRepository repository;

    public GetPlaceDetailsUseCase(PlaceRepository repository) {
        this.repository = repository;
    }

    public PlaceDomain execute(int placeId) {
        return repository.getPlaceById(placeId).getValue();
    }
}
