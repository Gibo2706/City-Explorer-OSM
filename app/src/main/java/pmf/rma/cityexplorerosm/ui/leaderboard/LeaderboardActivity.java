package pmf.rma.cityexplorerosm.ui.leaderboard;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityexplorer.R;

import java.util.Collections;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.util.Resource;

@AndroidEntryPoint
public class LeaderboardActivity extends AppCompatActivity {

    private LeaderboardViewModel viewModel;
    private LeaderboardAdapter adapter;
    private ProgressBar progress;
    private TextView empty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        RecyclerView rv = findViewById(R.id.rvLeaderboard);
        progress = findViewById(R.id.progressLb);
        empty = findViewById(R.id.tvEmptyLb);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);
        observe();
    }

    private void observe() {
        viewModel.getTop().observe(this, res -> {
            if (res == null) return;
            if (res.status == Resource.Status.LOADING) {
                progress.setVisibility(View.VISIBLE);
                empty.setVisibility(View.GONE);
            } else {
                progress.setVisibility(View.GONE);
            }
            switch (res.status) {
                case SUCCESS:
                    if (res.data == null || res.data.isEmpty()) {
                        adapter.submit(Collections.emptyList());
                        empty.setVisibility(View.VISIBLE);
                        empty.setText(getString(R.string.leaderboard_empty));
                    } else {
                        empty.setVisibility(View.GONE);
                        adapter.submit(res.data);
                    }
                    break;
                case ERROR:
                    adapter.submit(Collections.emptyList());
                    empty.setVisibility(View.VISIBLE);
                    empty.setText(res.message == null ? getString(R.string.leaderboard_empty) : res.message);
                    break;
            }
        });
    }
}

