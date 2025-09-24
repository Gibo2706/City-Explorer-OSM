package pmf.rma.cityexplorerosm.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityexplorer.R;

import java.util.ArrayList;
import java.util.List;

import pmf.rma.cityexplorerosm.data.local.entities.Place;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Place place);
    }

    private List<Place> places = new ArrayList<>();
    private final OnItemClickListener listener;

    public PlaceAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setPlaces(List<Place> newPlaces) {
        this.places = newPlaces;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.bind(places.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textDescription;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textDescription = itemView.findViewById(R.id.text_description);
        }

        public void bind(final Place place, final OnItemClickListener listener) {
            textName.setText(place.name);
            textDescription.setText(place.description);
            itemView.setOnClickListener(v -> listener.onItemClick(place));
        }
    }
}
