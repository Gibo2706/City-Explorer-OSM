package pmf.rma.cityexplorerosm.ui.list;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityexplorer.R;

import pmf.rma.cityexplorerosm.data.local.entities.Place;

public class ListFragment extends Fragment {

    public interface OnPlaceSelectedListener {
        void onPlaceSelected(long placeId);
    }

    private OnPlaceSelectedListener callback;
    private ListViewModel viewModel;
    private PlaceAdapter adapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnPlaceSelectedListener) {
            callback = (OnPlaceSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement OnPlaceSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_places);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PlaceAdapter(this::onPlaceClicked);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ListViewModel.class);
        viewModel.getAllPlaces().observe(getViewLifecycleOwner(), places -> {
            Log.d("ListFragment", "Broj mesta: " + (places != null ? places.size() : 0));
            adapter.setPlaces(places);
        });
    }

    private void onPlaceClicked(Place place) {
        if (callback != null) {
            callback.onPlaceSelected(place.id);
        }
    }
}
