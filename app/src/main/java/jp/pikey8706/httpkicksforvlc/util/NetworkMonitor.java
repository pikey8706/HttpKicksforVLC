package aaa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.net.Inet4Address;
import java.net.InetAddress;

public class NetworkMonitor {
    public static final String TAG = "NetworkMonitor";

    private final Context mAppContext;
    private final ConnectivityManager mConnectivityManager;
    private final DefaultNetworkCallback mDefaultNetworkCallback;
    private final ConnectivityReceiver mConnectivityReceiver;

    private final Object mNetworkEventLock = new Object();

    private Network mCurrentNetwork;

    private boolean mIsMobile;
    private boolean mIsWifi;
    private boolean mIsVpn;
    private boolean mIsConnected;
    private InetAddress mLocalAddress = null;

    // NetworkCallback で モバイル接続中の、VPN接続、切断のイベントが取得できないOSバージョン.
    private final int[] mVpnOverMobileNotDetectingOS = new int[]{
            Build.VERSION_CODES.R // Android11
    };

    public NetworkMonitor(@NonNull Context context) {
        mAppContext = context.getApplicationContext();
        mConnectivityManager = (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mDefaultNetworkCallback = new DefaultNetworkCallback(this);
        mConnectivityReceiver = new ConnectivityReceiver(this);
    }

    public void start() {
        mConnectivityManager.registerDefaultNetworkCallback(mDefaultNetworkCallback);
        mAppContext.registerReceiver(mConnectivityReceiver, new IntentFilter(APICompat.CONNECTIVITY_ACTION));
    }

    public void stop() {
        mConnectivityManager.unregisterNetworkCallback(mDefaultNetworkCallback);
        mAppContext.unregisterReceiver(mConnectivityReceiver);
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    private static class ConnectivityReceiver extends BroadcastReceiver {
        private final NetworkMonitor mNetworkMonitor;

        private ConnectivityReceiver(@NonNull NetworkMonitor networkMonitor) {
            mNetworkMonitor = networkMonitor;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && APICompat.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                Parcelable networkInfo = APICompat.getNetworkInfoParcelable(intent);
                Log.d(TAG, "onReceive networkInfo: " + networkInfo);
                mNetworkMonitor.onConnectivityChanged(networkInfo);
            }
        }
    }

    private static class DefaultNetworkCallback extends ConnectivityManager.NetworkCallback {
        private static final String TAG = "DefaultNetworkCallback";
        private final NetworkMonitor mNetworkMonitor;

        private Network mAvailableNetwork = null;
        private InetAddress mInetAddress = null;

        public DefaultNetworkCallback(@NonNull NetworkMonitor networkMonitor) {
            mNetworkMonitor = networkMonitor;
        }

        @Override
        public void onAvailable(@NonNull Network network) {
            Log.d(TAG, "onAvailable network: " + network);
            // 接続NWを保持
            mAvailableNetwork = network;
            // 端末ローカルアドレスをクリア
            mInetAddress = null;
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            // VPN接続あり、インターネット接続ありの場合
            boolean isPreviousVpnAndInternetConnected = mNetworkMonitor.mIsVpn && mNetworkMonitor.mIsConnected;

            // 接続できていたかをチェック
            mNetworkMonitor.checkCapabilityChanged("onCapabilitiesChanged network: " + network, networkCapabilities);

            // VPN接続あり、インターネット接続なしになった場合
            boolean isOnlyVpnConnected = mNetworkMonitor.mIsVpn && !mNetworkMonitor.mIsConnected;
            if (isPreviousVpnAndInternetConnected && isOnlyVpnConnected) {
                Log.i(TAG, "isOnlyVpnConnected true");
                onLost(network);
            }

            if (network.equals(mAvailableNetwork)) {
                checkAvailableNetwork();
            }
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            // 端末ローカルアドレスを取得
            mInetAddress = mNetworkMonitor.getInterfaceIPv4Address("onLinkPropertiesChanged network: " + network, linkProperties);

            if (network.equals(mAvailableNetwork)) {
                checkAvailableNetwork();
            }
        }

        private void checkAvailableNetwork() {
            // onAvailable コールバック後、MOBILEまたはWIFIに接続済み、端末ローカルアドレスが取得できている場合に、接続イベント通知する
            if (mAvailableNetwork != null && mNetworkMonitor.mIsConnected && mInetAddress != null) {
                if (mNetworkMonitor.mCurrentNetwork != null
                        && !mAvailableNetwork.equals(mNetworkMonitor.mCurrentNetwork)) {
                    // NW切断を通知
                    mNetworkMonitor.onNetworkDisconnected(mNetworkMonitor.mCurrentNetwork);
                }
                // ネットワーク接続イベントを通知
                mNetworkMonitor.onNetworkConnected(mAvailableNetwork, mInetAddress);

                // 接続イベントのNWをクリア
                mAvailableNetwork = null;
                // 端末ローカルアドレスをクリア
                mInetAddress = null;
            }
        }

        @Override
        public void onLost(@NonNull Network network) {
            Log.d(TAG, "onLost network: " + network);

            // 以前接続していたNWと同じNWで切断イベントがあった場合
            if (network.equals(mNetworkMonitor.mCurrentNetwork)) {
                // NW切断を通知
                mNetworkMonitor.onNetworkDisconnected(network);

                if (network.equals(mAvailableNetwork)) {
                    // 接続イベントのNWをクリア
                    mAvailableNetwork = null;
                    // 端末ローカルアドレスをクリア
                    mInetAddress = null;
                }
            }
        }
    }

    private void onNetworkConnected(@NonNull Network network, @NonNull InetAddress inetAddress) {
        // ネットワーク接続イベントを通知
        synchronized (mNetworkEventLock) {
            if (!network.equals(mCurrentNetwork) && !inetAddress.equals(mLocalAddress)) {
                Log.i(TAG, "onNetworkConnected network: " + network + " inetAddress: " + inetAddress);
                // 現在有効なネットワークを保持
                mCurrentNetwork = network;

                // 現在有効なアドレスを保持
                mLocalAddress = inetAddress;
            }
        }
    }

    private void onNetworkDisconnected(@NonNull Network network) {
        // ネットワーク切断イベントを通知
        synchronized (mNetworkEventLock) {
            Log.i(TAG, "onNetworkDisconnected network: " + network);
            // 現在有効なネットワークをクリア
            mCurrentNetwork = null;

            // 現在有効なアドレスをクリア
            mLocalAddress = null;
        }
    }

    private void checkCapabilityChanged(String tag, NetworkCapabilities networkCapabilities) {

        if (networkCapabilities != null) {
            mIsMobile = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            mIsWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            mIsVpn = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
            boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            boolean hasValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

            // MOBILE または WIFI に接続しているときのみ、接続中とする
            mIsConnected = (mIsMobile || mIsWifi);

            Log.d(TAG, tag
                    + " isConnected: " + mIsConnected
                    + " isMobile: " + mIsMobile
                    + " isWifi: " + mIsWifi
                    + " isVpn: " + mIsVpn
                    + " hasInternet: " + hasInternet
                    + " hasValidated: " + hasValidated
            );
        }
    }

    public void onConnectivityChanged(Parcelable networkInfoParcelable) {
        Log.i(TAG, "onConnectivityChanged networkInfo: " + networkInfoParcelable);

        if (!isVpnOverMobileNotDetectingOS()) {
            return;
        }

        if (APICompat.isConnected(networkInfoParcelable)) {
            if (APICompat.isVpn(networkInfoParcelable)) {
                if (mIsMobile) {
                    // モバイル接続かつVPN接続中の場合
                    checkCurrentNetwork();
                }
            }
        } else {
            if (APICompat.isVpn(networkInfoParcelable)) {
                // モバイル接続かつVPN接続中の場合
                // VPN接続状態で、機内モードONなどでNW切断を検知した場合は、VPN切断イベントかチェックする.
                if (mIsMobile) {
                    checkCurrentNetwork();
                }
            } else if (APICompat.isMobile(networkInfoParcelable)) {
                // モバイル接続かつVPN接続状態で、機内モードONなどでNW切断を検知した場合は、VPN切断イベントかチェックする.
                // WIFI接続かつVPN接続状態で、機内モードONなどでNW切断を検知した場合は、VPN切断イベントは、NetworkCallback側で検知する.
                if (mIsVpn) {
                    checkCurrentNetwork();
                }
            }
        }
    }

    // NetworkCallback で モバイル接続中の、VPN接続、切断のイベントが取得できないOSバージョンかどうかをチェックする.
    private boolean isVpnOverMobileNotDetectingOS() {
        for (int os : mVpnOverMobileNotDetectingOS) {
            if (Build.VERSION.SDK_INT == os) {
                return true;
            }
        }
        return false;
    }

    private void checkCurrentNetwork() {
        // 現在有効なNWを確認
        Network network = mConnectivityManager.getActiveNetwork();
        if (mCurrentNetwork != null && !mCurrentNetwork.equals(network)) {
            // 保持しているNWと現在有効なNWが異なる場合は、保持しているNWは切断通知する.
            onNetworkDisconnected(mCurrentNetwork);
        }
        if (network != null) {
            NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null) {
                checkCapabilityChanged("checkCurrentNetwork: " + network, networkCapabilities);
                LinkProperties linkProperties = mConnectivityManager.getLinkProperties(network);
                InetAddress inetAddress = getInterfaceIPv4Address("checkCurrentNetwork: " + network, linkProperties);
                if (mIsConnected && inetAddress != null) {
                    // 接続通知する.
                    onNetworkConnected(network, inetAddress);
                }
            }
        }
    }

    private InetAddress getInterfaceIPv4Address(String tag, LinkProperties linkProperties) {
        InetAddress interfaceAddress = null;
        StringBuilder stringBuilder = new StringBuilder(" inetAddress: ");
        if (linkProperties != null) {
            for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
                if (linkAddress != null) {
                    InetAddress inetAddress = linkAddress.getAddress();
                    if (inetAddress != null) {
                        stringBuilder.append(inetAddress.getHostAddress()).append(", ");
                        // ループバックでない、リンクローカルでない、ワイルドカードアドレスでない、
                        // IPv4アドレスのみ使用
                        if (inetAddress instanceof Inet4Address
                                && !inetAddress.isLoopbackAddress()
                                && !inetAddress.isLinkLocalAddress()
                                && !inetAddress.isAnyLocalAddress()) {
                            interfaceAddress = inetAddress;
                        }
                    }
                }
            }
        }
        Log.d(TAG, tag + stringBuilder);

        return interfaceAddress;
    }
}
