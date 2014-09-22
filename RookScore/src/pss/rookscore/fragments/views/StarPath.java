package pss.rookscore.fragments.views;

import pss.rookscore.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

class StarPath extends Path {
    
    
    private static final int STAR_POINTS = 5;
    private static final float DEGREES_PER_STAR_POINT = 360f/STAR_POINTS;
    private static final double RADIANS_PER_DEGREE = Math.PI / 180;
    private Paint mStarPaint;
    private final int mBaseAlpha = 128;
    private int mAlphaOffset = 0;

    public StarPath(Context context) {
        final double triangeBaseWidth = Math.tan(DEGREES_PER_STAR_POINT/2 * RADIANS_PER_DEGREE);
        for(int i = 0; i < STAR_POINTS; i++){
            moveTo(0f, 0f);
            lineTo((float)(-1 * triangeBaseWidth), 1f);
            lineTo(0f, 2f);
            lineTo((float)(1 * triangeBaseWidth), 1f);
            lineTo(0, 0);
            close();
            
            Matrix m = new Matrix();
            m.setRotate(DEGREES_PER_STAR_POINT);
            transform(m);
        }
        
        
        //put the star point on top
        Matrix m = new Matrix();
        m.setRotate(180);
        transform(m);
        
        
        mStarPaint = new Paint();
        mStarPaint.setColor(context.getResources().getColor(R.color.rook_cyan));
    }

    public void drawToCanvas(Canvas canvas, float textSize) {
        Path p = new Path(this);
        Matrix m = new Matrix();
        float scaleFactor = (float)(1/(1 + Math.tan(DEGREES_PER_STAR_POINT / 2 * RADIANS_PER_DEGREE))) * textSize;
        m.setScale( scaleFactor, scaleFactor);
        p.transform(m);
        
        mStarPaint.setAlpha(mBaseAlpha + mAlphaOffset );
        
        canvas.drawPath(p, mStarPaint);
        
    }

    public void setAlphaOffset(int animatedValue) {
        mAlphaOffset = animatedValue; 
    }


}
