package pmf.rma.cityexplorerosm.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cityexplorer.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.auth.AuthManager;

@AndroidEntryPoint
public class AuthActivity extends AppCompatActivity {

    @Inject AuthManager auth;

    private EditText email, pass;
    private Button btnIn, btnUp, btnOut, btnDelToken;
    private CheckBox cbAnalytics, cbMarketing;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        email = findViewById(R.id.etEmail);
        pass  = findViewById(R.id.etPassword);
        btnIn = findViewById(R.id.btnSignIn);
        btnUp = findViewById(R.id.btnSignUp);
        btnOut = findViewById(R.id.btnSignOut);
        btnDelToken = findViewById(R.id.btnDeleteLocalData);
        cbAnalytics = findViewById(R.id.cbAnalytics);
        cbMarketing = findViewById(R.id.cbMarketing);

        cbAnalytics.setChecked(auth.hasAnalyticsConsent());
        cbMarketing.setChecked(auth.hasMarketingConsent());

        cbAnalytics.setOnCheckedChangeListener((v, g) -> auth.setAnalyticsConsent(g));
        cbMarketing.setOnCheckedChangeListener((v, g) -> auth.setMarketingConsent(g));

        btnIn.setOnClickListener(v -> auth.signInEmail(
                email.getText().toString().trim(),
                pass.getText().toString(),
                new AuthManager.Callback() {
                    @Override public void onSuccess() {
                        Toast.makeText(AuthActivity.this, R.string.login_ok, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override public void onError(String msg) { toast(msg); }
                }
        ));

        btnUp.setOnClickListener(v -> auth.signUpEmail(
                email.getText().toString().trim(),
                pass.getText().toString(),
                new AuthManager.Callback() {
                    @Override public void onSuccess() {
                        Toast.makeText(AuthActivity.this, R.string.register_ok, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override public void onError(String msg) { toast(msg); }
                }
        ));

        btnOut.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, R.string.logout_ok, Toast.LENGTH_SHORT).show();
        });

        btnDelToken.setOnClickListener(v -> {
            getSharedPreferences("cityexplorer.db", MODE_PRIVATE).edit().clear().apply(); // nije Room
            Toast.makeText(this, R.string.local_data_cleared, Toast.LENGTH_SHORT).show();
        });

        if (!auth.isFirebaseEnabled()) {
            findViewById(R.id.tvAuthStatus).setVisibility(View.VISIBLE);
            findViewById(R.id.tvAuthStatus).setOnClickListener(v ->
                    Toast.makeText(this, R.string.firebase_not_configured, Toast.LENGTH_LONG).show()
            );
        }
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_LONG).show(); }
}
