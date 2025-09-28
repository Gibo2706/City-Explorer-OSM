package pmf.rma.cityexplorerosm.ui.leaderboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityexplorer.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {
    private final List<LeaderboardEntry> items = new ArrayList<>();

    public void submit(List<LeaderboardEntry> newItems) {
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return newItems == null ? 0 : newItems.size(); }
            @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return items.get(oldItemPosition).uid.equals(newItems.get(newItemPosition).uid);
            }
            @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                LeaderboardEntry o = items.get(oldItemPosition); LeaderboardEntry n = newItems.get(newItemPosition);
                return o.points == n.points && o.rank == n.rank && safe(o.displayName).equals(safe(n.displayName)) && safe(o.username).equals(safe(n.username));
            }
            private String safe(String s){ return s==null?"":s; }
        });
        items.clear();
        if (newItems != null) items.addAll(newItems);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LeaderboardEntry e = items.get(position);
        h.tvRank.setText(e.rank + ".");
        h.tvDisplay.setText(e.displayName);
        h.tvUsername.setText(e.username == null ? "" : ("@" + e.username));
        h.tvPoints.setText(h.itemView.getContext().getString(R.string.leaderboard_points_format, e.points));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvRank; final TextView tvDisplay; final TextView tvUsername; final TextView tvPoints;
        VH(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvDisplay = itemView.findViewById(R.id.tvDisplayNameLb);
            tvUsername = itemView.findViewById(R.id.tvUsernameLb);
            tvPoints = itemView.findViewById(R.id.tvPointsLb);
        }
    }
}

