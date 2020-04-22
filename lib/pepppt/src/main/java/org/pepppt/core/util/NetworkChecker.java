package org.pepppt.core.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * Checks the connectivity of the app.
 */
public class NetworkChecker
{
    private static final  int SIMPLE_NETWORK_TYPE_WIFI = 1;
    private static final  int SIMPLE_NETWORK_TYPE_2G = 2;
    private static final  int SIMPLE_NETWORK_TYPE_3G = 3;
    private static final  int SIMPLE_NETWORK_TYPE_4G = 4;
    private static final  int SIMPLE_NETWORK_TYPE_5G = 5;
    private static final  int SIMPLE_NETWORK_TYPE_UNKNOWN = 0;

    public static boolean isConnected(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Network nw = cm.getActiveNetwork();
            NetworkCapabilities actNw = cm.getNetworkCapabilities(nw);
            if (actNw!=null)
            {
                return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
            }
        }
        else
        {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return  (info != null && info.isConnected());
        }
        return false;
    }
    public static boolean isConnectedFast(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            Network nw = cm.getActiveNetwork();
            if (nw!=null)
            {
                NetworkCapabilities actNw = cm.getNetworkCapabilities(nw);
                if (actNw!=null)
                {
                    //TODO: check NetworkSpeed
                    //actNw.getLinkUpstreamBandwidthKbps();
                    //actNw.getLinkDownstreamBandwidthKbps()
                    return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                }
            }
        }
        else
        {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if ((info != null && info.isConnected()))
            {
                switch (getSimpleNetworkType(info.getType(), info.getSubtype()))
                {
                    case SIMPLE_NETWORK_TYPE_WIFI:
                    case SIMPLE_NETWORK_TYPE_5G:
                    case SIMPLE_NETWORK_TYPE_4G:
                        return true;
                    case SIMPLE_NETWORK_TYPE_3G:
                    case SIMPLE_NETWORK_TYPE_2G:
                    case SIMPLE_NETWORK_TYPE_UNKNOWN:
                        return false;
                }
            }
        }
        return false;
    }

    private static int getSimpleNetworkType(int type, int subType)
    {
        if (type == ConnectivityManager.TYPE_WIFI)
        {
            return SIMPLE_NETWORK_TYPE_WIFI;
        }
        else if (type == ConnectivityManager.TYPE_MOBILE)
        {
            switch (subType)
            {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return SIMPLE_NETWORK_TYPE_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return SIMPLE_NETWORK_TYPE_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return SIMPLE_NETWORK_TYPE_4G;
                case TelephonyManager.NETWORK_TYPE_NR:
                    return SIMPLE_NETWORK_TYPE_5G;
                default:
                    return SIMPLE_NETWORK_TYPE_UNKNOWN;
            }
        }
        else
        {
            return SIMPLE_NETWORK_TYPE_UNKNOWN;
        }
    }
}
