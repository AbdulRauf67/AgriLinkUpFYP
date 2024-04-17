package thesis.agriproducts.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;
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
import thesis.agriproducts.model.entities.User;
import thesis.agriproducts.util.Utils;

public class SignUpActivity extends AppCompatActivity {

    EditText mNameView, mEmailView, mPasswordView, mConfirmPasswordView, mNumberView, mAddressView;
    TextView mErrorView;
    ImageView mValidIdView;
    ProgressDialog pDialog;
    RequestBody nameBody, emailBody, passwordBody, numberBody, addressBody, fileBody;
    int PICK_IMAGE_REQUEST = 1;
    Uri fileUri;
    User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mErrorView = findViewById(R.id.txtSignUpError);
        mNameView = findViewById(R.id.txtSignUpName);
        mEmailView = findViewById(R.id.txtSignUpEmail);
        mPasswordView = findViewById(R.id.txtSignUpPassword);
        mConfirmPasswordView = findViewById(R.id.txtSignUpConfirmPassword);
        mNumberView = findViewById(R.id.txtSignUpMobileNumber);
        mAddressView = findViewById(R.id.txtSignUpAddress);
        mValidIdView = findViewById(R.id.imgValidId);

        Button mSignUpButton = findViewById(R.id.btnSignUp);
        Button mSignInButton = findViewById(R.id.btnSignIn);
        Button mUploadValidId = findViewById(R.id.btnUploadValidID);

        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticate();
            }
        });
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openLogin();
            }
        });
        mUploadValidId.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                fileUri = data.getData();
                mValidIdView.setImageBitmap(new Compressor(this)
                        .compressToBitmap(new File(Utils.getRealPathFromURI(this, fileUri))));
                mValidIdView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);
    }

    private void openLogin() {
        finish();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    private void clearViews() {
        mErrorView.setVisibility(View.GONE);
        mEmailView.setError(null);
        mPasswordView.setError(null);
    }

    private void authenticate() {
        clearViews();
        user = new User(
                mNameView.getText().toString(),
                mEmailView.getText().toString(),
                mPasswordView.getText().toString(),
                mNumberView.getText().toString(),
                mAddressView.getText().toString()
        );

        if (Utils.isEmptyFields(user.getName(), user.getEmail(), user.getPassword(), mConfirmPasswordView.getText().toString(), user.getNumber(), user.getAddress())) {
            mErrorView.setText(R.string.error_sign_up);
            mErrorView.setVisibility(View.VISIBLE);
        } else if (fileUri == null) {
            mErrorView.setText(R.string.error_valid_id);
            mErrorView.setVisibility(View.VISIBLE);
        } else if (!Utils.isEmailValid(user.getEmail())) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mEmailView.requestFocus();
        } else if (!Utils.isPasswordValid(user.getPassword())) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
        } else if (!user.getPassword().equals(mConfirmPasswordView.getText().toString())) {
            mPasswordView.setError(getString(R.string.error_confirm_password));
            mPasswordView.requestFocus();
        } else if (!Utils.isPasswordGreaterThanEight(user.getPassword())) {
            mPasswordView.setError(getString(R.string.error_greater_than_eight));
            mPasswordView.requestFocus();
        } else {
            Utils.hideKeyboard(this);
            fetchSignUp(user);
        }
    }

    private void fetchSignUp(User user) {

        pDialog = Utils.showProgressDialog(this, "Creating your account...");
        parseRequestBody();
        Api.getInstance().getServices().setUser(nameBody, emailBody, passwordBody, numberBody, addressBody, fileBody).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if(response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if(response.body().getError())
                        throw new Exception(response.body().getMessage());
                    signUp(response.body().getMessage());
                } catch (Exception ex) {
                    ApiHelper.handleError(ex.getMessage(), mErrorView, pDialog);
                }
            }

            @Override
            public void onFailure(@Nullable Call<Result> call, @NonNull Throwable t) {
                Utils.dismissProgressDialog(pDialog);
                Toast.makeText(SignUpActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signUp(String message) {
        finish();
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    private void parseRequestBody() {
        File filePath = new File(Utils.getRealPathFromURI(this, fileUri));
        try {
            nameBody = RequestBody.create(MediaType.parse("text/plain"), user.getName());
            emailBody = RequestBody.create(MediaType.parse("text/plain"), user.getEmail());
            passwordBody = RequestBody.create(MediaType.parse("text/plain"), user.getPassword());
            numberBody = RequestBody.create(MediaType.parse("text/plain"), user.getNumber());
            addressBody = RequestBody.create(MediaType.parse("text/plain"), user.getAddress());
            fileBody = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), filePath);
            fileBody = RequestBody.create(MediaType.parse(this.getContentResolver().getType(fileUri)), new Compressor(this).compressToFile(filePath));
        } catch (Exception ex) {

        }
    }

}
