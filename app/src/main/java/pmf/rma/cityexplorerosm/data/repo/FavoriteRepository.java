package pmf.rma.cityexplorerosm.data.repo;

import android.content.Context;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pmf.rma.cityexplorerosm.data.local.dao.FavoriteDao;
import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.data.local.entities.Favorite;

public class FavoriteRepository {

    private final FavoriteDao favoriteDao;
    private final ExecutorService executor;

    public FavoriteRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.favoriteDao = db.favoriteDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void insertFavorite(Favorite favorite) {
        executor.execute(() -> favoriteDao.insert(favorite));
    }

    public List<Favorite> getAllFavorites() {
        return favoriteDao.getAllFavorites();
    }

    public void clearFavorites() {
        executor.execute(favoriteDao::clearAll);
    }
}
