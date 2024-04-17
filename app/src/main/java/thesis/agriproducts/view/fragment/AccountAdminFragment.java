package thesis.agriproducts.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thesis.agriproducts.R;
import thesis.agriproducts.domain.Api;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Utils;

public class AccountAdminFragment extends Fragment {

    //region Attributes
    View mView;
    TextView mName;
    TextView mEmail;
    int adminId;
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_account_admin, container, false);
        mName = mView.findViewById(R.id.txtAdminName);
        mEmail = mView.findViewById(R.id.txtAdminEmail);
        Button mChangePassword = mView.findViewById(R.id.btnAdminChangePassowrd);
        Button mAddAdmin = mView.findViewById(R.id.btnAdminAdd);
        Button mLogout = mView.findViewById(R.id.btnAdminLogout);
        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePassword();

            }
        });
        mAddAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAdmin();

            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
        showAdmin();
        return mView;
    }

    private void showAdmin() {
        adminId = SharedPrefManager.getInstance().getUser(getActivity()).getUserId();
        mName.setText(SharedPrefManager.getInstance().getUser(getActivity()).getName());
        mEmail.setText(SharedPrefManager.getInstance().getUser(getActivity()).getEmail());
    }

    private void showChangePassword() {
        //Initialize
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        final LinearLayout layout = new LinearLayout(getActivity());
        final EditText mOldPassword = new EditText (getActivity());
        final EditText mNewPassword = new EditText (getActivity());

        //Layout
        mOldPassword.setHint("Current Password");
        mNewPassword.setHint("New Password");
        mOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(mOldPassword);
        layout.addView(mNewPassword);
        alert.setTitle("Change Password");
        alert.setView(layout);

        //Handler
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String oldPassword = mOldPassword.getText().toString();
                String newPassword = mNewPassword.getText().toString();
                changePassword(oldPassword, newPassword);
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { dialog.dismiss();
            }
        });
        alert.show();
    }

    private void showAddAdmin() {
        //Initialize
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        final LinearLayout layout = new LinearLayout(getActivity());
        final EditText mName = new EditText (getActivity());
        final EditText mEmail = new EditText (getActivity());
        final EditText mPassword = new EditText (getActivity());

        //Layout
        mName.setHint("Name");
        mEmail.setHint("Email");
        mPassword.setHint("Password");
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(mName);
        layout.addView(mEmail);
        layout.addView(mPassword);
        alert.setTitle("Add Admin Account");
        alert.setView(layout);

        //Handler
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = mName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                addAdmin(name, email, password);
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { dialog.dismiss();
            }
        });
        alert.show();
    }

    private void changePassword(String oldP, String newP) {
        if (Utils.isEmptyFields(oldP, newP)) {
            Toast.makeText(getActivity(), "Invalid Fields", Toast.LENGTH_LONG).show();
        } else if (!Utils.isPasswordValid(oldP)) {
            Toast.makeText(getActivity(), "Invalid Old Password", Toast.LENGTH_LONG).show();
        } else if (!Utils.isPasswordValid(newP)) {
            Toast.makeText(getActivity(), "Invalid New Password", Toast.LENGTH_LONG).show();
        }

        Api.getInstance().getServices().changePassword(adminId, oldP, newP).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    logOut();
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@Nullable Call<Result> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Api Failure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addAdmin(String name, String email, String password) {
        if (Utils.isEmptyFields(name, email, password)) {
            Toast.makeText(getActivity(), "Invalid Fields", Toast.LENGTH_LONG).show();
        } else if (!Utils.isPasswordValid(password)) {
            Toast.makeText(getActivity(), "Invalid Password. (More than 8 characters)", Toast.LENGTH_LONG).show();
        }

        Api.getInstance().getServices().setAdmin(name, email, password).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    logOut();
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@Nullable Call<Result> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Api Failure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logOut() {
        SharedPrefManager.getInstance().logout(getActivity());
        getActivity().finish();
    }
}
