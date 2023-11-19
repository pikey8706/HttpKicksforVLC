package jp.pikey8706.httpkicksforvlc

import android.app.Service
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import java.net.Inet4Address
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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
    private val mLockForDiscoverService = ReentrantLock()
    private val mLockConditionForDiscoverService = mLockForDiscoverService.newCondition()
    private var isDiscoveryStarted = false
    var mDnsResolvedListener : OnDnsResolvedListener? = null

    interface OnDnsResolvedListener {
        fun onDnsResolved()
    }

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
                notifyRelease()
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                notifyRelease()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                mNsdManager!!.stopServiceDiscovery(this)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                notifyRelease()
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                mNsdDiscoveryServiceInfoList.add(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                mNsdDiscoveryServiceInfoList.remove(serviceInfo)
            }
        }
        mNsdManager!!.discoverServices(dnsServiceDiscoveryServiceTypes, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        waitFor()
        waitFor(TIME_TO_DISCOVER_SERVICES)
        mNsdManager!!.stopServiceDiscovery(discoveryListener)
        waitFor()
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
            if (mServiceTypes == null || mServiceTypes!!.isEmpty()) {
                Log.d(TAG, "Service type is not specified")
                return@Runnable
            }
            initializeDiscoveryListener()
            for (serviceType in mServiceTypes!!) {
                if (TextUtils.isEmpty(serviceType)) {
                    continue
                }
                Log.d(TAG, "discoverServices: " + serviceType)
                mNsdManager!!.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)

                // wait for discovery started.
                waitFor()
                if (isDiscoveryStarted) {
                    // wait for service discovery (timeout).
                    waitFor(TIME_TO_DISCOVER_SERVICES)
                    mNsdManager!!.stopServiceDiscovery(mDiscoveryListener)

                    // wait for discovery stopped.
                    waitFor()
                    Log.d(TAG, "stopServiceDiscovery: " + serviceType)
                }
            }
            initializeResolveListener()
            for (serviceInfo in mNsdServiceInfoList) {
                mNsdManager!!.resolveService(serviceInfo, mResolveListener)
                waitFor(TIME_TO_RESOLVE_SERVICES)
            }
            mDnsResolvedListener?.onDnsResolved();
        }).start()
    }

    fun initializeDiscoveryListener() {
        mDiscoveryListener = object : DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "onDiscoveryStarted regType: $regType")
                isDiscoveryStarted = true
                notifyRelease()
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.w(TAG, "onStartDiscoveryFailed: Error code:" + errorCode
                        + " for serviceType: " + serviceType)
                isDiscoveryStarted = false
                notifyRelease()
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                //見つかるのが<hostname>.local[<macアドレス>]みたいな感じだったのでcontainsで対応
                Log.i(TAG, "onServiceFound: " + service + " hash: " + service.hashCode())
                Log.d(TAG, "          name: " + service.serviceName + " type: " + service.serviceType
                        + "   host: " + service.host + " port: " + service.port)
                mNsdServiceInfoList.add(service)
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.w(TAG, "onServiceLost: $service")
                mNsdServiceInfoList.remove(service)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "onDiscoveryStopped: $serviceType")
                notifyRelease()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.w(TAG, "onStopDiscoveryFailed: Error code:$errorCode")
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
                notifyRelease()
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "Resolve Succeeded. " + serviceInfo + " hash: " + serviceInfo.hashCode())

//                Log.d(TAG, "             name: " + serviceInfo.getServiceName() + " type: " + serviceInfo.getServiceType()
//                        + "   host: " + serviceInfo.getHost().getHostAddress() + " port: " + serviceInfo.getPort());
                mResolvedServiceInfoList.add(serviceInfo)
                notifyRelease()
            }
        }
    }

    private fun waitFor() {
        mLockForDiscoverService.withLock {
            try {
                mLockConditionForDiscoverService.await()
            } catch (e: InterruptedException) {
                Log.w(TAG, "Failed to wait lock.")
            }
        }
    }

    private fun waitFor(timeout: Long) {
        mLockForDiscoverService.withLock {
            try {
                mLockConditionForDiscoverService.await(timeout, TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                Log.w(TAG, "Failed to wait lock.")
            }
        }
    }

    private fun notifyRelease() {
        mLockForDiscoverService.withLock {
            mLockConditionForDiscoverService.signal()
        }
    }

    fun resolveServiceAddress(hostName: String): String? {
        Log.v(TAG, "resolveServiceAddress: $hostName")
        var hostAddress: String? = null
        for (serviceInfo in mResolvedServiceInfoList) {
            Log.v(TAG, "serviceName:: " + serviceInfo.serviceName)
            if (hostName.equals(serviceInfo.serviceName, true)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    for (hostAddressOne in serviceInfo.hostAddresses) {
                        if (hostAddressOne is Inet4Address) {
                            hostAddress = hostAddressOne.hostAddress
                            break
                        }
                    }
                } else {
                    hostAddress = serviceInfo.host.hostAddress
                }
            }
        }
        return hostAddress
    }

    companion object {
        private val TAG = AvahiService::class.java.simpleName
    }
}