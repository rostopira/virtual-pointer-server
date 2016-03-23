package ua.rostopira.virtualpointerserver;

public class MessageParser {
    private float x, y;

    public MessageParser() {
        x = S.get().screenSize.x / 2;
        y = S.get().screenSize.y / 2;
    }

    /**
     * Simple commands parser. No encryption, or anything else
     * Just a letter and numbers. KISS
     */
    public void exec(String... values) {
        String result;
        switch (values[0].charAt(0)) {
            case 'M': //move cursor
                x += S.get().screenSize.x * Math.sin(Float.parseFloat(values[1]));
                y += S.get().screenSize.y * Math.sin(Float.parseFloat(values[2]));
                S.get().overlayView.Update(Math.round(x), Math.round(y));
                return;
            case 'C': //center
                x = S.get().screenSize.x / 2;
                y = S.get().screenSize.y / 2;
                return;
            case 'S': //swipe
                result = String.format("input swipe %s %s %n",
                        xyToString(x,y),
                        xyToString(x+Float.parseFloat(values[1]), y+Float.parseFloat(values[2])),
                        S.swipeTime);
                break;
            case 'T': //tap
                result = "input tap " + xyToString(x,y);
                break;
            case 'L': //long press
                result = xyToString(x,y); //Call just once. Little optimization
                result = String.format("input swipe %s %s %d", result, result, S.get().longPress);
                break;
            case 'K': //key press
                result = "input keyevent " + values[1];
                break;
            default: //wtf?
                return;
        }
        S.get().su.DO(result);
    }

    /**
     * Formats x and y to string with space as seperator
     * Also, checks, if values in screen bounds.
     */
    private String xyToString(float x, float y) {
    int X = Math.round( (x < 0) ? 0 : (x > S.get().screenSize.x) ? S.get().screenSize.x : x );
    int Y = Math.round( (y < 0) ? 0 : (y > S.get().screenSize.y) ? S.get().screenSize.y : y );
    return String.format("%d %d", X, Y);
    }
}
