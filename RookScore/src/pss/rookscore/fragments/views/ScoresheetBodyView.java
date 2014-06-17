package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pss.rookscore.GameStateModel;
import pss.rookscore.GameStateModel.RoundResult;
import pss.rookscore.fragments.views.ScoresheetBodyView.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScoresheetBodyView extends View {

    
    private final Paint mPaint;
    private List<RoundSummary> mRoundSummaries = new ArrayList<ScoresheetBodyView.RoundSummary>();
	private GameStateModel mModel;
    
    public ScoresheetBodyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setTextSize(scaleText(getContext(), ViewUtilities.TEXT_SIZE));

    }

    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        
        ArrayList<String> playerNames = mModel.getPlayers();
        
        if(playerNames != null && playerNames.size() > 0){
            //evenly allocate width to players, draw their names
            
            int widthAvailable = getWidth() - (int)ScoresheetBodyView.scaleText(getContext(), ViewUtilities.ROUND_SUMMARY_WIDTH);
            float fullWidth = widthAvailable/playerNames.size();
            
            StringBuilder roundSummaryText = new StringBuilder();
            
            int row = 0;
            for (RoundSummary summary : mRoundSummaries) {
                
                //display the score per player, and then the round summary
                for(int i = 0; i < playerNames.size(); i++){
                    
                    //use paint to clip text
                    String playerName = playerNames.get(i);
                    
                    //TODO: Special case for numChars == 0: reduce font size?

                    String textToDraw = "" + summary.getRoundScores().get(playerName);
                    float textWidth = mPaint.measureText(textToDraw);

                    float leftmost = i * fullWidth;
                    
                    canvas.drawText(textToDraw, ViewUtilities.computeCentredStringStart(leftmost, fullWidth, textWidth), mPaint.getTextSize() * (row + 1), mPaint);
                }
                
                
                roundSummaryText.setLength(0);
                
                summarizeRoundResult(roundSummaryText, summary);
                
                
                
                canvas.drawText(roundSummaryText.toString(), getWidth() - ScoresheetBodyView.scaleText(getContext(), ViewUtilities.ROUND_SUMMARY_WIDTH),  mPaint.getTextSize() * (row + 1), mPaint);
                
                row++;
            }
        }
    }



	private void summarizeRoundResult(StringBuilder roundSummaryText, RoundSummary summary) {
		roundSummaryText
		.append(ViewUtilities.shorterName(mModel.getPlayers(), summary.getRoundResult().getCaller()))
		.append(' ')
		.append('(')
		.append(summary.getRoundResult().getBid())
		.append(')');
		
		if(summary.getRoundResult().getCaller().equals(summary.getRoundResult().getParter())){
		    roundSummaryText.append(" -- ");
		} else {
		    roundSummaryText
		    .append(',')
		    .append(ViewUtilities.shorterName(mModel.getPlayers(), summary.getRoundResult().getParter()))
		    .append(" - ");
		}
		
		
		
		roundSummaryText.append(summary.getRoundResult().getMade());
		
		if(summary.getRoundResult().getMade() >= summary.getRoundResult().getBid()){
		    roundSummaryText.append('\u2713'); //checkmark
		} else {
		    roundSummaryText.append('\u2717'); //X
		}
	}



	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), (int)scaleText(getContext(), ViewUtilities.TEXT_SIZE) * mRoundSummaries.size());
    }
    
    
    public void setRoundSummaries(List<RoundSummary> summaries){
        mRoundSummaries = summaries;
    }
    
    
    public static class RoundSummary {
        private final RoundResult mRoundResult;
        private final Map<String, Integer> mRoundScores;

        public RoundSummary(RoundResult roundResult, Map<String, Integer> roundScores) {
            super();
            mRoundResult = roundResult;
            mRoundScores = roundScores;
        }
        
        public RoundResult getRoundResult() {
            return mRoundResult;
        }
        
        public Map<String, Integer> getRoundScores() {
            return mRoundScores;
        }
    }
    
    
    public static float scaleText(Context c, int size){
        return c.getResources().getDisplayMetrics().density * size;
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
