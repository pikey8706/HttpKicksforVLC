package jp.pikey8706.httpkicksforvlc;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AvahiService extends Service {
    private static final String TAG = AvahiService.class.getSimpleName();

    private String mServiceTypes[] = null;

    private long TIME_TO_DISCOVER_SERVICES = 1000L * 1;
    private long TIME_TO_RESOLVE_SERVICES = 1000L * 3;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;

    private ArrayList<NsdServiceInfo> mNsdDiscoveryServiceInfoList = new ArrayList<>();
    private ArrayList<NsdServiceInfo> mNsdServiceInfoList = new ArrayList<>();
    private ArrayList<NsdServiceInfo> mResolvedServiceInfoList = new ArrayList<>();


    private Object mLockForStartOrStopForDiscover = new Object();
    private Object mLockForResolveListener = new Object();

    private boolean isDiscoveryStarted = false;

    public class AvahiServiceBinder extends Binder {
        public AvahiService getService() {
            return AvahiService.this;
        }
    }

    private AvahiServiceBinder mAvahiServiceBinder;

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate");
        super.onCreate();

        mAvahiServiceBinder = new AvahiServiceBinder();

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        startDiscovery();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return mAvahiServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(TAG, "onBind");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
    }

    private void setupServiceTypes() {
        mServiceTypes =
                getResources().getStringArray(R.array.service_types);

        String dnsServiceDiscoveryServiceTypes = getString(R.string.services_dns_sd_udo);
        NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String serviceType) {
                notifyRelease(mLockForStartOrStopForDiscover);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                notifyRelease(mLockForStartOrStopForDiscover);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                notifyRelease(mLockForStartOrStopForDiscover);
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                mNsdDiscoveryServiceInfoList.add(serviceInfo);
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                mNsdDiscoveryServiceInfoList.remove(serviceInfo);
            }
        };
        mNsdManager.discoverServices(dnsServiceDiscoveryServiceTypes, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        waitFor(mLockForStartOrStopForDiscover, 0);
        waitFor(mLockForResolveListener, TIME_TO_DISCOVER_SERVICES);
        mNsdManager.stopServiceDiscovery(discoveryListener);
        waitFor(mLockForStartOrStopForDiscover, 0);
        if (mNsdDiscoveryServiceInfoList.size() > 0) {
            ArrayList<String> serviceTypeList = new ArrayList<>();
            for (NsdServiceInfo nsdServiceInfo : mNsdDiscoveryServiceInfoList) {
                String name = nsdServiceInfo.getServiceName();
                String type = nsdServiceInfo.getServiceType();
                if ("_tcp.local.".equals(type)) {
                    String typeName = name + "." + "_tcp";
                    serviceTypeList.add(typeName);
                }
            }
            if (serviceTypeList.size() > 0) {
                mServiceTypes = new String[serviceTypeList.size()];
                serviceTypeList.toArray(mServiceTypes);
            }
        }
    }

    private void startDiscovery() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                setupServiceTypes();

                if (mServiceTypes == null || mServiceTypes.length == 0) {
                    Log.d(TAG, "Service type is not specified");
                    return;
                }

                initializeDiscoveryListener();

                for (String serviceType : mServiceTypes) {
                    if (TextUtils.isEmpty(serviceType)) {
                        continue;
                    }

                    mNsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

                    // wait for discovery started.
                    waitFor(mLockForStartOrStopForDiscover, 0);

                    if (isDiscoveryStarted) {
                        // wait for service discovery (timeout).
                        waitFor(mLockForResolveListener, TIME_TO_DISCOVER_SERVICES);

                        mNsdManager.stopServiceDiscovery(mDiscoveryListener);

                        // wait for discovery stopped.
                        waitFor(mLockForStartOrStopForDiscover, 0);
                    }
                }

                initializeResolveListener();

                for (NsdServiceInfo serviceInfo : mNsdServiceInfoList) {

                    mNsdManager.resolveService(serviceInfo, mResolveListener);

                    waitFor(mLockForResolveListener, TIME_TO_RESOLVE_SERVICES);
                }
            }
        }).start();
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started regType: " + regType);
                isDiscoveryStarted = true;
                notifyRelease(mLockForStartOrStopForDiscover);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.w(TAG, "Discovery failed: Error code:" + errorCode
                        + " for serviceType: " + serviceType);
                isDiscoveryStarted = false;
                notifyRelease(mLockForStartOrStopForDiscover);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                //見つかるのが<hostname>.local[<macアドレス>]みたいな感じだったのでcontainsで対応
                Log.i(TAG, "service found: " + service + " hash: " + service.hashCode());
                Log.d(TAG, "         name: " + service.getServiceName() + " type: " + service.getServiceType()
                        + "   host: " + service.getHost() + " port: " + service.getPort());

                mNsdServiceInfoList.add(service);
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.w(TAG, "service lost: " + service);
                mNsdServiceInfoList.remove(service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "Discovery stopped: " + serviceType);
                notifyRelease(mLockForStartOrStopForDiscover);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.w(TAG, "Discovery failed: Error code:" + errorCode);
                // try stop again.
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                Log.w(TAG, "Resolve failed: " + errorCode
                        + " for serviceType: " + serviceInfo.getServiceType()
                        + " for serviceName: " + serviceInfo.getServiceName());
                notifyRelease(mLockForResolveListener);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Resolve Succeeded. " + serviceInfo + " hash: " + serviceInfo.hashCode());

//                Log.d(TAG, "             name: " + serviceInfo.getServiceName() + " type: " + serviceInfo.getServiceType()
//                        + "   host: " + serviceInfo.getHost().getHostAddress() + " port: " + serviceInfo.getPort());
                mResolvedServiceInfoList.add(serviceInfo);
                notifyRelease(mLockForResolveListener);
            }
        };
    }

    private void waitFor(Object lockObj, long timeout) {
        synchronized (lockObj) {
            try {
                lockObj.wait(timeout);
            } catch (InterruptedException e) {
                Log.w(TAG, "Failed to wait lock " + lockObj);
            }
        }
    }

    private void notifyRelease(Object lockObj) {
        synchronized (lockObj) {
            try {
                lockObj.notify();
            } catch (IllegalMonitorStateException e) {
                Log.w(TAG, "Failed to notify lock " + lockObj);
            }
        }
    }
}
