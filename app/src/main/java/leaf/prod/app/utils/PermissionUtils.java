package leaf.prod.app.utils;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-10-31 下午4:50
 * Cooperation: Loopring
 */
public class PermissionUtils {

    public static void initPermissions(Context context) {
        /**
         //		 * 6.0系统 获取权限
         //		 */
        List<String> list = new ArrayList<>();
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            list.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            list.add(Manifest.permission.CAMERA);
        }
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)) {
            list.add(Manifest.permission.READ_CONTACTS);
        }
        //        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)) {
        //            list.add(Manifest.permission.CALL_PHONE);
        //        }
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.READ_LOGS)) {
            list.add(Manifest.permission.READ_LOGS);
        }
        //        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)) {
        //            list.add(Manifest.permission.READ_PHONE_STATE);
        //        }
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.SET_DEBUG_APP)) {
            list.add(Manifest.permission.SET_DEBUG_APP);
        }
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            list.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
        }
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)) {
            list.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
        }
        if (list.size() > 0) {
            String[] mPermissionList = list.toArray(new String[]{});
            ActivityCompat.requestPermissions((Activity) context, mPermissionList, 100);
        }
    }
}
