package thesis.agriproducts.view.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thesis.agriproducts.R;
import thesis.agriproducts.domain.Api;
import thesis.agriproducts.domain.ApiHelper;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;

public class LoginActivity extends AppCompatActivity {

    String TAG = "Login Activity";
    EditText mEmailView, mPasswordView;
    TextView mErrorView;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkPermissions();
        checkUserLoggedIn();

        mEmailView = findViewById(R.id.txtEmail);
        mPasswordView = findViewById(R.id.txtPassword);
        mErrorView = findViewById(R.id.txtLoginError);
        Button mLoginButton = findViewById(R.id.btnLogin);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticate();
            }
        });
        Button mSignUpButton = findViewById(R.id.btnRegister);
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void checkPermissions() {
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.INTERNET");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_NETWORK_STATE");
        permissionCheck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
        permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1001); //Any number
        }
    }

    private void checkUserLoggedIn() {
        boolean isLoggedIn = SharedPrefManager.getInstance().isLoggedIn(this);
        int accountId = SharedPrefManager.getInstance().getUser(this).getAccountId();
        if(isLoggedIn) {
            switch (accountId) {
                case Tags.ADMIN: finish(); startActivity(new Intent(getApplicationContext(), AdminActivity.class)); break;
                case Tags.USER: finish(); startActivity(new Intent(getApplicationContext(), HomeActivity.class)); break;
            }
        }
    }

    private void authenticate() {
        mErrorView.setVisibility(View.GONE);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (Utils.isEmptyFields(email, password)) {
            mErrorView.setText(R.string.error_login);
            mErrorView.setVisibility(View.VISIBLE);
        } else {
            Utils.hideKeyboard(this);
            fetchLogin(email, password);
        }
    }

    private void fetchLogin(String email, String password) {
        pDialog = Utils.showProgressDialog(this, "Logging in...");
        Api.getInstance().getServices().login(email, password).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if(!response.isSuccessful())
                        throw new Exception(response.errorBody().string());
                    if(response.body().getError())
                        throw new Exception(response.body().getMessage());
                    if(response.body().getUser().getAccountId() != Tags.ADMIN && !response.body().getUser().getActivated())
                        throw new Exception("User is not activated");
                    SharedPrefManager.getInstance().userLogin(getApplicationContext(), response.body().getUser());
                    checkUserLoggedIn();
                } catch (Exception ex) {
                    ApiHelper.handleError(ex.getMessage(), mErrorView, pDialog);
                }
            }
            @Override
            public void onFailure(@Nullable Call<Result> call,@NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signUp() {
        finish();
        startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
    }

}

