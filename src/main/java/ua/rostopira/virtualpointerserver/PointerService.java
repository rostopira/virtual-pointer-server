package ua.rostopira.virtualpointerserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

public class PointerService extends Service {
    OverlayView mView;
    UDPListener listener;
    CountDownTimer timer;

    public void Update(final int x, final int y) {
        Log.d("PointerService", "Updating cursor position. X = " + Integer.toString(x) +
                "\nY = " + Integer.toString(y));
        mView.mShowCursor = true;
        timer.cancel();
        timer.start();
        mView.Update(x, y);
        mView.postInvalidate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Singleton.getInstance().pointerService = this;
        Log.d("PointerService", "Creating service");

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        mView = new OverlayView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1280, 720, //Temporary workaround. TODO: FIX ME
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSPARENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.setTitle("Cursor");
        wm.addView(mView, params);

        timer = new CountDownTimer(5000, 5000) {
            @Override public void onTick(long l) {}
            @Override public void onFinish() {
                mView.mShowCursor = false;
            }
        };
        timer.start();

        Log.d("PointerService", "Starting UDPListener");
        listener = new UDPListener();
        listener.execute(6969);
        Log.d("PointerService", "Service created");
        Singleton.getInstance().screenH = metrics.heightPixels;
        Singleton.getInstance().screenW = metrics.widthPixels;
        Singleton.getInstance().longPress = Integer.toString(ViewConfiguration.getLongPressTimeout());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CursorService", "Service destroyed");
        Singleton.getInstance().pointerService = null;
        if(mView != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView);
            mView = null;
        }
        listener.stop();
    }
}

class OverlayView extends View {
    private Paint mLoadPaint;
    public boolean mShowCursor;
    public int x = 0,y = 0;

    public void Update(int nx, int ny) {
        x = nx;
        y = ny;
    }

    public OverlayView(Context context) {
        super(context);
        mLoadPaint = new Paint();
        mLoadPaint.setColor(Color.MAGENTA);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShowCursor)
            canvas.drawCircle(x,y,20,mLoadPaint); //TODO: replace dot with cursor
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        //I don't know, what must be here
        //If I don't know what is it - than I don't need it at all
    }

}