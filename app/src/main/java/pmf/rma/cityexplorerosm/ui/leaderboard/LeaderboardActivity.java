package pmf.rma.cityexplorerosm.ui.leaderboard;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityexplorer.R;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.Collections;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.util.Resource;

@AndroidEntryPoint
public class LeaderboardActivity extends AppCompatActivity {

    private LeaderboardViewModel viewModel;
    private LeaderboardAdapter adapter;
    private ShimmerFrameLayout shimmerContainer;
    private RecyclerView recyclerView;
    private TextView empty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        shimmerContainer = findViewById(R.id.shimmerContainer);
        recyclerView = findViewById(R.id.rvLeaderboard);
        empty = findViewById(R.id.tvEmptyLb);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);
        observe();
    }

    private void observe() {
        viewModel.getTop().observe(this, res -> {
            if (res == null) return;

            switch (res.status) {
                case LOADING:
                    shimmerContainer.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    empty.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    shimmerContainer.setVisibility(View.GONE);
                    if (res.data == null || res.data.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                        empty.setText(getString(R.string.leaderboard_empty));
                        adapter.submit(Collections.emptyList());
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        empty.setVisibility(View.GONE);
                        adapter.submit(res.data);
                    }
                    break;
                case ERROR:
                    shimmerContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                    empty.setText(res.message == null ? getString(R.string.leaderboard_empty) : res.message);
                    adapter.submit(Collections.emptyList());
                    break;
            }
        });
    }
}
