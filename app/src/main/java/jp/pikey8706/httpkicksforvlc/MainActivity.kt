package jp.pikey8706.httpkicksforvlc

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import jp.pikey8706.httpkicksforvlc.AvahiService.AvahiServiceBinder
import jp.pikey8706.httpkicksforvlc.kicks.Constants
import jp.pikey8706.httpkicksforvlc.kicks.Utility
import jp.pikey8706.httpkicksforvlc.kicks.XHandler
import jp.pikey8706.httpkicksforvlc.ui.main.TunerPagerAdapter
import java.util.*

open class MainActivity : AppCompatActivity(), AvahiService.OnDnsResolvedListener {
    private var mHandler: Handler? = null

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
        mHandler = Handler(Looper.getMainLooper())
        setupAvahiService()

        Constants.init()

        XHandler.instance?.onX();
        XHandler.instance?.onY();
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
                mAvahiService?.mDnsResolvedListener = this@MainActivity
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

    fun getHostAddressFromName(name: String): String? {
        var hostAddress: String?
        hostAddress = mAvahiService?.resolveServiceAddress(name)
        Log.v(TAG, "getHostAddressFromName: $name : $hostAddress")
        return hostAddress
    }

    companion object : MainActivity() {
        private const val TAG = "MainActivity"
    }

    override fun onDnsResolved() {
        Log.v(TAG, "onDnsResolved")
        var sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        var index = 0
        val hostNameKeys: Array<String> = (Constants.KEY_HOST_NAMES_TS + Constants.KEY_HOST_NAMES_BS)
        val hostKeys: Array<String> = (Constants.KEY_HOSTS_TS + Constants.KEY_HOSTS_BS)
        for (hostNameKey:String in hostNameKeys) {
            Log.v(TAG, "resolve: $hostNameKey")
            var hostName: String? = Utility.loadPref(hostNameKey, null, sharedPrefs)
            if (hostName != null) {
                var hostKey: String = hostKeys[index]
                var hostPortAddress: String = Utility.loadPref(hostKey, null, sharedPrefs)
                var address: String? = getHostAddressFromName(hostName)
                var port = Utility.getPortPart(hostPortAddress)
                if (address != null) {
                    hostPortAddress = Utility.getHttpHostAddress(address, port)
                    Log.v(TAG, "onDnsResolved key: $hostKey savePref: $hostPortAddress index: $index")
                    Utility.savePref(hostKey, hostPortAddress, sharedPrefs)
                }
            }
            index++
        }
    }
}