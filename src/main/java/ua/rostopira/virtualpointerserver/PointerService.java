package ua.rostopira.virtualpointerserver;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Toast;

public class PointerService extends Service {
    private UDPListener listener;

    @Override
    public void onCreate() {
        super.onCreate();

        //Root check
        if (!S.get().su.isPermitted()) {
            Toast.makeText(this, "SU required. Grant acces and restart app", Toast.LENGTH_LONG);
            //Commit suicide
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point ss = new Point();
        wm.getDefaultDisplay().getRealSize(ss);
        S.get().screenSize = ss;

        S.get().overlayView = new OverlayView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            ss.x, ss.y,
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

