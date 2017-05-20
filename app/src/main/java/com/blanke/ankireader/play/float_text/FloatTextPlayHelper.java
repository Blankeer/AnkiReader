package com.blanke.ankireader.play.float_text;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.play.BasePlayHelper;
import com.blanke.ankireader.play.float_text.view.BaseFloatView;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.Disposable;

import static android.content.Context.WINDOW_SERVICE;

/**
 * 浮动文本播放
 * Created by blanke on 2017/5/14.
 */

public class FloatTextPlayHelper extends BasePlayHelper {

    private static final int NOTIFICATION_ID = 888;
    private Disposable disposable;
    private WindowManager windowManager;
    private BaseFloatView floatView;
    private boolean isViewAdded = false;


    public static FloatTextPlayHelper getInstance(Service service) {
        return new FloatTextPlayHelper(new WeakReference<>(service));
    }

    public FloatTextPlayHelper(WeakReference<Service> service) {
        super(service);
        initPermission();
    }

    public void setFloatView(BaseFloatView floatView) {
        this.floatView = floatView;
        if (floatView != null) {
            addFloatView();
        }
    }

    private boolean initPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getService())) {
                Toast.makeText(getService(),
                        "请在设置中授予悬浮窗权限，重新播放",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getService().startActivity(intent);
                return false;
            }
        }
        return true;
    }

    private boolean addFloatView() {
        if (getService() == null) {
            return false;
        }
        if (floatView == null) {
            return false;
        }
        if (!initPermission()) {
            return false;
        }
        windowManager = (WindowManager) getService().getSystemService(WINDOW_SERVICE);
        View view = (View) floatView;
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.x = 0;
        params.y = 0;
        windowManager.addView(view, params);
        floatView.initView(windowManager, params);
        isViewAdded = true;
        return true;
    }

    @Override
    public void play(Note note) throws Exception {
        if (getService() != null) {
            if (isViewAdded) {
                floatView.setNote(note);
            } else {
                addFloatView();
            }
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }

    @Override
    public void destroy() {
        this.service.clear();
        if (floatView != null) {
            windowManager.removeView((View) floatView);
            floatView = null;
        }
    }

    private Service getService() {
        return this.service.get();
    }
}
