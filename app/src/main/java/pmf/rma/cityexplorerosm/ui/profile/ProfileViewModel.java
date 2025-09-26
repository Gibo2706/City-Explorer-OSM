package pmf.rma.cityexplorerosm.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pmf.rma.cityexplorerosm.data.repo.GamificationRepository;
import pmf.rma.cityexplorerosm.domain.model.BadgeDomain;
import pmf.rma.cityexplorerosm.domain.model.UserDomain;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
    private final GamificationRepository repo;

    @Inject
    public ProfileViewModel(GamificationRepository repo) {
        this.repo = repo;
    }

    public LiveData<UserDomain> getUser() {
        return repo.observeUser();
    }

    public LiveData<List<BadgeDomain>> getBadges() {
        return repo.observeBadges();
    }
}
