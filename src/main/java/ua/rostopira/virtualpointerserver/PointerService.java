package ua.rostopira.virtualpointerserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

public class PointerService extends Service {
    public OverlayView overlayView;
    private UDPListener listener;
    private Point screenSize = new Point();

    @Override
    public void onCreate() {
        super.onCreate();
        S.get().pointerService = this;

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealSize(screenSize); //Yeah, that's why I support only 4.2+
        //Just joking. I support only 4.2+, because the first Android HDMI stick was with 4.2.2

        overlayView = new OverlayView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            screenSize.x, screenSize.y,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSPARENT);
        wm.addView(overlayView, params);

        S.get().screenSize = screenSize;
        S.get().longPress = ViewConfiguration.getLongPressTimeout();
        listener = new UDPListener();
        listener.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CursorService", "Service destroyed");
        S.get().pointerService = null;
        listener.cancel(false);
        ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(overlayView);
    }

    /**
     * Long time ago there was some code from pocketmagic.com
     * And that was horrible, so I fully rewrote it
     */
    protected class OverlayView extends View {
        private boolean showCursor;
        private int x, y;
        CountDownTimer timer; //for autohide
        Bitmap cursor;
        Paint paint;

        public OverlayView(Context context) {
            super(context);
            timer = new CountDownTimer(500, 500) {
                @Override public void onTick(long l) {}
                @Override public void onFinish() {
                    showCursor = false;
                    postInvalidate();
                }
            };
            showCursor = false;
            cursor = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cursor);
            paint = new Paint();
        }

        public void Update(int X, int Y) {
            showCursor = true;
            timer.cancel();
            timer.start();
            x = (X < 0) ? 0 : (x > screenSize.x) ? screenSize.x : x;
            y = (Y < 0) ? 0 : (y > screenSize.y) ? screenSize.y : y;
            postInvalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (showCursor)
                canvas.drawBitmap(cursor,x,y,paint);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

