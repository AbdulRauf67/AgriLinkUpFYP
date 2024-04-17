package thesis.agriproducts.view.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.reactivex.annotations.NonNull;
import thesis.agriproducts.R;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Utils.switchContent(AdminActivity.this, R.id.adminContainer, Tags.USERS_FRAGMENT);
        BottomNavigationView navigation = findViewById(R.id.adminNavigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    //region Navigation Listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_users:
                    Utils.switchContent(AdminActivity.this, R.id.adminContainer, Tags.USERS_FRAGMENT);
                    return true;
                case R.id.navigation_products:
                    Utils.switchContent(AdminActivity.this, R.id.adminContainer, Tags.PRODUCTS_FRAGMENT);
                    return true;
                case R.id.navigation_admin_account:
                    Utils.switchContent(AdminActivity.this, R.id.adminContainer, Tags.ACCOUNT_ADMIN_FRAGMENT);
                    return true;
            }
            return false;
        }
    };
    //endregion
}