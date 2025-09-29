package pmf.rma.cityexplorerosm.ui.map;

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

import pmf.rma.cityexplorerosm.domain.model.PlaceDomain;

public class DomainPlaceAdapter extends RecyclerView.Adapter<DomainPlaceAdapter.VH> {
    public interface OnPlaceClick { void onClick(PlaceDomain place); }
    private final List<PlaceDomain> items = new ArrayList<>();
    private final OnPlaceClick listener;

    public DomainPlaceAdapter(OnPlaceClick listener) {
        this.listener = listener; setHasStableIds(true);
    }

    public void submit(List<PlaceDomain> data) {
        List<PlaceDomain> newList = data == null ? new ArrayList<>() : new ArrayList<>(data);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCb(items, newList));
        items.clear();
        items.addAll(newList);
        diff.dispatchUpdatesTo(this);
    }

    @Override public long getItemId(int position) { return items.get(position).getId(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos), listener); }
    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name; TextView desc;
        VH(@NonNull View itemView) { super(itemView); name = itemView.findViewById(R.id.text_name); desc = itemView.findViewById(R.id.text_description); }
        void bind(PlaceDomain p, OnPlaceClick l) {
            name.setText(p.getName());
            desc.setText(p.getDescription());
            itemView.setOnClickListener(v -> l.onClick(p));
        }
    }

    static class DiffCb extends DiffUtil.Callback {
        private final List<PlaceDomain> oldL, newL;
        DiffCb(List<PlaceDomain> o, List<PlaceDomain> n){ oldL=o; newL=n; }
        @Override public int getOldListSize(){ return oldL.size(); }
        @Override public int getNewListSize(){ return newL.size(); }
        @Override public boolean areItemsTheSame(int oPos,int nPos){ return oldL.get(oPos).getId()==newL.get(nPos).getId(); }
        @Override public boolean areContentsTheSame(int oPos,int nPos){
            PlaceDomain a=oldL.get(oPos), b=newL.get(nPos);
            return safe(a.getName()).equals(safe(b.getName())) && safe(a.getDescription()).equals(safe(b.getDescription()));
        }
        private String safe(String s){ return s==null?"":s; }
    }
}
