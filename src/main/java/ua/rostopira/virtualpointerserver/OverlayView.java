package ua.rostopira.virtualpointerserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.view.View;

/**
 * Long time ago there was some code from pocketmagic.com
 * And that was horrible, so I fully rewrote it
 */
public class OverlayView extends View {
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
        x = (X < 0) ? 0 : (x > S.get().screenSize.x) ? S.get().screenSize.x : x;
        y = (Y < 0) ? 0 : (y > S.get().screenSize.y) ? S.get().screenSize.y : y;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showCursor)
            canvas.drawBitmap(cursor,x,y,paint);
    }
}