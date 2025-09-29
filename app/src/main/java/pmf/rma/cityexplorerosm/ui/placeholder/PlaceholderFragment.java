package pmf.rma.cityexplorerosm.ui.placeholder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cityexplorer.R;

/** Simple placeholder fragment shown on tablets before a place is selected. */
public class PlaceholderFragment extends Fragment {

    public static PlaceholderFragment newInstance() { return new PlaceholderFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detail_placeholder, container, false);
    }
}

