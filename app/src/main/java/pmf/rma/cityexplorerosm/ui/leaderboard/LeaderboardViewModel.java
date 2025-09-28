package pmf.rma.cityexplorerosm.ui.leaderboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pmf.rma.cityexplorerosm.util.Resource;

@HiltViewModel
public class LeaderboardViewModel extends ViewModel {
    private final LeaderboardRepository repo;
    private final LiveData<Resource<List<LeaderboardEntry>>> top;

    @Inject
    public LeaderboardViewModel(LeaderboardRepository repo) {
        this.repo = repo;
        this.top = repo.observeTop(10);
    }

    public LiveData<Resource<List<LeaderboardEntry>>> getTop() { return top; }

    @Override
    protected void onCleared() {
        super.onCleared();
        repo.clear();
    }
}

