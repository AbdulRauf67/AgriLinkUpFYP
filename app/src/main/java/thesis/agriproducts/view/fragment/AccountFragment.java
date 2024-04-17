package thesis.agriproducts.view.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thesis.agriproducts.R;
import thesis.agriproducts.domain.Api;
import thesis.agriproducts.domain.ApiHelper;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Utils;

public class AccountFragment extends Fragment {

    //region Attributes
    View mView, mFormView, mFormEdit;
    TextView mName, mEmail, mNumber, mAddress, mErrorView;
    EditText mEditName, mEditEmail, mEditNumber, mEditAddress;
    Button mEdit, mSave;
    ProgressDialog pDialog;
    RequestBody userIdBody, nameBody, emailBody, numberBody, addressBody;
    int userId;
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_account, container, false);
        mFormView = mView.findViewById(R.id.formViewAccount);
        mFormEdit = mView.findViewById(R.id.formEditAccount);
        mErrorView = mView.findViewById(R.id.txtEditUserError);

        mName = mView.findViewById(R.id.txtUserName);
        mEmail = mView.findViewById(R.id.txtUserEmail);
        mNumber = mView.findViewById(R.id.txtUserNumber);
        mAddress = mView.findViewById(R.id.txtUserAddress);

        mEditName = mView.findViewById(R.id.txtEditUserName);
        mEditEmail = mView.findViewById(R.id.txtEditUserEmail);
        mEditNumber = mView.findViewById(R.id.txtEditUserNumber);
        mEditAddress = mView.findViewById(R.id.txtEditUserAddress);

        mEdit = mView.findViewById(R.id.btnEditUser);
        mSave = mView.findViewById(R.id.btnSaveUser);
        Button mChangePassword = mView.findViewById(R.id.btnChangePassowrd);
        Button mLogout = mView.findViewById(R.id.btnLogout);


        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditUser();

            }
        });
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUser();

            }
        });
        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePassword();

            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
        showUser();
        return mView;
    }

    private void showUser() {
        mFormView.setVisibility(View.VISIBLE);
        mFormEdit.setVisibility(View.GONE);
        mEdit.setVisibility(View.VISIBLE);
        mSave.setVisibility(View.GONE);
        userId = SharedPrefManager.getInstance().getUser(getActivity()).getUserId();
        mName.setText(SharedPrefManager.getInstance().getUser(getActivity()).getName());
        mEmail.setText(SharedPrefManager.getInstance().getUser(getActivity()).getEmail());
        mNumber.setText(SharedPrefManager.getInstance().getUser(getActivity()).getNumber());
        mAddress.setText(SharedPrefManager.getInstance().getUser(getActivity()).getAddress());
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

    private void changePassword(String oldP, String newP) {
        if (Utils.isEmptyFields(oldP, newP)) {
            Toast.makeText(getActivity(), "Invalid Fields", Toast.LENGTH_LONG).show();
        } else if (!Utils.isPasswordValid(oldP)) {
            Toast.makeText(getActivity(), "Invalid Old Password", Toast.LENGTH_LONG).show();
        } else if (!Utils.isPasswordValid(newP)) {
            Toast.makeText(getActivity(), "Invalid New Password", Toast.LENGTH_LONG).show();
        }

        Api.getInstance().getServices().changePassword(userId, oldP, newP).enqueue(new Callback<Result>() {
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

    private void showEditUser() {
        mEditName.setText(mName.getText());
        mEditEmail.setText(mEmail.getText());
        mEditNumber.setText(mNumber.getText());
        mEditAddress.setText(mAddress.getText());
        mFormView.setVisibility(View.GONE);
        mFormEdit.setVisibility(View.VISIBLE);
        mEdit.setVisibility(View.GONE);
        mSave.setVisibility(View.VISIBLE);
    }

    private void saveUser() {
        if(!authenticate()) return;
        parseRequestBody();
        pDialog = Utils.showProgressDialog(getActivity(), "Updating your account...");
        Api.getInstance().getServices().updateUser(userIdBody, nameBody, emailBody, numberBody, addressBody).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if(response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if(response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    showUser();
                } catch (Exception ex) {
                    ApiHelper.handleError(ex.getMessage(), mErrorView, pDialog);
                }
            }

            @Override
            public void onFailure(@Nullable Call<Result> call, @NonNull Throwable t) {
                Utils.dismissProgressDialog(pDialog);
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean authenticate() {
        mErrorView.setVisibility(View.GONE);
        Utils.hideKeyboard(getActivity());
        if (Utils.isEmptyFields(mEditName.getText().toString(),
                mEditEmail.getText().toString(),
                mEditNumber.getText().toString(),
                mEditAddress.getText().toString())) {
            mErrorView.setText(R.string.error_sign_up);
            mErrorView.setVisibility(View.VISIBLE);
            return false;
        } else if (!Utils.isEmailValid(mEditEmail.getText().toString())) {
            mEditEmail.setError(getString(R.string.error_invalid_email));
            mEditEmail.requestFocus();
            return false;
        }
        return true;
    }

    private void parseRequestBody() {
        userIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
        nameBody = RequestBody.create(MediaType.parse("text/plain"), mEditName.getText().toString());
        emailBody = RequestBody.create(MediaType.parse("text/plain"), mEditEmail.getText().toString());
        numberBody = RequestBody.create(MediaType.parse("text/plain"), mEditNumber.getText().toString());
        addressBody = RequestBody.create(MediaType.parse("text/plain"), mEditAddress.getText().toString());
    }

    private void logOut() {
        SharedPrefManager.getInstance().logout(getActivity());
        getActivity().finish();
    }
}
