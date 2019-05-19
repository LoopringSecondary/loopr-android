package leaf.prod.walletsdk.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * SharedPreferences工具类
 */

public class SPUtils {

    /**
     * 保存在手机里面的文件名
     */
    public static final String FILE_NAME = "share_data";

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void put(Context context, String key, Object object) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (object == null) {
            editor.putString(key, "");
        } else if (object instanceof String) {
            String encodeString = MyBase64.encode(((String) object).getBytes());
            editor.putString(key, encodeString);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, new Gson().toJson(object));
        }
        SharedPreferencesCompat.commit(editor);
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object get(Context context, String key, Object defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (defaultObject instanceof String) {
            String str = sp.getString(key, (String) defaultObject);
            if (!sp.contains(key)) {
                return defaultObject;
            } else {
                String decodeString = null;
                decodeString = new String(MyBase64.decode(str), StandardCharsets.UTF_8);
                //			return sp.getString(key, (String) defaultObject);
                return decodeString;
            }
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.commit(editor);
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.commit(editor);
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getAll();
    }

    /**
     * 保存List
     *
     * @param tag
     * @param datalist
     */
    public static <T> void setDataList(Context context, String tag, List<T> datalist) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (null == datalist || datalist.size() <= 0)
            return;
        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        editor.putString(tag, strJson);
        editor.commit();
        SharedPreferencesCompat.commit(editor);
    }

    /**
     * 获取List
     *
     * @param tag
     * @return
     */
    public static <T> List<T> getTokenList(Context context, String tag) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        List<T> datalist = new ArrayList<T>();
        String strJson = sp.getString(tag, null);
        if (null == strJson) {
            datalist.add((T) "ETH");
            datalist.add((T) "WETH");
            datalist.add((T) "LRC");
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
        }.getType());
        return datalist;
    }

    /**
     * 获取List
     *
     * @param tag
     * @return
     */
    public static <T> List<T> getDataList(Context context, String tag, Class<T> cls) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        List<T> datalist = new ArrayList<T>();
        String strJson = sp.getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        JsonArray array = new JsonParser().parse(strJson).getAsJsonArray();
        for (JsonElement jsonElement : array) {
            datalist.add(gson.fromJson(jsonElement, cls));
        }
        return datalist;
    }

    /**
     * 获取bean
     *
     * @param context
     * @param tag
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T getBean(Context context, String tag, Class<T> cls) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(tag, null);
        if (json != null && !json.isEmpty()) {
            Gson gson = new Gson();
            return gson.fromJson(json, cls);
        }
        return null;
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author zhy
     */
    private static class SharedPreferencesCompat {

        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }

        public static void commit(SharedPreferences.Editor editor) {
            editor.commit();
        }
    }
}
