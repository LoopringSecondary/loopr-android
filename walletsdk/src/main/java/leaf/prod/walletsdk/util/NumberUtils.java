package leaf.prod.walletsdk.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

/**
 * @author brucezee Jan 27, 2013 1:24:22 PM
 */
public class NumberUtils {

    /**
     * 由于Java的简单类型不能够精确的对浮点数进行运算，这个工具类提供精 确的浮点数运算，包括加减乘除和四舍五入。
     */
    // 默认除法运算精度
    private static final int DEF_DIV_SCALE = 10;

    /**
     * BIG_DECIMAL_ZERO = 0
     */
    public static BigDecimal BIG_DECIMAL_ZERO = new BigDecimal(0);

    /**
     * BIG_DECIMAL_ZERO_POINT = 0.002(千分之二)
     */
    public static BigDecimal BIG_DECIMAL_ZERO_POINT = new BigDecimal(0.002);

    /**
     * BIG_DECIMAL_TWO = 2
     */
    public static BigDecimal BIG_DECIMAL_TWO = new BigDecimal(2);

    /**
     * 保留整数
     *
     * @param d
     * @return
     */
    public static String numberformat2(Double d) {
        DecimalFormat fmt = new DecimalFormat("#,##0");
        return fmt.format(d);
    }

    /**
     * 保留一位小数
     *
     * @param d
     * @return
     */
    public static String numberformat1(Double d) {
        DecimalFormat fmt = new DecimalFormat("##0.0");
        return fmt.format(d);
    }

    /**
     * 保留2位小数
     *
     * @param d
     * @return
     */
    public static String numberformat(Double d) {
        DecimalFormat fmt = new DecimalFormat("#,##0.00");
        return fmt.format(d);
    }

    public static BigDecimal numberformat(BigDecimal d) {
        DecimalFormat fmt = new DecimalFormat("##0.00");
        return new BigDecimal(fmt.format(d.doubleValue()));
    }

    // 18 -> 10000000000000000000
    public static String toBigDecimal(int digit) {
        BigDecimal pow = BigDecimal.valueOf(Math.pow(10, digit));
        return pow.toPlainString();
    }

    // 10000000000000000000 -> 18
    public static int toDigitLength(String bigDecimal) {
        return bigDecimal.length() - 1;
    }

    public static int precision(double d) {
        // 0.0000001 -> 0
        // 0.01  -> 1
        // 0.1 -> 2
        // 100.34 -> 5
        // regex
        String s = new BigDecimal(d).toPlainString();
        if (d > 0 && d < 1) {
            Matcher m = Pattern.compile("(0\\.[0]*)").matcher(s);
            if (m.find()) {
                return 4 - m.group().length() > 0 ? (4 - m.group().length()) : 0;
            }
        } else if (d > 1) {
            return Pattern.compile("(\\.\\d*)").matcher(s).replaceAll("").length() + 2;
        }
        return 4;
    }

    public static int integralLength(double d) {
        int integer = (int) d;
        return new Integer(integer).toString().length();
    }

    public static String format1(double d, int precision) {
        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(precision);
        formater.setMinimumFractionDigits(precision);
        formater.setGroupingSize(0);
        formater.setRoundingMode(RoundingMode.FLOOR);
        return formater.format(d);
    }

    public static double format2(double d) {
        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(2);
        formater.setGroupingSize(0);
        formater.setRoundingMode(RoundingMode.FLOOR);
        return Double.parseDouble(formater.format(d));
    }

    public static BigDecimal format3(double d) {
        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(2);
        formater.setGroupingSize(0);
        formater.setRoundingMode(RoundingMode.FLOOR);
        return new BigDecimal(formater.format(d));
    }

    public static double format4(double d) {
        DecimalFormat df = new DecimalFormat("0.00");
        String ds = df.format(d);
        return Double.parseDouble(ds);
    }

    public static String format6(double d, int minPrecision, int maxPrecision) {
        DecimalFormat df = new DecimalFormat("0.00");
        df.setMinimumFractionDigits(minPrecision);
        df.setMaximumFractionDigits(maxPrecision);
        String ds = df.format(d);
        return ds;
    }

    public static String format7(double d, int minPrecision, int maxPrecision) {
        DecimalFormat formater = new DecimalFormat();
        formater.setMinimumFractionDigits(minPrecision);
        formater.setMaximumFractionDigits(maxPrecision);
        return formater.format(d);
    }

    public static Double parseDouble2(String value) {
        Double result = 0d;
        DecimalFormat formater = new DecimalFormat();
        try {
            Number number = formater.parse(value);
            result = number.doubleValue();
        } catch (ParseException e) {
        }
        return result;
    }

    public static String formatSix(String s1, String s2) {
        BigDecimal d1 = new BigDecimal(s1);
        BigDecimal d2 = new BigDecimal(s2);
        String ss = d1.multiply(d2).setScale(6, BigDecimal.ROUND_HALF_DOWN).toString();
        return ss;
    }

    public static String formatTwo(String s1, String s2) {
        BigDecimal d1 = new BigDecimal(s1);
        BigDecimal d2 = new BigDecimal(s2);
        String ss = d1.multiply(d2).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString();
        return ss;
    }

    public static int getInt(String str) {
        if (str == null || str.equals(""))
            return 0;
        int ret = 0;
        try {
            ret = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            ret = 0;
        }
        return ret;
    }

    public static boolean isDigitsWithNoZero(String money) {
        if (TextUtils.isEmpty(money)) {
            return false;
        }
        String[] a = money.split("\\.");
        if (a.length == 1) {
            return true;
        } else {
            return Integer.valueOf(a[1]) == 0;
        }
    }

    /**
     * 获取Integer对象的数值
     *
     * @param value Integer对象
     * @return value不为空返回value，否则返回0
     */
    public static Integer valueOf(Integer value) {
        return value != null ? value : 0;
    }

    /**
     * 获取Long对象的数值
     *
     * @param value Long对象
     * @return value不为空返回value，否则返回0
     */
    public static Long valueOf(Long value) {
        return value != null ? value : 0L;
    }

    /**
     * 获取Double对象的数值
     *
     * @param value Double对象
     * @return value不为空返回value，否则返回0
     */
    public static Double valueOf(Double value) {
        return value != null ? value : 0L;
    }

    /**
     * 获取Float对象的数值
     *
     * @param value Float对象
     * @return value不为空返回value，否则返回0
     */
    public static Float valueOf(Float value) {
        return value != null ? value : 0L;
    }

    /**
     * 将字符串转换为Integer类型对象
     *
     * @param str 数字字符串
     * @return 成功返回字符串对应的Integer对象，失败返回null
     */
    public static Integer intValue(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 将字符串转换为Long类型对象
     *
     * @param str 数字字符串
     * @return 成功返回字符串对应的Long对象，失败返回null
     */
    public static Long longValue(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 将字符串转换为Double类型对象
     *
     * @param str 数字字符串
     * @return 成功返回字符串对应的Double对象，失败返回null
     */
    public static Double doubleValue(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 将字符串转换为Float类型对象
     *
     * @param str 数字字符串
     * @return 成功返回字符串对应的Float对象，失败返回null
     */
    public static Float floatValue(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 判断两个数字是否相等
     *
     * @param value1 Integer对象1
     * @param value2 Integer对象2
     * @return 相等返回true，否则返回false
     */
    public static boolean isEqual(Integer value1, Integer value2) {
        if (value1 != null && value2 != null) {
            return value1.equals(value2);
        }
        return false;
    }

    public static boolean notEqual(Integer value1, Integer value2) {
        return !isEqual(value1, value2);
    }

    /**
     * 判断两个数字是否相等
     *
     * @param value1 Long对象1
     * @param value2 Long对象2
     * @return 相等返回true，否则返回false
     */
    public static boolean isEqual(Long value1, Long value2) {
        if (value1 != null && value2 != null) {
            return value1.equals(value2);
        }
        return false;
    }

    public static boolean notEqual(Long value1, Long value2) {
        return !isEqual(value1, value2);
    }

    /**
     * 判断两个数字是否相等
     *
     * @param value1 Double对象1
     * @param value2 Double对象2
     * @return 相等返回true，否则返回false
     */
    public static boolean isEqual(Double value1, Double value2) {
        if (value1 != null && value2 != null) {
            return value1.equals(value2);
        }
        return false;
    }

    public static boolean notEqual(Double value1, Double value2) {
        return !isEqual(value1, value2);
    }

    /**
     * 获取min到max的一个随机整数x，x>=min && x<=max
     *
     * @param min 随机数最小值
     * @param max 随机数最大值
     * @return 随机整数
     */
    public static int random(int min, int max) {
        return (int) ((max - min + 1) * Math.random()) + min;
    }

    public static double random(double min, double max) {
        return (max - min) * Math.random() + min;
    }

    public static long random(long min, long max) {
        return (long) ((max - min + 1) * Math.random()) + min;
    }

    public static Integer parseInt(String s) {
        if (s != null && s.length() > 0) {
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static Long parseLong(String s) {
        if (s != null && s.length() > 0) {
            try {
                return Long.parseLong(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0l;
    }

    public static Double parseDouble(String s) {
        if (s != null && s.length() > 0) {
            try {
                return Double.parseDouble(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0d;
    }

    public static Float parseFloat(String s) {
        if (s != null && s.length() > 0) {
            try {
                return Float.parseFloat(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0f;
    }

    /**
     * 获取一个列表（或数组）指定数量的随机索引
     *
     * @param size       列表（或数组）的大小
     * @param indexCount 需要生产的随机索引的数量
     */
    public static List<Integer> getRandomIndexList(int size, int indexCount) {
        List<Integer> indexList = new LinkedList<Integer>();
        List<Integer> allIndexList = new LinkedList<Integer>();
        for (int i = 0; i < size; i++) {
            allIndexList.add(i);
        }
        for (int i = 0; i < indexCount; i++) {
            int allIndexSize = allIndexList.size();
            if (allIndexSize == 0) {
                break;
            }
            indexList.add(allIndexList.remove(random(0, allIndexSize - 1)));
        }
        return indexList;
    }

    /**
     * 提供精确的加法运算。
     *
     * @param value1 被加数
     * @param value2 加数
     * @return 两个参数的和
     */
    public static double add(double value1, double value2) {
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.add(b2).doubleValue();
    }

    public static byte[] append(byte[] a1, byte[] a2) {
        byte[] a3 = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, a3, a1.length, a2.length);
        return a3;
    }

    /**
     * 提供精确的加法运算。
     *
     * @param values 加数
     * @return 所有参数的和
     */
    public static double addAll(double... values) {
        double total = 0;
        if (values.length > 0) {
            for (double value : values) {
                total = add(total, value);
            }
        }
        return total;
    }

    /**
     * 提供精确的减法运算。
     *
     * @param value1 被减数
     * @param value2 减数
     * @return 两个参数的差
     */

    public static double subtract(double value1, double value2) {
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param value1 被乘数
     * @param value2 乘数
     * @return 两个参数的积
     */

    public static double multiply(double value1, double value2) {
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算。由scale参数指 定精度，对结果四舍五入
     *
     * @param value1 被乘数
     * @param value2 乘数
     * @param scale  表示表示需要精确到小数点以后几位。
     * @return 两个参数的积
     */

    public static double multiply(double value1, double value2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return round(b1.multiply(b2).doubleValue(), scale);
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
     *
     * @param value1 被除数
     * @param value2 除数
     * @return 两个参数的商
     */

    public static double divide(double value1, double value2) {
        return divide(value1, value2, DEF_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
     *
     * @param value1 被除数
     * @param value2 除数
     * @param scale  表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */

    public static double divide(double value1, double value2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 提供精确的加法运算。
     *
     * @param value1 被加数
     * @param value2 加数
     * @return 两个参数的和
     */
    public static float add(float value1, float value2) {
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return b1.add(b2).floatValue();
    }

    /**
     * 提供精确的减法运算。
     *
     * @param value1 被减数
     * @param value2 减数
     * @return 两个参数的差
     */

    public static float subtract(float value1, float value2) {
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return b1.subtract(b2).floatValue();
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param value1 被乘数
     * @param value2 乘数
     * @return 两个参数的积
     */

    public static float multiply(float value1, float value2) {
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return b1.multiply(b2).floatValue();
    }

    /**
     * 提供精确的乘法运算。由scale参数指 定精度，对结果四舍五入
     *
     * @param value1 被乘数
     * @param value2 乘数
     * @param scale  表示表示需要精确到小数点以后几位。
     * @return 两个参数的积
     */

    public static float multiply(float value1, float value2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return round(b1.multiply(b2).floatValue(), scale);
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
     *
     * @param value1 被除数
     * @param value2 除数
     * @return 两个参数的商
     */

    public static float divide(float value1, float value2) {
        return divide(value1, value2, DEF_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
     *
     * @param value1 被除数
     * @param value2 除数
     * @param scale  表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */

    public static float divide(float value1, float value2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Float.toString(value1));
        BigDecimal b2 = new BigDecimal(Float.toString(value2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param value 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double value, int scale) {
        return rounding(value, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param value 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static float round(float value, int scale) {
        return rounding(value, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 提供精确的小数位"入"处理。
     *
     * @param value 需要"入"的数字
     * @param scale 小数点后保留几位
     * @return "入"后的结果
     */
    public static double roundUp(double value, int scale) {
        return rounding(value, scale, BigDecimal.ROUND_UP);
    }

    /**
     * 提供精确的小数位"入"处理。
     *
     * @param value 需要"入"的数字
     * @param scale 小数点后保留几位
     * @return "入"后的结果
     */
    public static float roundUp(float value, int scale) {
        return rounding(value, scale, BigDecimal.ROUND_UP);
    }

    /**
     * 提供精确的小数位"舍"处理。
     *
     * @param value 需要"舍"的数字
     * @param scale 小数点后保留几位
     * @return "舍"后的结果
     */
    public static double roundDown(double value, int scale) {
        return rounding(value, scale, BigDecimal.ROUND_DOWN);
    }

    /**
     * 提供精确的小数位"舍"处理。
     *
     * @param value 需要"舍"的数字
     * @param scale 小数点后保留几位
     * @return "舍"后的结果
     */
    public static float roundDown(float value, int scale) {
        return rounding(value, scale, BigDecimal.ROUND_DOWN);
    }

    /**
     * 提供精确的小数位处理。
     *
     * @param value        需要处理的数字
     * @param scale        小数点后保留几位
     * @param roundingMode 处理模式
     * @return "处理后的结果
     */
    private static double rounding(double value, int scale, int roundingMode) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(value));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, roundingMode).doubleValue();
    }

    /**
     * 提供精确的小数位处理。
     *
     * @param value        需要处理的数字
     * @param scale        小数点后保留几位
     * @param roundingMode 处理模式
     * @return "处理后的结果
     */
    private static float rounding(float value, int scale, int roundingMode) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Float.toString(value));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, roundingMode).floatValue();
    }
}
