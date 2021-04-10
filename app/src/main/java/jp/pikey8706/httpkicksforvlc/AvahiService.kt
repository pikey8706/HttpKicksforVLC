package jp.pikey8706.httpkicksforvlc

import android.app.Service
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdServiceInfo
import android.os.Binder
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import jp.pikey8706.httpkicksforvlc.AvahiService
import java.util.*

class AvahiService : Service() {
    private var mServiceTypes: Array<String?>? = null
    private val TIME_TO_DISCOVER_SERVICES = 1000L * 1
    private val TIME_TO_RESOLVE_SERVICES = 1000L * 3
    private var mNsdManager: NsdManager? = null
    private var mDiscoveryListener: DiscoveryListener? = null
    private var mResolveListener: NsdManager.ResolveListener? = null
    private val mNsdDiscoveryServiceInfoList = ArrayList<NsdServiceInfo>()
    private val mNsdServiceInfoList = ArrayList<NsdServiceInfo>()
    private val mResolvedServiceInfoList = ArrayList<NsdServiceInfo>()
    private val mLockForStartOrStopForDiscover = Object()
    private val mLockForResolveListener = Object()
    private var isDiscoveryStarted = false

    inner class AvahiServiceBinder : Binder() {
        val service: AvahiService
            get() = this@AvahiService
    }

    private var mAvahiServiceBinder: AvahiServiceBinder? = null
    override fun onCreate() {
        Log.v(TAG, "onCreate")
        super.onCreate()
        mAvahiServiceBinder = AvahiServiceBinder()
        mNsdManager = getSystemService(NSD_SERVICE) as NsdManager
        startDiscovery()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.v(TAG, "onBind")
        return mAvahiServiceBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.v(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent) {
        Log.v(TAG, "onBind")
        super.onRebind(intent)
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun setupServiceTypes() {
        mServiceTypes = resources.getStringArray(R.array.service_types)
        val dnsServiceDiscoveryServiceTypes = getString(R.string.services_dns_sd_udo)
        val discoveryListener: DiscoveryListener = object : DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                notifyRelease(mLockForStartOrStopForDiscover)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                notifyRelease(mLockForStartOrStopForDiscover)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                mNsdManager!!.stopServiceDiscovery(this)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                notifyRelease(mLockForStartOrStopForDiscover)
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                mNsdDiscoveryServiceInfoList.add(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                mNsdDiscoveryServiceInfoList.remove(serviceInfo)
            }
        }
        mNsdManager!!.discoverServices(dnsServiceDiscoveryServiceTypes, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        waitFor(mLockForStartOrStopForDiscover, 0)
        waitFor(mLockForResolveListener, TIME_TO_DISCOVER_SERVICES)
        mNsdManager!!.stopServiceDiscovery(discoveryListener)
        waitFor(mLockForStartOrStopForDiscover, 0)
        if (mNsdDiscoveryServiceInfoList.size > 0) {
            val serviceTypeList = ArrayList<String>()
            for (nsdServiceInfo in mNsdDiscoveryServiceInfoList) {
                val name = nsdServiceInfo.serviceName
                val type = nsdServiceInfo.serviceType
                if ("_tcp.local." == type) {
                    val typeName = "$name._tcp"
                    serviceTypeList.add(typeName)
                }
            }
            if (serviceTypeList.size > 0) {
                mServiceTypes = arrayOfNulls(serviceTypeList.size)
                serviceTypeList.toArray(mServiceTypes)
            }
        }
    }

    private fun startDiscovery() {
        Thread(Runnable {
            setupServiceTypes()
            if (mServiceTypes == null || mServiceTypes!!.size == 0) {
                Log.d(TAG, "Service type is not specified")
                return@Runnable
            }
            initializeDiscoveryListener()
            for (serviceType in mServiceTypes!!) {
                if (TextUtils.isEmpty(serviceType)) {
                    continue
                }
                mNsdManager!!.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)

                // wait for discovery started.
                waitFor(mLockForStartOrStopForDiscover, 0)
                if (isDiscoveryStarted) {
                    // wait for service discovery (timeout).
                    waitFor(mLockForResolveListener, TIME_TO_DISCOVER_SERVICES)
                    mNsdManager!!.stopServiceDiscovery(mDiscoveryListener)

                    // wait for discovery stopped.
                    waitFor(mLockForStartOrStopForDiscover, 0)
                }
            }
            initializeResolveListener()
            for (serviceInfo in mNsdServiceInfoList) {
                mNsdManager!!.resolveService(serviceInfo, mResolveListener)
                waitFor(mLockForResolveListener, TIME_TO_RESOLVE_SERVICES)
            }
        }).start()
    }

    fun initializeDiscoveryListener() {
        mDiscoveryListener = object : DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started regType: $regType")
                isDiscoveryStarted = true
                notifyRelease(mLockForStartOrStopForDiscover)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.w(TAG, "Discovery failed: Error code:" + errorCode
                        + " for serviceType: " + serviceType)
                isDiscoveryStarted = false
                notifyRelease(mLockForStartOrStopForDiscover)
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                //見つかるのが<hostname>.local[<macアドレス>]みたいな感じだったのでcontainsで対応
                Log.i(TAG, "service found: " + service + " hash: " + service.hashCode())
                Log.d(TAG, "         name: " + service.serviceName + " type: " + service.serviceType
                        + "   host: " + service.host + " port: " + service.port)
                mNsdServiceInfoList.add(service)
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.w(TAG, "service lost: $service")
                mNsdServiceInfoList.remove(service)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped: $serviceType")
                notifyRelease(mLockForStartOrStopForDiscover)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.w(TAG, "Discovery failed: Error code:$errorCode")
                // try stop again.
                mNsdManager!!.stopServiceDiscovery(this)
            }
        }
    }

    fun initializeResolveListener() {
        mResolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                // Called when the resolve fails. Use the error code to debug.
                Log.w(TAG, "Resolve failed: " + errorCode
                        + " for serviceType: " + serviceInfo.serviceType
                        + " for serviceName: " + serviceInfo.serviceName)
                notifyRelease(mLockForResolveListener)
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "Resolve Succeeded. " + serviceInfo + " hash: " + serviceInfo.hashCode())

//                Log.d(TAG, "             name: " + serviceInfo.getServiceName() + " type: " + serviceInfo.getServiceType()
//                        + "   host: " + serviceInfo.getHost().getHostAddress() + " port: " + serviceInfo.getPort());
                mResolvedServiceInfoList.add(serviceInfo)
                notifyRelease(mLockForResolveListener)
            }
        }
    }

    private fun waitFor(lockObj: Object, timeout: Long) {
        synchronized(lockObj) {
            try {
                lockObj.wait(timeout)
            } catch (e: InterruptedException) {
                Log.w(TAG, "Failed to wait lock $lockObj")
            }
        }
    }

    private fun notifyRelease(lockObj: Object) {
        synchronized(lockObj) {
            try {
                lockObj.notify()
            } catch (e: IllegalMonitorStateException) {
                Log.w(TAG, "Failed to notify lock $lockObj")
            }
        }
    }

    companion object {
        private val TAG = AvahiService::class.java.simpleName
    }
}