package ua.rostopira.virtualpointerserver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * InjectionManager is main class for injection InputEvents.
 * The class uses internal APIs to inject. Injection depends on android.permission.INJECT_EVENTS.
 * @author Anders Bo Pedersen, ABP Consult Aps, 2013
 */
public class InjectionManager
{
    private static final String TAG = InjectionManager.class.getSimpleName();

    private static int EVENT_MODE_ASYNC;
    private static int EVENT_MODE_WAIT_FOR_RESULT;
    private static int EVENT_MODE_WAIT_FOR_FINISH;

    private Object mInputManager;
    private Method mInjectEventMethod;

    public boolean gotService;

    public InjectionManager(Context c)
    {
        mInputManager = c.getSystemService(Context.INPUT_SERVICE);
        gotService = true;

        try
        {
            //Unveil hidden methods
            mInjectEventMethod = mInputManager.getClass().getDeclaredMethod("injectInputEvent", new Class[] { InputEvent.class, Integer.TYPE });
            mInjectEventMethod.setAccessible(true);
            Field eventAsync = mInputManager.getClass().getDeclaredField("INJECT_INPUT_EVENT_MODE_ASYNC");
            Field eventResult = mInputManager.getClass().getDeclaredField("INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT");
            Field eventFinish = mInputManager.getClass().getDeclaredField("INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH");
            eventAsync.setAccessible(true);
            eventResult.setAccessible(true);
            eventFinish.setAccessible(true);
            EVENT_MODE_ASYNC = eventAsync.getInt(mInputManager.getClass());
            EVENT_MODE_WAIT_FOR_RESULT = eventResult.getInt(mInputManager.getClass());
            EVENT_MODE_WAIT_FOR_FINISH = eventFinish.getInt(mInputManager.getClass());
        }
        catch (NoSuchMethodException nsme)
        {
            Log.e(TAG,  "Critical methods not available");
            gotService = false;
        }
        catch (NoSuchFieldException nsfe)
        {
            Log.e(TAG,  "Critical fields not available");
            gotService = false;
        }
        catch (IllegalAccessException iae)
        {
            Log.e(TAG,  "Critical fields not accessable");
            gotService = false;
        }
    }

    public void injectTouchEventDown(int x, int y)
    {
        MotionEvent me = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis()+10,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                0
        );

        me.setSource(InputDevice.SOURCE_TOUCHSCREEN);

        injectEvent(me, EVENT_MODE_WAIT_FOR_RESULT);
        me.recycle();
    }

    public void injectTouchEventRelease(int x, int y)
    {
        MotionEvent me = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis()+10,
                MotionEvent.ACTION_UP,
                x,
                y,
                0
        );

        me.setSource(InputDevice.SOURCE_TOUCHSCREEN);

        injectEvent(me, EVENT_MODE_WAIT_FOR_RESULT);
        me.recycle();
    }

    public void injectMove(int x, int y) {
        MotionEvent me = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis()+10,
                MotionEvent.ACTION_MOVE,
                x,
                y,
                0
        );
        me.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        injectEvent(me, EVENT_MODE_WAIT_FOR_RESULT);
        me.recycle();
    }

    public void injectKeyPress(int keycode) {
        KeyEvent ke = new KeyEvent(KeyEvent.ACTION_DOWN, keycode);
        injectEvent(ke, EVENT_MODE_WAIT_FOR_RESULT);
        KeyEvent ke2 = new KeyEvent(KeyEvent.ACTION_UP, keycode);
        injectEvent(ke2, EVENT_MODE_WAIT_FOR_RESULT);
    }

    public void injectMouseBtn(int mouseBtn) {
        MotionEvent me = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis()+10,
                mouseBtn,
                0, 0, 0
        );
    }

    private void injectEvent(InputEvent ie, int mode)
    {
        try
        {
            mInjectEventMethod.invoke(mInputManager, new Object[] { ie, mode });
        }
        catch (IllegalAccessException iae)
        {
            Log.e(TAG,  "Critical methods not accessable: "+iae.getLocalizedMessage());
        }
        catch (InvocationTargetException ite)
        {
            Log.e(TAG, "Error invoking injection method: "+ite.getLocalizedMessage());
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error using injection method: "+e.getLocalizedMessage());
        }
    }

}
