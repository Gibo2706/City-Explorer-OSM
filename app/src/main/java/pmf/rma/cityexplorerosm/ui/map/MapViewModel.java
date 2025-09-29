package pmf.rma.cityexplorerosm.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;
import pmf.rma.cityexplorerosm.data.local.dao.VisitDao;

@HiltViewModel
public class MapViewModel extends ViewModel {
    private final PlaceRepository repository;
    private final VisitDao visitDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private static final int DAILY_GOAL = 5; // simple constant goal
    private final MutableLiveData<Integer> dailyProgressCount = new MutableLiveData<>(0);

    @Inject
    public MapViewModel(PlaceRepository repository, VisitDao visitDao) {
        this.repository = repository;
        this.visitDao = visitDao;
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

    public void refresh(PlaceRepository.RefreshCallback cb) { repository.refreshFromRemote(cb); }

    public MutableLiveData<Integer> getDailyProgressCount() { return dailyProgressCount; }

    public void computeDailyProgress(String userId) {
        if (userId == null || userId.equals("local_user")) {
            dailyProgressCount.postValue(0);
            return;
        }
        io.execute(() -> {
            try {
                Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long start = cal.getTimeInMillis();
                long end = System.currentTimeMillis();
                int cnt = visitDao.countVerifiedInRange(userId, start, end);
                dailyProgressCount.postValue(cnt);
            } catch (Exception e) {
                dailyProgressCount.postValue(0);
            }
        });
    }

    public int getDailyGoal() { return DAILY_GOAL; }
}
