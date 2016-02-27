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
    OverlayView overlayView;
    UDPListener listener;

    @Override
    public void onCreate() {
        super.onCreate();
        S.get().pointerService = this;
        Log.d("PointerService", "Creating service");

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point screenSize = new Point();
        wm.getDefaultDisplay().getRealSize(screenSize); //Yeah, that's why I support only 4.2+
        //Just joking. I support only 4.2+, because the first Android HDMI stick used 4.2.2,
        //Have an older version of Android? Really? Use CM or just throw away that mammoth shit

        overlayView = new OverlayView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                screenSize.x, screenSize.y,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSPARENT);
        //params.gravity = Gravity.TOP | Gravity.LEFT;
        //params.setTitle("Cursor");
        wm.addView(overlayView, params);

        S.get().screenSize = screenSize;
        S.get().longPress = Integer.toString(ViewConfiguration.getLongPressTimeout());
        Log.d("PointerService", "Starting UDPListener");
        listener = new UDPListener();
        listener.execute(6969);
        Log.d("PointerService", "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CursorService", "Service destroyed");
        S.get().pointerService = null;
        if(overlayView != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(overlayView);
            overlayView = null;
        }
        listener.stop();
    }

    /**
     * Long time ago there was some code from pocketmagic.com
     * And that was horrible, so I fully rewrote it
     * But thanks for idea
     */
    class OverlayView extends View {
        boolean showCursor;
        int x = 0,y = 0;
        CountDownTimer timer;
        Bitmap cursor;
        Paint paint;

        public OverlayView(Context context) {
            super(context);
            timer = new CountDownTimer(3000, 3000) {
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
            x = X;
            y = Y;
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

