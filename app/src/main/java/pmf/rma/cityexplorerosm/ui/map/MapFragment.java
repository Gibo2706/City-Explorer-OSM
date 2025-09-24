package pmf.rma.cityexplorerosm.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cityexplorer.R;
import com.google.android.material.appbar.MaterialToolbar;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

public class MapFragment extends Fragment {

    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_map, container, false);



        mapView = root.findViewById(R.id.mapView);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setMultiTouchControls(true);

        org.osmdroid.api.IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        org.osmdroid.util.GeoPoint startPoint = new org.osmdroid.util.GeoPoint(44.8176, 20.4569); // npr. Trg Republike
        mapController.setCenter(startPoint);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
