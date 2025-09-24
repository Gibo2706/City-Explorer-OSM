package pmf.rma.cityexplorerosm.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cityexplorer.R;

import pmf.rma.cityexplorerosm.ui.list.ListFragment;
import pmf.rma.cityexplorerosm.ui.map.MapFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ListFragment())
                    .commit();
        }
    }
}
