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
    private static int w, h; // Screen size
    private CountDownTimer timer; //for autohide
    private Bitmap cursor;
    private Paint paint;

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
        w = S.get().screenSize.x;
        h = S.get().screenSize.y;
    }

    public void Update(int X, int Y) {
        showCursor = true;
        timer.cancel();
        timer.start();
        x = (X < 0) ? 0 : (X > w) ? w : X;
        y = (Y < 0) ? 0 : (Y > h) ? h : Y;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showCursor)
            canvas.drawBitmap(cursor,x,y,paint);
    }
}