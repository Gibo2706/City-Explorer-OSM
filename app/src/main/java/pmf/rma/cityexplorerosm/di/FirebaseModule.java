package pmf.rma.cityexplorerosm.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import com.google.firebase.firestore.FirebaseFirestore;

@Module
@InstallIn(SingletonComponent.class)
public class FirebaseModule {
    @Provides @Singleton
    public static FirebaseFirestore provideFirestore() {
        return FirebaseFirestore.getInstance();
    }
}
