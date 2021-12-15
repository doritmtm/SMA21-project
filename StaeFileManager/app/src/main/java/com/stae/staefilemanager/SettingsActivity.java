package com.stae.staefilemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.widget.NestedScrollView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat nightSwitch,systemNightSwitch;
    private NestedScrollView settingsScroll;
    private SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(findViewById(R.id.toolbar2));
        pref=AppState.instance().getPreferences();
        nightSwitch=findViewById(R.id.nightSwitch);
        systemNightSwitch=findViewById(R.id.systemNightSwitch);
        settingsScroll=findViewById(R.id.settingsScroll);
        settingsScroll.post(() -> settingsScroll.scrollTo(0,0));
        nightSwitch.setChecked(pref.getBoolean("nightModeChecked",false));
        systemNightSwitch.setChecked(pref.getBoolean("systemNightModeChecked",true));
        nightSwitch.setEnabled(!pref.getBoolean("systemNightModeChecked",true));
        nightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nightSwitch.isChecked())
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    pref.edit().putBoolean("nightModeChecked",true).apply();
                }
                else
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    pref.edit().putBoolean("nightModeChecked",false).apply();
                }
            }
        });
        systemNightSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(systemNightSwitch.isChecked())
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    nightSwitch.setEnabled(false);
                    pref.edit().putBoolean("systemNightModeChecked",true).apply();
                }
                else
                {
                    nightSwitch.setEnabled(true);
                    pref.edit().putBoolean("systemNightModeChecked",false).apply();
                    if(nightSwitch.isChecked())
                    {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                    else
                    {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.toolbarSettings:
                Intent intent=new Intent(this,SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}