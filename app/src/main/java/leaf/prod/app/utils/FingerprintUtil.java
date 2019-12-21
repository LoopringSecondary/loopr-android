/**
 * Created with IntelliJ IDEA.
 * User: kenshin wangchen@loopring.org
 * Time: 2018-10-16 4:53 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.utils;

import android.content.Context;
import android.os.Build;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

public class FingerprintUtil {

    public static boolean isEnable(Context context) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManagerCompat manager = FingerprintManagerCompat.from(context);
            result = manager.isHardwareDetected() && manager.hasEnrolledFingerprints();
        }
        return result;
    }
}
