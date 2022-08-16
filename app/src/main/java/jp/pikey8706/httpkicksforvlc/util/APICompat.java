package util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Parcelable;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * 新しいOS用のAPI使用と、旧OS用のAPI使用を使い分けるためのユーティリティクラス
 */
public class APICompat {
    private static final String TAG = "APICompat";

    private static final String CONST_DEPRECATION = "deprecation";

    @SuppressWarnings(CONST_DEPRECATION)
    public static final String CONNECTIVITY_ACTION = ConnectivityManager.CONNECTIVITY_ACTION;

    public static Vibrator getVibratorCompat(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager =
                    (context != null) ?
                            (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                            : null;
            return (vibratorManager != null) ?
                    vibratorManager.getDefaultVibrator()
                    : null;
        } else {
            return APICompat.getVibratorForOlderOS(context);
        }
    }

    @SuppressWarnings(CONST_DEPRECATION)
    private static Vibrator getVibratorForOlderOS(Context context) {
        return (context != null) ?
                (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)
                : null;
    }

    @SuppressWarnings(CONST_DEPRECATION)
    public static List<ActivityManager.RunningServiceInfo> getRunningServiceInfo(ActivityManager activityManager, int maxNum) {
        return (activityManager != null) ?
                activityManager.getRunningServices(maxNum) : null;
    }

    /**
     * ロック中に画面表示するフラグセットなどを行う
     * @param activity 表示する Activity
     * @param show ロック中に画面表示するかどうか
     */
    public static void setShowActivity(@NonNull Activity activity, boolean show) {
        activity.setTurnScreenOn(show);
        activity.setShowWhenLocked(show);
        if (show) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * ｽﾃｰﾀｽﾊﾞｰ透過&ﾅﾋﾞｹﾞｰｼｮﾝﾊﾞｰ非表示ｼｽﾃﾑUIﾌﾗｸﾞset. //chg [MFTAP_Android11] SAE 2021/05/10
     * @param window 表示対象画面オブジェクト
     */
    public static void setHideNavigationUiFlag(Window window) {
        if (window == null) {
            LogWrapper.i(LogWrapper.TYPE_MAIN, TAG, "setHideNavigationUiFlag: decorView=null");
            return;
        }
        View decorView = window.getDecorView();

        //ｽﾃｰﾀｽﾊﾞｰ透過&ﾅﾋﾞｹﾞｰｼｮﾝﾊﾞｰ非表示
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView.getWindowInsetsController().hide(
                    WindowInsets.Type.navigationBars()
            );
            decorView.getWindowInsetsController().setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            window.setDecorFitsSystemWindows(false);
        } else {
            setHideNavigationUiFlagForOlderOS(decorView);
        }
    }

    @SuppressWarnings(CONST_DEPRECATION)
    private static void setHideNavigationUiFlagForOlderOS(@NonNull View decorView) {
        int flags = 0;
        flags = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //ｼｽﾃﾑﾊﾞｰ非表示
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        LogWrapper.i(LogWrapper.TYPE_MAIN, TAG, "setHideNavigationUiFlag = " + Integer.toHexString(flags));

        //ｽﾃｰﾀｽﾊﾞｰ透過&ﾅﾋﾞｹﾞｰｼｮﾝﾊﾞｰ表示
        decorView.setSystemUiVisibility(flags);
    }

    @SuppressWarnings(CONST_DEPRECATION)
    public static ProgressDialog createProgressDialog(@NonNull Activity activity, @NonNull String message) {
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMessage(message);
        // プログレスダイアログのスタイルを円スタイルに設定
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @SuppressWarnings(CONST_DEPRECATION)
    public static Parcelable getNetworkInfoParcelable(@NonNull Intent intent) {
        return intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
    }

    @SuppressWarnings(CONST_DEPRECATION)
    public static boolean isConnected(Parcelable networkInfoParcelable) {
        NetworkInfo networkInfo = (NetworkInfo) networkInfoParcelable;
        return networkInfo != null && networkInfo.isConnected();
    }

    @SuppressWarnings(CONST_DEPRECATION)
    public static boolean isMobile(Parcelable networkInfoParcelable) {
        NetworkInfo networkInfo = (NetworkInfo) networkInfoParcelable;
        return networkInfo != null && (ConnectivityManager.TYPE_MOBILE == networkInfo.getType());
    }

    @SuppressWarnings(CONST_DEPRECATION)
    public static boolean isVpn(Parcelable networkInfoParcelable) {
        NetworkInfo networkInfo = (NetworkInfo) networkInfoParcelable;
        return networkInfo != null && (ConnectivityManager.TYPE_VPN == networkInfo.getType());
    }
}
