package pmf.rma.cityexplorerosm.di;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import pmf.rma.cityexplorerosm.data.local.dao.PlaceDao;
import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.data.remote.ApiService;
import pmf.rma.cityexplorerosm.data.remote.RetrofitClient;
import pmf.rma.cityexplorerosm.data.repo.PlaceRepository;
import pmf.rma.cityexplorerosm.domain.usecase.GetPlaceDetailsUseCase;
import pmf.rma.cityexplorerosm.domain.usecase.GetPlacesUseCase;

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
        ).fallbackToDestructiveMigration().build();
    }

    @Provides
    public static PlaceDao providePlaceDao(AppDatabase db) {
        return db.placeDao();
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
    public static GetPlacesUseCase provideGetPlacesUseCase(PlaceRepository repo) {
        return new GetPlacesUseCase(repo);
    }

    @Provides
    public static GetPlaceDetailsUseCase provideGetPlaceDetailsUseCase(PlaceRepository repo) {
        return new GetPlaceDetailsUseCase(repo);
    }
}
