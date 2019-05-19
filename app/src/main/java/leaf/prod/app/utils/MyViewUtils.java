package leaf.prod.app.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 主要是一些通用的界面需要用的方法
 */
public class MyViewUtils {

    /**
     * 隐藏视图
     */
    public static void hideView(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        } else {
            throw new IllegalArgumentException("A view is null in your parameter.");
        }
    }

    /**
     * 隐藏视图
     */
    public static void setViewInvisible(View view) {
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
        } else {
            throw new IllegalArgumentException("A view is null in your parameter.");
        }
    }

    /**
     * 显示视图
     */
    public static void showView(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        } else {
            throw new IllegalArgumentException("A view is null in your parameters.");
        }
    }

    /**
     * 将各个视图显示
     *
     * @param viewIds 视图控件数组
     */
    public static void showViews(Activity activity, int... viewIds) {
        View view = null;
        for (int id : viewIds) {
            view = activity.findViewById(id);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            } else {
                throw new IllegalArgumentException("A view is not found in current activity.");
            }
        }
    }

    /**
     * 将各个视图显示
     */
    public static void showViews(View... views) {
        for (View view : views) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            } else {
                throw new IllegalArgumentException("A view is null in your parameters.");
            }
        }
    }

    /**
     * 给图标设置级别
     */
    public static void setImageLevel(ImageView imageView, Integer level) {
        if (imageView != null) {
            if (level == null)
                level = 0;
            imageView.setImageLevel(level);
        } else {
            throw new IllegalArgumentException("A view is null in your parameters.");
        }
    }

    /**
     * 给文本设置文字
     */
    public static void setText(TextView textView, String text) {
        if (textView != null) {
            if (text == null)
                text = "";
            textView.setText(text);
        } else {
            throw new IllegalArgumentException("A view is null in your parameters.");
        }
    }

    /**
     * 给图标设置内容
     */
    public static void setDrawable(ImageView imageView, Drawable drawable) {
        if (imageView != null && drawable != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * 清除所有TextView的文字内容
     */
    public static void clearTextViews(TextView... textViews) {
        for (TextView textView : textViews) {
            textView.setText("");
        }
    }

    /**
     * 取消某些手机系统里listview滚动到顶部和底部时的回弹效果
     */
    @TargetApi(9)
    public static void disableOverScroll(AbsListView listView) {
        if (Build.VERSION.SDK_INT >= 9) {// Build.VERSION_CODES.GINGERBREAD) {
            listView.setOverScrollMode(2);// ListView.OVER_SCROLL_NEVER);
        }
    }

    /**
     * 取消某些手机系统里scrollView滚动到顶部和底部时的回弹效果
     */
    @TargetApi(9)
    public static void disableOverScroll(ScrollView scrollView) {
        if (Build.VERSION.SDK_INT >= 9) {// Build.VERSION_CODES.GINGERBREAD) {
            scrollView.setOverScrollMode(2);// ScrollView.OVER_SCROLL_NEVER);
        }
    }

    /**
     * 全屏显示
     */
    public static void setFullScreen(Activity activity) {
        Window window = activity.getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 隐藏软键盘
     */
    public static void hideInput(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) view.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 弹出软键盘
     */
    public static void showInput(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) view.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果软键盘没有显示则显示，否则隐藏
     */
    public static void toggleInput(Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 弹出软键盘，并选中内容
     */
    public static void showInputAndSelectAll(View view) {
        try {
            view.requestFocus();
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                editText.setCursorVisible(true);
                int editTextLength = editText.getText().toString().length();
                editText.setSelection(editTextLength);
            }
            InputMethodManager imm = (InputMethodManager) view.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 给EditText设置文字，并将光标移到文字后面
     */
    public static void setEditText(EditText editText, String text) {
        if (editText != null && text != null) {
            editText.setText(text);
            Selection.setSelection(editText.getText(), text.length());
        }
    }

    /**
     * 获取屏幕尺寸，返回屏幕尺寸宽高数组
     */
    public static int[] getScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        if (screenHeight > screenWidth) {
            return new int[]{screenWidth, screenHeight};
        }
        return new int[]{screenHeight, screenWidth};
    }

    /**
     * 设置EditText的hint的文字大小
     *
     * @param editText     EditText
     * @param hintText     hint的文字
     * @param hintTextSize hint文字的大小
     */
    public static void setEditTextHint(EditText editText, String hintText, int hintTextSize) {
        // 新建一个可以添加属性的文本对象
        SpannableString spannableString = new SpannableString(hintText);
        // 新建一个属性对象,设置文字的大小
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(hintTextSize, true);
        // 附加属性到文本
        spannableString.setSpan(absoluteSizeSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 设置hint
        editText.setHint(new SpannedString(spannableString)); // 一定要进行转换,否则属性会消失
    }

    public static void setEditTextHintSize(EditText editText, int hintTextSize) {
        CharSequence charSequence = editText.getHint();
        if (charSequence != null && charSequence.length() > 0) {
            setEditTextHint(editText, charSequence.toString(), hintTextSize);
        }
    }

    public static void setEditTextHintSize(int hintTextSize, EditText... editTextArray) {
        for (EditText editText : editTextArray) {
            CharSequence charSequence = editText.getHint();
            if (charSequence != null && charSequence.length() > 0) {
                setEditTextHint(editText, charSequence.toString(), hintTextSize);
            }
        }
    }

    public static void setEditTextHintSize(EditText... editTextArray) {
        setEditTextHintSize(13, editTextArray);
    }

    public static String getEditText(EditText editText) {
        return editText.getText().toString().trim();
    }

    /**
     * 使输入框获取焦点并弹出输入法
     */
    public static void showSoftInput(Context context, EditText editText) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }
        editText.requestFocus();
    }

    /**
     * 隐藏键盘
     */
    public static void hideSoftInput(Context context, EditText editText) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static void setPhoneNumberTextWatcher(final EditText editText, final View clearButton) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence == null || charSequence.length() == 0)
                    return;
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < charSequence.length(); i++) {
                    if (i != 3 && i != 8 && charSequence.charAt(i) == ' ') {
                        continue;
                    } else {
                        stringBuilder.append(charSequence.charAt(i));
                        if ((stringBuilder.length() == 4 || stringBuilder.length() == 9) && stringBuilder.charAt(stringBuilder
                                .length() - 1) != ' ') {
                            stringBuilder.insert(stringBuilder.length() - 1, ' ');
                        }
                    }
                }
                if (!stringBuilder.toString().equals(charSequence.toString())) {
                    int index = start + 1;
                    if (stringBuilder.charAt(start) == ' ') {
                        if (before == 0) {
                            index++;
                        } else {
                            index--;
                        }
                    } else {
                        if (before == 1) {
                            index--;
                        }
                    }
                    editText.setText(stringBuilder.toString());
                    editText.setSelection(index);
                }
                int length = charSequence.length();
                if (clearButton != null) {
                    if (length == 0) {
                        MyViewUtils.hideView(clearButton);
                    } else {
                        MyViewUtils.showView(clearButton);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        if (clearButton != null) {
            clearButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.setText("");
                    editText.requestFocus();
                }
            });
        }
    }

    public static void setTextWatcher(final EditText editText, int maxLength, final View clearButton) {
        if (maxLength > 0) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                if (clearButton != null) {
                    if (length == 0) {
                        MyViewUtils.hideView(clearButton);
                    } else {
                        MyViewUtils.showView(clearButton);
                    }
                }
            }
        });
        if (clearButton != null) {
            clearButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.setText("");
                    editText.requestFocus();
                }
            });
        }
    }

    public static void setBankCardNumberTextWatcher(final EditText editText) {
        final int maxLength = 29;
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                if (length != 0 && length % 5 == 0) {
                    String text = editText.getText().toString();
                    if (!text.endsWith(" ")) {
                        String newText = text.substring(0, length - 1) + " " + text.substring(length - 1);
                        editText.setText(newText);
                        editText.setSelection(newText.length());
                    }
                }
            }
        });
    }

    /**
     * editText设置监听，改变下划线背景
     *
     * @param edit
     * @param lview
     */
    public static void setEditViewFocusListener(EditText edit, final View lview) {
        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    lview.setBackgroundColor(Color.rgb(0xcc, 0xcc, 0xcc));
                } else {
                    lview.setBackgroundColor(Color.rgb(0xe1, 0x96, 0x00));
                }
            }
        });
    }

    /**
     * 是否包含特殊字符
     *
     * @param str
     * @return
     */
    public static boolean compileExChar(String str) {
        String limitEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern pattern = Pattern.compile(limitEx);
        Matcher m = pattern.matcher(str);
        return m.find();
    }

    /**
     * 禁止EditText输入特殊字符
     *
     * @param editText
     */
    public static void setEditTextInhibitInputSpeChat(EditText editText, int length) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                //				String speChat = "[`~!@$%^&*()+=|{}':;',\\[\\].<>/?~！@￥%……&*（）+|{}【】‘；：”“’。，、？]";
                String speChat = "[`~!@$%^&*()+=|{}':;',\\[\\]#<>/?~！@￥%……&*（）+|{}【】‘；：”“’。，、？]";
                Pattern pattern = Pattern.compile(speChat);
                Matcher matcher = pattern.matcher(source.toString());
                if (matcher.find())
                    return "";
                else
                    return null;
            }
        };
        editText.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(length)});
    }
}
