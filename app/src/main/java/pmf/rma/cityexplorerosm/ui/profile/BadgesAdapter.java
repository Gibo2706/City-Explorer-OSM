package pmf.rma.cityexplorerosm.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityexplorer.R;

import pmf.rma.cityexplorerosm.domain.model.BadgeDomain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BadgesAdapter extends RecyclerView.Adapter<BadgesAdapter.VH> {

    private final List<BadgeDomain> data = new ArrayList<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public void submit(List<BadgeDomain> badges) {
        data.clear();
        data.addAll(badges);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        BadgeDomain b = data.get(pos);
        h.title.setText(b.getTitle());
        h.desc.setText(b.getDescription());
        h.date.setText(fmt.format(b.getUnlockedAt()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, desc, date;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvBadgeTitle);
            desc = itemView.findViewById(R.id.tvBadgeDesc);
            date = itemView.findViewById(R.id.tvBadgeDate);
        }
    }
}
