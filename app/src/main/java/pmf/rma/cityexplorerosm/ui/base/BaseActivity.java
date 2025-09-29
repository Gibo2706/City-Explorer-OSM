package pmf.rma.cityexplorerosm.ui.base;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import com.example.cityexplorer.R;
import com.google.android.material.snackbar.Snackbar;

import pmf.rma.cityexplorerosm.util.UiEvents;

/** Base Activity sa Material 3 toolbar i standardnim lifecycle handling. */
public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        setupToolbar();
        initViews();
        setupObservers();
        UiEvents.get().messages().observe(this, globalSnackbarObserver);
    }

    private final Observer<String> globalSnackbarObserver = msg -> {
        if (msg == null || msg.isEmpty()) return;
        View root = findViewById(android.R.id.content);
        if (root != null) Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
    };

    @LayoutRes
    protected abstract int getLayoutResId();

    protected abstract void initViews();
    protected abstract void setupObservers();

    protected void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(shouldShowBackButton());
            }
        }
    }

    protected boolean shouldShowBackButton() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    protected void setToolbarSubtitle(String subtitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    @Override
    protected void onDestroy() {
        UiEvents.get().messages().removeObserver(globalSnackbarObserver);
        super.onDestroy();
    }
}
