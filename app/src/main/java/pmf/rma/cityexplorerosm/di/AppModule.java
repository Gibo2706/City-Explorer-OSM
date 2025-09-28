package pmf.rma.cityexplorerosm.di;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import pmf.rma.cityexplorerosm.data.local.dao.BadgeDao;
import pmf.rma.cityexplorerosm.data.local.dao.PlaceDao;
import pmf.rma.cityexplorerosm.data.local.dao.UserDao;
import pmf.rma.cityexplorerosm.data.local.dao.VisitDao;
import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.data.remote.ApiService;
import pmf.rma.cityexplorerosm.data.remote.RetrofitClient;
import pmf.rma.cityexplorerosm.data.repo.GamificationRepository;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;
import pmf.rma.cityexplorerosm.domain.usecase.AddVisitUseCase;
import pmf.rma.cityexplorerosm.domain.usecase.GetPlaceDetailsUseCase;
import pmf.rma.cityexplorerosm.domain.usecase.GetPlacesUseCase;
import pmf.rma.cityexplorerosm.sync.FirebaseSyncManager;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                        context,
                        AppDatabase.class,
                        "cityexplorer.db"
                )
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                .fallbackToDestructiveMigration().build();
    }

    @Provides
    public static PlaceDao providePlaceDao(AppDatabase db) {
        return db.placeDao();
    }

    @Provides
    public static UserDao provideUserDao(AppDatabase db) {
        return db.userDao();
    }

    @Provides
    public static BadgeDao provideBadgeDao(AppDatabase db) {
        return db.badgeDao();
    }

    @Provides
    public static VisitDao provideVisitDao(AppDatabase db) {
        return db.visitDao();
    }

    @Provides
    @Singleton
    public static ApiService provideApiService() {
        return RetrofitClient.getClient().create(ApiService.class);
    }

    @Provides
    @Singleton
    public static PlaceRepository providePlaceRepository(PlaceDao dao, ApiService api) {
        return new PlaceRepository(dao, api);
    }

    @Provides
    @Singleton
    public static GamificationRepository provideGamificationRepository(
            UserDao u, BadgeDao b, VisitDao v, PlaceDao p, pmf.rma.cityexplorerosm.auth.AuthManager auth,
            FirebaseSyncManager fsm
    ) {
        return new GamificationRepository(u, b, v, p, auth, fsm);
    }

    @Provides
    public static GetPlacesUseCase provideGetPlacesUseCase(PlaceRepository repo) {
        return new GetPlacesUseCase(repo);
    }

    @Provides
    public static GetPlaceDetailsUseCase provideGetPlaceDetailsUseCase(PlaceRepository repo) {
        return new GetPlaceDetailsUseCase(repo);
    }

    @Provides
    public static AddVisitUseCase provideAddVisitUseCase(GamificationRepository repo) {
        return new AddVisitUseCase(repo);
    }
}
