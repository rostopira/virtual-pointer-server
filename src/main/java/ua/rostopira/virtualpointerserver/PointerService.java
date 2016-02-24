package ua.rostopira.virtualpointerserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.IBinder;
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
        Point screenSize = new Point();
        wm.getDefaultDisplay().getRealSize(screenSize); //Yeah, that's why I support only 4.2+
        //Just joking. I support only 4.2+, because the first Android HDMI stick used 4.2.2,
        //and almost all of them currently updated to KitKat
        //Have an older version of Android? Really? Use CM or just throw out that mammoth shit

        mView = new OverlayView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                screenSize.x, screenSize.y,
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

        Singleton.getInstance().screenSize = screenSize;
        Singleton.getInstance().longPress = Integer.toString(ViewConfiguration.getLongPressTimeout());
        Log.d("PointerService", "Starting UDPListener");
        listener = new UDPListener();
        listener.execute(6969);
        Log.d("PointerService", "Service created");
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

    /**
     * Long time ago there was some code from pocketmagic.com
     * And that was horrible shit
     * So I rewrote it
     * But thanks for idea
     */

    class OverlayView extends View {
        private Paint mLoadPaint;
        public boolean mShowCursor;
        public int x = 0,y = 0;

        public void Update(int nx, int ny) {
            x = nx;
            y = ny;
            postInvalidate();
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
    }

}

