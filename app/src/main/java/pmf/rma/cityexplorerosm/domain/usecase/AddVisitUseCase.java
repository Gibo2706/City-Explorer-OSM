package pmf.rma.cityexplorerosm.domain.usecase;

import javax.inject.Inject;

import pmf.rma.cityexplorerosm.data.repo.GamificationRepository;

public class AddVisitUseCase {
    private final GamificationRepository repo;

    @Inject
    public AddVisitUseCase(GamificationRepository repo) {
        this.repo = repo;
    }

    public void execute(int placeId) {
        repo.markVisited(placeId);
    }
}
