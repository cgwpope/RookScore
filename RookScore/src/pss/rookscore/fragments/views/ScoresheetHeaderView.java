package pss.rookscore.fragments.views;

import java.util.ArrayList;

import pss.rookscore.GameStateModel;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScoresheetHeaderView extends View {

    

    private final Paint mPaint;
	private GameStateModel mModel;
    
    public ScoresheetHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setTextSize(ScoresheetBodyView.scaleText(getContext(), ViewUtilities.TEXT_SIZE));
    }

    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        
        ArrayList<String> playerNames = mModel.getPlayers();
        
        if(playerNames != null && playerNames.size() > 0){
            //evenly allocate width to players, draw their names
            
            int widthAvailable = getWidth() - (int)ScoresheetBodyView.scaleText(getContext(), ViewUtilities.ROUND_SUMMARY_WIDTH);
            
            float widthPerPlayer = widthAvailable/playerNames.size();
            
            for(int i = 0; i < playerNames.size(); i++){
                
                //use paint to clip text
                String playerName = playerNames.get(i);
                int numChars = mPaint.breakText(playerName, true, widthPerPlayer, null);
                
                //TODO: Special case for numChars == 0: reduce font size?

                String textToDraw;
                if(numChars < playerName.length()){
                    //try initials
                    String shorterName = ViewUtilities.shorterName(playerNames, playerName);
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
                
                canvas.drawText(textToDraw, ViewUtilities.computeCentredStringStart(i * widthPerPlayer, widthPerPlayer, textWidth), mPaint.getTextSize(), mPaint);
            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), (int)ScoresheetBodyView.scaleText(getContext(), ViewUtilities.TEXT_SIZE));
    }
    
    public void setGameStateModel(GameStateModel model){
        mModel = model;
        invalidate();
    }
    
    

}
