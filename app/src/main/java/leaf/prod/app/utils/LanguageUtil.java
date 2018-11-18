package leaf.prod.app.utils;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import leaf.prod.walletsdk.model.Language;

/**
 * 标题:    LanguageUtil
 * 版本:    V-1.0.0
 */
public class LanguageUtil {

    public static void changeLanguage(Context context, Language language) {
        Configuration configuration = context.getResources().getConfiguration();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        switch (language) {
            case zh_CN:
                configuration.locale = Locale.SIMPLIFIED_CHINESE;
                SPUtils.put(context, "language", 2);
                ThirdLoginUtil.updateLocal(context, Language.zh_CN, null);
                break;
            case zh_Hant:
                configuration.locale = Locale.TRADITIONAL_CHINESE;
                SPUtils.put(context, "language", 3);
                ThirdLoginUtil.updateLocal(context, Language.zh_Hant, null);
                break;
            case en_US:
                configuration.locale = Locale.US;
                SPUtils.put(context, "language", 1);
                ThirdLoginUtil.updateLocal(context, Language.en_US, null);
                break;
        }
        context.getResources().updateConfiguration(configuration, displayMetrics);
    }

    /**
     * 获得当前系统的版本
     */
    public static Language getLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String languageDisplayName = locale.getDisplayName();
        if (languageDisplayName.contains("English")) {
            return Language.en_US;
        } else if (languageDisplayName.contains("Taiwan")) {
            return Language.zh_Hant;
        } else {
            return Language.zh_CN;
        }
    }

    public static Language getSettingLanguage(Context context) {
        /*
         * 通过language的状态来判断是否设置了显示英文还是中文，1为英文，2为中文,0为未设置，显示系统默认
         */
        switch ((int) SPUtils.get(context, "language", 0)) {
            case 1:
                return Language.en_US;
            case 2:
                return Language.zh_CN;
            case 3:
                return Language.zh_Hant;
            default:
                return getLanguage(context);
        }
    }
}
