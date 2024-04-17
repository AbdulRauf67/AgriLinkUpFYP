package thesis.agriproducts.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.reactivex.annotations.NonNull;
import thesis.agriproducts.R;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;

public class HomeActivity extends AppCompatActivity {

    String TAG = "Home Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Utils.switchContent(this, R.id.fragContainer, Tags.HOME_FRAGMENT);
    }

    @Override
    public void onBackPressed() {
//        moveTaskToBack(false);
        int count = getSupportFragmentManager().getBackStackEntryCount();
        Log.d(TAG, "onBackPressed: " + count);
        if (count == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    //region Navigation Listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Utils.switchContent(HomeActivity.this, R.id.fragContainer, Tags.HOME_FRAGMENT);
                    return true;
                case R.id.navigation_sell:
                    Utils.switchContent(HomeActivity.this, R.id.fragContainer, Tags.SELL_FRAGMENT);
                    return true;
                case R.id.navigation_inbox:
                    Utils.switchContent(HomeActivity.this, R.id.fragContainer, Tags.INBOX_FRAGMENT);
                    return true;
                case R.id.navigation_orders:
                    Utils.switchContent(HomeActivity.this, R.id.fragContainer, Tags.ORDERS_FRAGMENT);
                    return true;
                case R.id.navigation_profile:
                    Utils.switchContent(HomeActivity.this, R.id.fragContainer, Tags.PROFILE_FRAGMENT);
                    return true;
            }
            return false;
        }
    };
    //endregion
}
