package pmf.rma.cityexplorerosm.ui.detail;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cityexplorer.R;
import com.google.android.material.appbar.MaterialToolbar;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.provider.FavoritesProvider;

public class DetailFragment extends Fragment {

    private static final String ARG_PLACE_ID = "place_id";

    private long placeId;
    private DetailViewModel viewModel;

    private Button buttonFavorite;
    private boolean isFavorite = false;

    private MapView mapView;

    public static DetailFragment newInstance(long placeId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PLACE_ID, placeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            placeId = getArguments().getLong(ARG_PLACE_ID);
        }
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        TextView textName = view.findViewById(R.id.text_name);
        TextView textDescription = view.findViewById(R.id.text_description);
        TextView textCoordinates = view.findViewById(R.id.text_coordinates);
        buttonFavorite = view.findViewById(R.id.button_favorite);
        mapView = view.findViewById(R.id.mapView);

        // init osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setMultiTouchControls(true);

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        viewModel.getPlaceById(placeId).observe(getViewLifecycleOwner(), place -> {
            if (place != null) {
                textName.setText(place.name);
                textDescription.setText(place.description);
                textCoordinates.setText(
                        "Lat: " + place.latitude + ", Lng: " + place.longitude
                );

                setupMap(place);

                // provera da li je već u favoritima
                isFavorite = checkIfFavorite(place.id);
                updateButton();

                buttonFavorite.setOnClickListener(v -> toggleFavorite(place));
            }
        });
    }

    private void setupMap(Place place) {
        GeoPoint point = new GeoPoint(place.latitude, place.longitude);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(point);

        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(place.name);
        mapView.getOverlays().clear();
        mapView.getOverlays().add(marker);
    }

    private boolean checkIfFavorite(long placeId) {
        Cursor cursor = null;
        try {
            cursor = requireContext().getContentResolver().query(
                    FavoritesProvider.CONTENT_URI,
                    null,
                    "placeId=?",
                    new String[]{String.valueOf(placeId)},
                    null
            );
            return cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void toggleFavorite(Place place) {
        if (isFavorite) {
            int rows = requireContext().getContentResolver().delete(
                    FavoritesProvider.CONTENT_URI,
                    "placeId=?",
                    new String[]{String.valueOf(place.id)}
            );
            if (rows > 0) {
                Toast.makeText(getContext(), "Uklonjeno iz favorita", Toast.LENGTH_SHORT).show();
                isFavorite = false;
                updateButton();
            }
        } else {
            // dodaj
            ContentValues values = new ContentValues();
            values.put("placeId", place.id);
            values.put("timestamp", System.currentTimeMillis());
            Uri result = requireContext().getContentResolver().insert(
                    FavoritesProvider.CONTENT_URI, values
            );
            if (result != null) {
                Toast.makeText(getContext(), "Dodato u favorite", Toast.LENGTH_SHORT).show();
                isFavorite = true;
                updateButton();
            }
        }
    }

    private void updateButton() {
        if (isFavorite) {
            buttonFavorite.setText("Ukloni iz favorita");
        } else {
            buttonFavorite.setText("Dodaj u favorite");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
}
