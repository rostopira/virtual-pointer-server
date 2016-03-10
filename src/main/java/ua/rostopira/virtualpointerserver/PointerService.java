package ua.rostopira.virtualpointerserver;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.WindowManager;

public class PointerService extends Service {
    private UDPListener listener;

    @Override
    public void onCreate() {
        super.onCreate();

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealSize(S.get().screenSize);
        //Yeah, that's why support only 4.2+
        //Just joking. First Android HDMI stick was with 4.2.2

        S.get().overlayView = new OverlayView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            S.get().screenSize.x, S.get().screenSize.y,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSPARENT);
        wm.addView(S.get().overlayView, params);

        S.get().longPress = ViewConfiguration.getLongPressTimeout();
        listener = new UDPListener();
        listener.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CursorService", "Service destroyed");
        listener.cancel(false);
        ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(S.get().overlayView);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

