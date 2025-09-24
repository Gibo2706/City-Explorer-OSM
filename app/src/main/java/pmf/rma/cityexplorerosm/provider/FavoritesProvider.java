package pmf.rma.cityexplorerosm.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pmf.rma.cityexplorerosm.data.local.db.AppDatabase;
import pmf.rma.cityexplorerosm.data.local.entities.Favorite;

import java.util.List;

public class FavoritesProvider extends ContentProvider {

    public static final String AUTHORITY = "pmf.rma.cityexplorerosm.provider.favorites";
    public static final String TABLE_NAME = "favorites";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    private static final int FAVORITES = 1;
    private static final int FAVORITE_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, TABLE_NAME, FAVORITES);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", FAVORITE_ID);
    }

    @Override
    public boolean onCreate() {
        return true; // DB se lazy-inicijalizuje kad zatreba
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        AppDatabase db = AppDatabase.getInstance(getContext());
        switch (uriMatcher.match(uri)) {
            case FAVORITES:
                return db.getOpenHelper().getReadableDatabase()
                        .query("SELECT * FROM favorites");
            case FAVORITE_ID:
                String id = uri.getLastPathSegment();
                return db.getOpenHelper().getReadableDatabase()
                        .query("SELECT * FROM favorites WHERE id = ?", new Object[]{id});
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case FAVORITES:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
            case FAVORITE_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) != FAVORITES) {
            throw new IllegalArgumentException("Invalid URI for insert: " + uri);
        }
        AppDatabase db = AppDatabase.getInstance(getContext());
        assert values != null;
        long id = db.getOpenHelper().getWritableDatabase()
                .insert("favorites", 0, values);
        if (id > 0) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        AppDatabase db = AppDatabase.getInstance(getContext());
        switch (uriMatcher.match(uri)) {
            case FAVORITES:
                return db.getOpenHelper().getWritableDatabase()
                        .delete("favorites", selection, selectionArgs);
            case FAVORITE_ID:
                String id = uri.getLastPathSegment();
                return db.getOpenHelper().getWritableDatabase()
                        .delete("favorites", "id=?", new String[]{id});
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        AppDatabase db = AppDatabase.getInstance(getContext());
        switch (uriMatcher.match(uri)) {
            case FAVORITES:
                assert values != null;
                return db.getOpenHelper().getWritableDatabase()
                        .update("favorites", 0, values, selection, selectionArgs);
            case FAVORITE_ID:
                String id = uri.getLastPathSegment();
                assert values != null;
                return db.getOpenHelper().getWritableDatabase()
                        .update("favorites", 0, values, "id=?", new String[]{id});
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
