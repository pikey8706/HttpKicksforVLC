package jp.pikey8706.httpkicksforvlc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import jp.pikey8706.httpkicksforvlc.ui.main.TunerPagerAdapter;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TunerPagerAdapter tunerPagerAdapter = new TunerPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(tunerPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.GONE);

        setupAvahiService();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        cleanupAvahiService();
        super.onDestroy();
    }

    @Override
    public void finish() {
        Log.v(TAG, "finish");
        cleanupAvahiService();
        super.finish();
    }

    private AvahiService mAvahiService;

    private ServiceConnection mAvahiServiceConnection = null;

    private void setupAvahiService() {
        Log.v(TAG, "setupAvahiService");
        Intent bindIntent = new Intent(this, AvahiService.class);
        mAvahiServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.v(TAG, "onServiceConnected");
                mAvahiService = ((AvahiService.AvahiServiceBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.v(TAG, "onServiceDisconnected");
                mAvahiService = null;
            }
        };
        bindService(bindIntent, mAvahiServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void cleanupAvahiService() {
        Log.v(TAG, "cleanupAvahiService");
        if (mAvahiServiceConnection != null) {
            unbindService(mAvahiServiceConnection);
            mAvahiServiceConnection = null;
        }
    }
}