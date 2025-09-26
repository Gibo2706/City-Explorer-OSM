package pmf.rma.cityexplorerosm.domain.usecase;

import java.util.List;

import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;

public class GetPlacesUseCase {
    private final PlaceRepository repository;

    public GetPlacesUseCase(PlaceRepository repository) {
        this.repository = repository;
    }

    public List<PlaceDomain> execute() {
        return repository.getAllPlaces().getValue();
    }
}
