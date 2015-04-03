
package pss.rookscore.fragments.views;

import android.content.Context;
import android.graphics.Paint;

public class ViewUtilities {

    static final int TEXT_SIZE = 16;

    static float computeCentredStringStart(float leftmost, float fullWidth, float textWidth) {
        return (leftmost + fullWidth / 2) - textWidth / 2;
    }

    static float scaleText(Context c, int size) {
        return c.getResources().getDisplayMetrics().density * size;
    }


    public static float computeLineHeight(Context context, Paint paint) {
        return -paint.getFontMetrics().ascent + paint.getFontMetrics().descent + ViewUtilities.scaleText(context, 1);
    }

    public static Paint defaultTextPaint(Context context) {
        Paint defaultTextPaint = new Paint(); 
        defaultTextPaint.setTextSize(ViewUtilities.scaleText(context, ViewUtilities.TEXT_SIZE));
        defaultTextPaint.setAntiAlias(true);
        return defaultTextPaint;

    }
}
