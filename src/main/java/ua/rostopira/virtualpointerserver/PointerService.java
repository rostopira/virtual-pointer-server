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
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

/* @project
 * License to access, copy or distribute this file.
 * This file or any portions of it, is Copyright (C) 2012, Radu Motisan ,  http://www.pocketmagic.net . All rights reserved.
 * @author Radu Motisan, radu.motisan@gmail.com
 *
 * This file is protected by copyright law and international treaties. Unauthorized access, reproduction
 * or distribution of this file or any portions of it may result in severe civil and criminal penalties.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * @purpose
 * Cursor Overlay Sample
 * (C) 2012 Radu Motisan , all rights reserved.
 */

public class PointerService extends Service {
    OverlayView mView;
    UDPListener listener;

    //Timer for autohide cursor when unused
    CountDownTimer timer = new CountDownTimer(5000,5000) {
        @Override public void onTick(long l) {}
        @Override public void onFinish() {
            mView.mShowCursor = false;
        }
    };

    public void Update(final int x, final int y) {
        Log.d("PointerService", "Updating cursor position. X = " + Integer.toString(x) +
                "\nY = " + Integer.toString(y));
        timer.cancel(); timer.start(); //Packet received => timer reset
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

        mView = new OverlayView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, //will cover status bar as well
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.setTitle("Cursor");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(mView, params);

        //Get display resolution
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        Singleton.getInstance().screenH = metrics.heightPixels;
        Singleton.getInstance().screenW = metrics.widthPixels;
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
}

class OverlayView extends ViewGroup {
    private Paint mLoadPaint;
    public boolean mShowCursor;
    public int x = 0,y = 0;

    public void Update(int nx, int ny) {
        mShowCursor = true;
        x = nx;
        y = ny;
    }

    public OverlayView(Context context) {
        super(context);
        mLoadPaint = new Paint();
        //TODO: add size and color chooser to MainActivity
        mLoadPaint.setTextSize(100);
        mLoadPaint.setColor(Color.MAGENTA);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText("▶", x, y, mLoadPaint); //Who needs picture?
        //Maybe should add unicode version to requirements? :D
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        //I don't know, what must be here
        //If I don't know what is it - than I don't need it at all
    }

}