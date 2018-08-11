package max.pawras.bitbucket.auth;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import max.pawras.bitbucket.R;

import static android.content.Context.WIFI_SERVICE;

public class AuthUtils  {


    //it will do email validation
    public static boolean isEmailValid(String email) {
        boolean isValid = false;
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    //it will check the internet connection
    public static boolean haveNetworkConnection(Context mContext) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static String getMacAddress(Context mContext) {
        //getting macAddress
        WifiManager mWifiManager;
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo info = mWifiManager.getConnectionInfo();
        String mac_address = info.getMacAddress();

        //replace ':' with '-' in macaddress
        char[] charArray = mac_address.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == ':') {
                charArray[i] = '-';
            }
        }
        mac_address=String.valueOf(charArray);
        Log.d("macaddressformat",mac_address);
        return mac_address;
    }


}
