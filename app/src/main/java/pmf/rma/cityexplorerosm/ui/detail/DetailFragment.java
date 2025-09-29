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
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.MaterialToolbar;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.data.local.entities.Place;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;
import pmf.rma.cityexplorerosm.provider.FavoritesProvider;

@AndroidEntryPoint
public class DetailFragment extends Fragment {

    private static final String ARG_PLACE_ID = "place_id";

    private long placeId;
    private DetailViewModel viewModel;

    private Button buttonFavorite;
    private boolean isFavorite = false;

    private MapView mapView;
    private ShimmerFrameLayout shimmerMap;
    private ShimmerFrameLayout shimmerContent;
    private View cardInfo;

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
        if (toolbar != null) {
            // Hide navigation icon in two-pane (tablet) mode (parent has detailContainer)
            View container = getActivity() != null ? getActivity().findViewById(R.id.detailContainer) : null;
            if (container != null) {
                toolbar.setNavigationIcon(null);
            } else {
                toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
            }
        }
        TextView textName = view.findViewById(R.id.text_name);
        TextView textDescription = view.findViewById(R.id.text_description);
        TextView textCoordinates = view.findViewById(R.id.text_coordinates);
        buttonFavorite = view.findViewById(R.id.button_favorite);
        mapView = view.findViewById(R.id.mapView);
        shimmerMap = view.findViewById(R.id.shimmerMap);
        shimmerContent = view.findViewById(R.id.shimmerContent);
        cardInfo = view.findViewById(R.id.cardInfo);
        // Start shimmer while loading
        startShimmer();
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        viewModel.getPlaceById((int) placeId).observe(getViewLifecycleOwner(), place -> {
            if (place != null) {
                stopShimmer();
                cardInfo.setVisibility(View.VISIBLE);
                buttonFavorite.setVisibility(View.VISIBLE);
                textName.setText(place.getName());
                textDescription.setText(place.getDescription());
                textCoordinates.setText("Lat: " + place.getLatitude() + ", Lng: " + place.getLongitude());
                if (toolbar != null) toolbar.setTitle(place.getName());
                if (mapView != null) setupMapDomain(place);
                isFavorite = checkIfFavorite(place.getId());
                updateButton();
                buttonFavorite.setOnClickListener(v -> toggleFavoriteDomain(place));
            }
        });
    }

    private void startShimmer() {
        if (shimmerMap != null) {
            shimmerMap.setVisibility(View.VISIBLE);
            shimmerMap.startShimmer();
        }
        if (shimmerContent != null) {
            shimmerContent.setVisibility(View.VISIBLE);
            shimmerContent.startShimmer();
        }
        if (cardInfo != null) cardInfo.setVisibility(View.GONE);
        if (buttonFavorite != null) buttonFavorite.setVisibility(View.GONE);
    }

    private void stopShimmer() {
        if (shimmerMap != null) {
            shimmerMap.stopShimmer();
            shimmerMap.setVisibility(View.GONE);
        }
        if (shimmerContent != null) {
            shimmerContent.stopShimmer();
            shimmerContent.setVisibility(View.GONE);
        }
    }

    private void setupMapDomain(PlaceDomain place) {
        GeoPoint point = new GeoPoint(place.getLatitude(), place.getLongitude());
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(point);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(place.getName());
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

    private void toggleFavoriteDomain(PlaceDomain place) {
        if (isFavorite) {
            int rows = requireContext().getContentResolver().delete(
                    FavoritesProvider.CONTENT_URI,
                    "placeId=?",
                    new String[]{String.valueOf(place.getId())}
            );
            if (rows > 0) {
                Toast.makeText(getContext(), getString(R.string.favorite_remove), Toast.LENGTH_SHORT).show();
                isFavorite = false;
                updateButton();
            }
        } else {
            ContentValues values = new ContentValues();
            values.put("placeId", place.getId());
            values.put("timestamp", System.currentTimeMillis());
            Uri result = requireContext().getContentResolver().insert(
                    FavoritesProvider.CONTENT_URI, values
            );
            if (result != null) {
                Toast.makeText(getContext(), getString(R.string.favorite_add), Toast.LENGTH_SHORT).show();
                isFavorite = true;
                updateButton();
            }
        }
    }

    private void updateButton() {
        if (buttonFavorite == null) return;
        buttonFavorite.setText(isFavorite ? R.string.favorite_remove : R.string.favorite_add);
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
