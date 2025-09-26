package pmf.rma.cityexplorerosm.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityexplorer.R;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.domain.model.BadgeDomain;
import pmf.rma.cityexplorerosm.domain.model.UserDomain;

import java.util.ArrayList;
import java.util.List;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private ProfileViewModel viewModel;
    private TextView tvName, tvPoints;
    private RecyclerView rvBadges;
    private BadgesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        tvName = v.findViewById(R.id.tvUserName);
        tvPoints = v.findViewById(R.id.tvUserPoints);
        rvBadges = v.findViewById(R.id.rvBadges);

        adapter = new BadgesAdapter();
        rvBadges.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBadges.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), this::bindUser);
        viewModel.getBadges().observe(getViewLifecycleOwner(), this::bindBadges);
    }

    private void bindUser(UserDomain user) {
        if (user == null) return;
        tvName.setText(user.getDisplayName());
        tvPoints.setText(String.valueOf(user.getPoints()));
    }

    private void bindBadges(List<BadgeDomain> badges) {
        adapter.submit(badges != null ? badges : new ArrayList<>());
    }
}
