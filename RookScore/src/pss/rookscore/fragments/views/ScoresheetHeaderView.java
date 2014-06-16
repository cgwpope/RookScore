package pss.rookscore.fragments.views;

import java.util.List;

import com.triggertrap.seekarc.SeekArc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScoresheetHeaderView extends View {

    

    private List<String> mPlayerNames;
    private final Paint mPaint;
    
    public ScoresheetHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setTextSize(ScoresheetBodyView.scaleText(getContext(), ScoresheetBodyView.TEXT_SIZE));
    }

    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if(mPlayerNames != null && mPlayerNames.size() > 0){
            //evenly allocate width to players, draw their names
            
            int widthAvailable = getWidth() - (int)ScoresheetBodyView.scaleText(getContext(), ScoresheetBodyView.ROUND_SUMMARY_WIDTH);
            
            float widthPerPlayer = widthAvailable/mPlayerNames.size();
            
            for(int i = 0; i < mPlayerNames.size(); i++){
                
                //use paint to clip text
                String playerName = mPlayerNames.get(i);
                int numChars = mPaint.breakText(playerName, true, widthPerPlayer, null);
                
                //TODO: Special case for numChars == 0: reduce font size?

                String textToDraw;
                if(numChars < playerName.length()){
                    //try initials
                    String shorterName = ViewUtilities.shorterName(mPlayerNames, playerName);
                    numChars = mPaint.breakText(shorterName, true, widthPerPlayer, null);
                    if(numChars < shorterName.length()){
                        textToDraw = playerName.substring(0, numChars);
                    } else {
                        textToDraw = shorterName;
                    }
                } else {
                    textToDraw = playerName;
                }
                
                float textWidth = mPaint.measureText(textToDraw);
                float midPoint = i * widthPerPlayer + widthPerPlayer/2;
                
                
                canvas.drawText(textToDraw, midPoint - textWidth/2, mPaint.getTextSize(), mPaint);
            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), (int)ScoresheetBodyView.scaleText(getContext(), ScoresheetBodyView.TEXT_SIZE));
    }
    
    public void setPlayerNames(List<String> players){
        mPlayerNames = players;
        invalidate();
    }
    
    

}
