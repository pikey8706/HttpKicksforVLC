package jp.pikey8706.httpkicksforvlc

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import jp.pikey8706.httpkicksforvlc.AvahiService.AvahiServiceBinder
import jp.pikey8706.httpkicksforvlc.ui.main.TunerPagerAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tunerPagerAdapter = TunerPagerAdapter(this, supportFragmentManager)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        viewPager.adapter = tunerPagerAdapter
        val tabs = findViewById<TabLayout>(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        setupAvahiService()
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        cleanupAvahiService()
        super.onDestroy()
    }

    override fun finish() {
        Log.v(TAG, "finish")
        cleanupAvahiService()
        super.finish()
    }

    private var mAvahiService: AvahiService? = null
    private var mAvahiServiceConnection: ServiceConnection? = null
    private fun setupAvahiService() {
        Log.v(TAG, "setupAvahiService")
        val bindIntent = Intent(this, AvahiService::class.java)
        mAvahiServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Log.v(TAG, "onServiceConnected")
                mAvahiService = (service as AvahiServiceBinder).service
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Log.v(TAG, "onServiceDisconnected")
                mAvahiService = null
            }
        }
        bindService(bindIntent, (mAvahiServiceConnection as ServiceConnection), BIND_AUTO_CREATE)
    }

    private fun cleanupAvahiService() {
        Log.v(TAG, "cleanupAvahiService")
        if (mAvahiServiceConnection != null) {
            unbindService(mAvahiServiceConnection!!)
            mAvahiServiceConnection = null
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}