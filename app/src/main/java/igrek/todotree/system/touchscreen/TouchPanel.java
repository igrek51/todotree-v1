package igrek.todotree.system.touchscreen;

import android.app.Activity;

import igrek.todotree.system.output.Output;

public class TouchPanel {
    float w, h; //rozmiary ekranu dotykowego
    float start_x, start_y; //punkt początkowy smyrania
    int dpi;
    public static final float INCH = 2.54f; //1 cal [cm]

    private Activity activity;

    public TouchPanel(Activity activity) {
        dpi = activity.getResources().getDisplayMetrics().densityDpi;
        Output.log("DPI urządzenia: " + dpi);
    }

    public float pixelsToCm(float pixels) {
        return pixels / dpi * INCH;
    }

    public float cmToPixels(float cm) {
        return cm / INCH * dpi;
    }

    public boolean touchDown(float touch_x, float touch_y) {
        start_x = touch_x;
        start_y = touch_y;

        return false;
    }

    public boolean touchMove(float touch_x, float touch_y) {

        return false;
    }

    public boolean touchUp(float touch_x, float touch_y) {

        return false;
    }
}
