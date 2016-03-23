package ua.rostopira.virtualpointerserver;

import android.app.Application;
import android.graphics.Point;

/**
 * Singleton
 */
public class S extends Application {
    private static S instance;
    public static final int port = 6969,
                        swipeTime = 250;
    public int longPress;
    public Point screenSize = new Point();
    public OverlayView overlayView;
    public SU su;

    public S() {
        super();
        su = new SU();
        instance = this;
    }

    // Double-checked singleton fetching
    public static S get() {
        if(instance == null) {
            synchronized(S.class) {
                if(instance == null) new S();
            }
        }
        return instance;
    }
    
    @Override
    public void onCreate()
    {
        super.onCreate();
    }
}

