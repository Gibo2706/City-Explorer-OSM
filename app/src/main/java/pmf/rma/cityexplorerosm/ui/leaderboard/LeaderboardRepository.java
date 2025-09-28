package pmf.rma.cityexplorerosm.ui.leaderboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import pmf.rma.cityexplorerosm.util.Resource;

/** Repository koji iz Firestore "users" kolekcije čita top N korisnika po poenima. */
@Singleton
public class LeaderboardRepository {

    private final FirebaseFirestore store;
    private ListenerRegistration activeListener;

    @Inject
    public LeaderboardRepository(FirebaseFirestore store) {
        this.store = store;
    }

    public LiveData<Resource<List<LeaderboardEntry>>> observeTop(int limit) {
        MutableLiveData<Resource<List<LeaderboardEntry>>> live = new MutableLiveData<>();
        live.setValue(Resource.loading());
        if (activeListener != null) {
            activeListener.remove();
            activeListener = null;
        }
        activeListener = store.collection("users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        live.postValue(Resource.error(e.getMessage() == null ? "Greška pri učitavanju" : e.getMessage()));
                        return;
                    }
                    if (snap == null || snap.isEmpty()) {
                        live.postValue(Resource.success(Collections.emptyList()));
                        return;
                    }
                    List<LeaderboardEntry> list = new ArrayList<>();
                    int rank = 1;
                    for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                        String uid = d.getId();
                        String displayName = d.getString("displayName");
                        String username = d.getString("username");
                        Number pts = d.getLong("points");
                        list.add(new LeaderboardEntry(uid, displayName, username, pts == null ? 0 : pts.intValue(), rank++));
                    }
                    live.postValue(Resource.success(list));
                });
        return live;
    }

    public void clear() {
        if (activeListener != null) {
            activeListener.remove();
            activeListener = null;
        }
    }
}
