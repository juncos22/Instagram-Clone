package dev.nicolas.firebasedemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dev.nicolas.firebasedemo.fragments.HomeFragment;
import dev.nicolas.firebasedemo.fragments.NotificationFragment;
import dev.nicolas.firebasedemo.fragments.ProfileFragment;
import dev.nicolas.firebasedemo.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case (int) R.id.nav_home:
                    selectorFragment = new HomeFragment();
                    break;
                case (int) R.id.nav_search:
                    selectorFragment = new SearchFragment();
                    break;
                case (int) R.id.nav_add:
                    selectorFragment = null;
                    startActivity(new Intent(MainActivity.this, PostActivity.class));
                    break;
                case (int) R.id.nav_heart:
                    selectorFragment = new NotificationFragment();
                    break;
                case (int) R.id.nav_profile:
                    selectorFragment = new ProfileFragment();
                    break;
            }
            if (selectorFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorFragment).commit();
            }
            return true;
        });

        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            String profileId = intent.getString("publisherId");
            getSharedPreferences("PROFILE", MODE_PRIVATE).edit().putString("profileId", profileId).apply();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }
}