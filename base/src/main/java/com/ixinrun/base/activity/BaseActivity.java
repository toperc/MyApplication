package com.ixinrun.base.activity;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ixinrun.base.activity.view.IBaseView;

/**
 * 描述：BaseActivity
 * </p>
 *
 * @author : ixinrun
 * @date : 2018/12/24
 */
public abstract class BaseActivity extends AppCompatActivity implements IBaseView {

    /**
     * 唯一标识
     */
    protected final String mTag = getClass().getName();

    /**
     * Context上下文
     */
    protected Activity mContext;

    /**
     * IBaseView的包装实现类
     */
    private IBaseView mWrapView;

    private boolean mKeyboardAutoHide = true;
    private boolean mFocusAutoLose = false;
    private boolean mActivityDestroyed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mWrapView = initBaseViewImpl(mContext, mTag);
        if (mWrapView == null) {
            throw new NullPointerException("BaseActivity's BaseViewImpl is NULL and you must initialize it.");
        }
        init();
        initView();
        initEvent();
        loadData(savedInstanceState);
    }

    /**
     * 初始化view之前的相关操作
     */
    protected void init() {
        if (isTranslucentStatusBar()) {
            translucentStatusBar();
        }
    }

    /**
     * 沉浸式状态栏
     */
    private void translucentStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int option = window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(option);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 初始化View视图
     */
    protected abstract void initView();

    /**
     * 初始化View事件
     */
    protected void initEvent() {
    }

    /**
     * 加载数据
     */
    protected abstract void loadData(Bundle savedInstanceState);

    /**
     * 沉浸式状态栏
     */
    protected boolean isTranslucentStatusBar() {
        return false;
    }

    /**
     * 设置点击空白处是否隐藏软键盘
     */
    protected void setKeyboardAutoHide(boolean b) {
        this.mKeyboardAutoHide = b;
    }

    /**
     * 设置点击空白处是否失去焦点
     */
    protected void setFocusAutoLose(boolean b) {
        this.mFocusAutoLose = b;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isEditText(v, ev) && mKeyboardAutoHide) {
                hideSoftInput(v);
                if (mFocusAutoLose) {
                    v.clearFocus();
                }
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean isEditText(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();

            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }

        return false;
    }

    /**
     * 隐藏软键盘
     */
    protected void hideSoftInput(View view) {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null) {
            im.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 提前进行资源回收释放
     * 例：
     * 1.A->B->A->B...反复快速执行，导致B页面执行onCreate()或onResume()后接着却执行上一次销毁时的onDestroy(),产生异常。
     * 2.Activity被引用，销毁时无法走到onDestroy(),产生内存泄漏。
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            destroy();
        }
    }

    /**
     * 生命尽头资源回收释放
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }

    private void destroy() {
        if (mActivityDestroyed) {
            return;
        }
        onRecycle();
        mActivityDestroyed = true;
    }

    /**
     * 资源回收释放
     */
    protected void onRecycle() {
    }

    /**
     * ---------------------------------------view操作代理------------------------------------
     */

    @Override
    public void showProgress() {
        mWrapView.showProgress();
    }

    @Override
    public void showProgress(boolean cancelable) {
        mWrapView.showProgress(cancelable);
    }

    @Override
    public void closeProgress() {
        mWrapView.closeProgress();
    }

    @Override
    public void tip(@NonNull String msg) {
        mWrapView.tip(msg);
    }

    @Override
    public void tip(int resId) {
        mWrapView.tip(resId);
    }

    @Override
    public void tip(@NonNull String msg, @NonNull TipEnum tipEnum) {
        mWrapView.tip(msg, tipEnum);
    }

    @Override
    public void tip(int resId, @NonNull TipEnum tipEnum) {
        mWrapView.tip(resId, tipEnum);
    }

    @Override
    public void detachView() {
        mWrapView.detachView();
    }

    /**
     * IBaseView包装实现
     *
     * @return BaseViewImpl
     */
    protected abstract IBaseView initBaseViewImpl(Context context, String tag);
}
