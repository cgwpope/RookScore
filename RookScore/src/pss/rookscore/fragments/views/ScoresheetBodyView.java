package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.GameStateModel;
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
            
            int row = 0;
            for (RoundSummary summary : mRoundSummaries) {
                
                //display the score per player, and then the round summary
                for(int i = 0; i < playerNames.size(); i++){
                    
                    //use paint to clip text
                    String playerName = playerNames.get(i);
                    
                    //TODO: Special case for numChars == 0: reduce font size?

                    String textToDraw = "" + summary.getRoundScores().get(playerName);
                    float textWidth = mTextPaint.measureText(textToDraw);

                    float leftmost = i * widthPerPlayer;
                    
                    canvas.drawText(textToDraw, ViewUtilities.computeCentredStringStart(leftmost, widthPerPlayer, textWidth), ViewUtilities.computeRowHeight(mTextPaint, getContext()) * (row + 1) - ViewUtilities.scaleText(getContext(), 4), mTextPaint);
                }
                
                
                roundSummaryText.setLength(0);
                
                ViewUtilities.summarizeRoundResult(roundSummaryText, summary, playerNames);
                
                
                
                float textWidth = mTextPaint.measureText(roundSummaryText.toString());
                float summaryX = ViewUtilities.computeCentredStringStart(getWidth() - roundSummaryWidth, roundSummaryWidth, textWidth);
                
                
                
                canvas.drawText(roundSummaryText.toString(), summaryX,  ViewUtilities.computeRowHeight(mTextPaint, getContext())  * (row + 1) - ViewUtilities.scaleText(getContext(), 4) , mTextPaint);
                
                
                //draw a faint gray line to separate rows
                canvas.drawLine(
                        0, 
                        ViewUtilities.computeRowHeight(mTextPaint, getContext()) * (row + 1), 
                        getWidth(), 
                        ViewUtilities.computeRowHeight(mTextPaint, getContext()) * (row + 1), mLinePaint);
                
                
                
                row++;
            }
        }
    }






	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), (int)ViewUtilities.computeRowHeight(mTextPaint, getContext())* mRoundSummaries.size() + 5);
    }
    
    

    




	public void setGameStateModel(GameStateModel model) {
		mModel = model;
		requestLayout();
	}


	public void setRoundScores(List<RoundSummary> roundSummaries) {
		mRoundSummaries = roundSummaries;
		requestLayout();
	}
    

    

    
    

}
