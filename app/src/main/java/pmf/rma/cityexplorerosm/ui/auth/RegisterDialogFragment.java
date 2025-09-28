package pmf.rma.cityexplorerosm.ui.auth;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cityexplorer.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pmf.rma.cityexplorerosm.auth.UserAccountRepository;

@AndroidEntryPoint
public class RegisterDialogFragment extends DialogFragment {

    @Inject UserAccountRepository accountRepo;

    private EditText etEmail, etPass, etFirst, etLast, etUsername;
    private CheckBox cbAnalytics, cbMarketing;

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = getLayoutInflater().inflate(R.layout.dialog_register, null, false);
        etEmail = v.findViewById(R.id.etEmail);
        etPass = v.findViewById(R.id.etPassword);
        etFirst = v.findViewById(R.id.etFirstName);
        etLast = v.findViewById(R.id.etLastName);
        etUsername = v.findViewById(R.id.etUsername);
        cbAnalytics = v.findViewById(R.id.cbAnalytics);
        cbMarketing = v.findViewById(R.id.cbMarketing);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.register_title)
                .setView(v)
                .setPositiveButton(R.string.sign_up, (d, w) -> {
                    String email = etEmail.getText().toString().trim();
                    String pass  = etPass.getText().toString();
                    String first = etFirst.getText().toString().trim();
                    String last  = etLast.getText().toString().trim();
                    String user  = etUsername.getText().toString().trim();
                    boolean a = cbAnalytics.isChecked();
                    boolean m = cbMarketing.isChecked();

                    if (TextUtils.isEmpty(user)) {
                        Toast.makeText(requireContext(), R.string.username_required, Toast.LENGTH_LONG).show();
                        return;
                    }

                    accountRepo.registerWithProfile(email, pass, first, last, user, a, m, new UserAccountRepository.Callback() {
                        @Override public void onSuccess() {
                            Toast.makeText(requireContext(), R.string.register_ok, Toast.LENGTH_SHORT).show();
                            dismissAllowingStateLoss();
                        }
                        @Override public void onError(String msg) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
                .create();
    }
}
