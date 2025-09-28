package pmf.rma.cityexplorerosm.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import pmf.rma.cityexplorerosm.auth.AuthManager;

@Module
@InstallIn(SingletonComponent.class)
public class AuthModule {

    @Provides
    @Singleton
    public static AuthManager provideAuthManager(@ApplicationContext Context ctx) {
        return new AuthManager(ctx);
    }
}
