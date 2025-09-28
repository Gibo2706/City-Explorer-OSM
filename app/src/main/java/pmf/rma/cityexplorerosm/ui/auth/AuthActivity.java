package pmf.rma.cityexplorerosm.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.cityexplorer.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.auth.AuthManager;
import pmf.rma.cityexplorerosm.auth.UserAccountRepository;

@AndroidEntryPoint
public class AuthActivity extends AppCompatActivity {

    @Inject
    AuthManager auth;
    @Inject
    UserAccountRepository accountRepo;
    private EditText email, pass;
    private Button btnIn, btnUp, btnOut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        email = findViewById(R.id.etEmail);
        pass = findViewById(R.id.etPassword);
        btnIn = findViewById(R.id.btnSignIn);
        btnUp = findViewById(R.id.btnSignUp);
        btnOut = findViewById(R.id.btnSignOut);


        auth.observeUserId().observe(this, uid -> {
            boolean loggedIn = !"local_user".equals(uid);
            if (loggedIn) {
                btnIn.setVisibility(View.GONE);
                btnUp.setVisibility(View.GONE);
                btnOut.setVisibility(View.VISIBLE);
            } else {
                btnIn.setVisibility(View.VISIBLE);
                btnUp.setVisibility(View.VISIBLE);
                btnOut.setVisibility(View.GONE);
            }
        });

        // PRIJAVA: posle uspeha povuci remote -> local
        btnIn.setOnClickListener(v -> auth.signInEmail(
                email.getText().toString().trim(),
                pass.getText().toString(),
                new AuthManager.Callback() {
                    @Override
                    public void onSuccess() {
                        accountRepo.afterLoginSync(new UserAccountRepository.Callback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(AuthActivity.this, R.string.login_ok, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(String msg) {
                                Toast.makeText(AuthActivity.this, msg, Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onError(String msg) {
                        toast(msg);
                    }
                }
        ));

        // REGISTRACIJA: otvori dialog
        btnUp.setOnClickListener(v -> {
            FragmentManager fm = getSupportFragmentManager();
            new RegisterDialogFragment().show(fm, "register");
        });

        btnOut.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, R.string.logout_ok, Toast.LENGTH_SHORT).show();
        });

        if (!auth.isFirebaseEnabled()) {
            findViewById(R.id.tvAuthStatus).setVisibility(View.VISIBLE);
            findViewById(R.id.tvAuthStatus).setOnClickListener(v ->
                    Toast.makeText(this, R.string.firebase_not_configured, Toast.LENGTH_LONG).show()
            );
        }
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
    }
}
