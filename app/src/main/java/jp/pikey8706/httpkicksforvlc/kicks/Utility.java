package jp.pikey8706.httpkicksforvlc.kicks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Set;

public class Utility {
    public static final String TAG = Utility.class.getSimpleName();

    public static void startMPEGHttpForVlc(Context context, String url) {
        final String mediaType = "video/mpeg";
//        mediaType = "application/octet-stream";
//        mediaType = "video/mpeg";
        // mediaType = "video/mp2t";
        startViewUrl(context, url, mediaType);
    }

    public static void startViewUrl(Context context, String url, String mediaType) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setDataAndType(uri, mediaType);

        context.startActivity(intent);
        Log.v(TAG, "startViewUrl uri: " + uri + " type: " + mediaType);
    }

    public static String getHttpHostAddress(String host, String port) {
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(port)) {
            return "";
        }
        return Constants.PROTOCOL_HTTP + host + ":" + port;
    }

    public static String getHostPart(String hostPortAddress) {
        String hostPart = hostPortAddress;
        if (!TextUtils.isEmpty(hostPart)) {
            if (hostPart.startsWith(Constants.PROTOCOL_HTTP)) {
                hostPart = hostPart.substring(Constants.PROTOCOL_HTTP.length());
            }
            int lastIndex = hostPart.lastIndexOf(":");
            if (lastIndex > -1) {
                hostPart = hostPart.substring(0, lastIndex);
            }
        }
        return hostPart;
    }

    public static String getPortPart(String hostPortAddress) {
        String portPart = hostPortAddress;
        if (!TextUtils.isEmpty(portPart)) {
            if (portPart.startsWith(Constants.PROTOCOL_HTTP)) {
                portPart = portPart.substring(Constants.PROTOCOL_HTTP.length());
            }
            int lastIndex = portPart.lastIndexOf(":");
            if (lastIndex > -1) {
                portPart = portPart.substring(lastIndex + 1);
            }
        }
        return portPart;
    }

    public static String getChannelNameAndId(String channelName, String channelId) {
        if (TextUtils.isEmpty(channelName) || TextUtils.isEmpty(channelId)) {
            return "";
        }
        return channelName + ":" + channelId;
    }

    public static String getChannelNamePart(String channelNameAndId) {
        String channelNamePart = channelNameAndId;
        if (!TextUtils.isEmpty(channelNamePart)) {
            int lastIndex = channelNamePart.lastIndexOf(":");
            if (lastIndex > -1) {
                channelNamePart = channelNamePart.substring(0, lastIndex);
            }
        }
        return channelNamePart;
    }

    public static String getChannelIdPart(String channelNameAndId) {
        String channelIdPart = channelNameAndId;
        if (!TextUtils.isEmpty(channelIdPart)) {
            int lastIndex = channelIdPart.lastIndexOf(":");
            if (lastIndex > -1) {
                channelIdPart = channelIdPart.substring(lastIndex + 1);
            }
        }
        return channelIdPart;
    }

    public static void savePref(String key, int value, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, Integer.valueOf(value));
        editor.commit();
    }

    public static void savePref(String key, String value, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, String.valueOf(value));
        editor.commit();
    }

    public static int loadPref(String key, int defaultValue, SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static String loadPref(String key, String defaultValue, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(key, defaultValue);
    }

    private static String getCSValuesFromStringArray(String[] values) {
        StringBuilder csValuesBuilder = new StringBuilder();
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                csValuesBuilder.append(value).append(",");
            }
        }
        return csValuesBuilder.toString();
    }

    private static String[] getStringArrayFromCSVString(String csValues) {
        String[] csValueArray = csValues.split(",");
        return csValueArray;
    }

    public static void savePrefAsCSV(String key, String[] values, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, getCSValuesFromStringArray(values));
        editor.commit();
    }

    public static String[] loadPrefFromCSV(String key, String[] defaultValues, SharedPreferences sharedPreferences) {
        String defaultCSValues = getCSValuesFromStringArray(defaultValues);
        String csValues = sharedPreferences.getString(key, defaultCSValues);
        return getStringArrayFromCSVString(csValues);
    }
}
