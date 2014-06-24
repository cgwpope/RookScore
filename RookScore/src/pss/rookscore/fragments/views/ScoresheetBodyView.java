package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScoresheetBodyView extends View {

    
    private final Paint mTextPaint;
    private List<RoundSummary> mRoundSummaries = new ArrayList<RoundSummary>();
	private GameStateModel mModel;
    private Paint mLinePaint;
    private boolean mUseFullWidth = true;
    
    public ScoresheetBodyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextPaint = new Paint();
        mTextPaint.setTextSize(ViewUtilities.scaleText(getContext(), ViewUtilities.TEXT_SIZE));

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.LTGRAY);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(2f);
        
        
    }

    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        
        ArrayList<String> playerNames = mModel.getPlayers();
        
        if(playerNames != null && playerNames.size() > 0){
            //evenly allocate width to players, draw their names
            
            int roundSummaryWidth = (int)ViewUtilities.computeRoundSummaryWidth(mRoundSummaries, mTextPaint, mModel.getPlayers());
            int widthAvailable = getWidth() - roundSummaryWidth;
            float widthPerPlayer = widthAvailable/playerNames.size();
            
            StringBuilder roundSummaryText = new StringBuilder();
            
            for (RoundSummary summary : mRoundSummaries) {
                canvas.translate(0, ViewUtilities.computeRowHeight(mTextPaint, getContext()));
                
                //display the score per player, and then the round summary
                for(int i = 0; i < playerNames.size(); i++){
                    
                    //use paint to clip text
                    String playerName = playerNames.get(i);
                    
                    //TODO: Special case for numChars == 0: reduce font size?

                    String textToDraw = "" + summary.getRoundCumulativeScores().get(playerName);
                    float textWidth = mTextPaint.measureText(textToDraw);

                    float leftmost = i * widthPerPlayer;
                    
                    canvas.drawText(textToDraw, ViewUtilities.computeCentredStringStart(leftmost, widthPerPlayer, textWidth),  - ViewUtilities.scaleText(getContext(), 4), mTextPaint);
                }
                
                
                roundSummaryText.setLength(0);
                
                ViewUtilities.summarizeRoundResult(roundSummaryText, summary.getRoundResult(), playerNames);
                
                
                float summaryX = getWidth() - roundSummaryWidth;
                
                canvas.drawText(roundSummaryText.toString(), summaryX,  - ViewUtilities.scaleText(getContext(), 4) , mTextPaint);
                
                
                //draw a faint gray line to separate rows
                canvas.drawLine(0,0,getWidth(),0, mLinePaint);
            }
        }
    }


	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), (int)ViewUtilities.computeRowHeight(mTextPaint, getContext())* mRoundSummaries.size() + 5);
    }



	public void setGameStateModel(GameStateModel model) {
		mModel = model;
		scoreUpdated();
	}





    public void scoreUpdated() {
        mRoundSummaries = mModel.computeRoundScores();
        requestLayout();
    }
    

    


    
    

}
