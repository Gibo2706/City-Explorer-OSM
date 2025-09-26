package pmf.rma.cityexplorerosm.ui.detail;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.cityexplorer.R;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;

@AndroidEntryPoint
public class DetailActivity extends AppCompatActivity {

    private DetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        int placeId = getIntent().getIntExtra("place_id", -1);

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        viewModel.getPlaceById(placeId).observe(this, this::bindData);
    }

    private void bindData(PlaceDomain place) {
        if (place == null) return;

        TextView name = findViewById(R.id.placeName);
        TextView category = findViewById(R.id.placeCategory);
        TextView desc = findViewById(R.id.placeDescription);
        TextView hours = findViewById(R.id.placeWorkingHours);
        ImageView img = findViewById(R.id.placeImage);

        name.setText(place.getName());
        category.setText(place.getCategory());
        desc.setText(place.getDescription());
        hours.setText("Radno vreme: " + place.getWorkingHours());

        Glide.with(this)
                .load(place.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(img);
    }
}
