package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pss.rookscore.GameStateModel.RoundResult;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScoresheetBodyView extends View {

    

    static final int TEXT_SIZE = 16;

    static final int ROUND_SUMMARY_WIDTH = 150;
    
    private List<String> mPlayerNames;
    private final Paint mPaint;
    private List<RoundSummary> mRoundSummaries = new ArrayList<ScoresheetBodyView.RoundSummary>();
    
    public ScoresheetBodyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setTextSize(scaleText(getContext(), TEXT_SIZE));

    }

    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if(mPlayerNames != null && mPlayerNames.size() > 0){
            //evenly allocate width to players, draw their names
            
            int widthAvailable = getWidth() - (int)ScoresheetBodyView.scaleText(getContext(), ROUND_SUMMARY_WIDTH);
            float widthPerPlayer = widthAvailable/mPlayerNames.size();
            
            StringBuilder roundSummaryText = new StringBuilder();
            
            int row = 0;
            for (RoundSummary summary : mRoundSummaries) {
                
                //display the score per player, and then the round summary
                for(int i = 0; i < mPlayerNames.size(); i++){
                    
                    //use paint to clip text
                    String playerName = mPlayerNames.get(i);
                    
                    //TODO: Special case for numChars == 0: reduce font size?

                    String textToDraw = "" + summary.getRoundScores().get(playerName);
                    float textWidth = mPaint.measureText(textToDraw);
                    float midPoint = i * widthPerPlayer + widthPerPlayer/2;
                    canvas.drawText(textToDraw, midPoint - textWidth/2, mPaint.getTextSize() * (row + 1), mPaint);
                }
                
                
                roundSummaryText.setLength(0);
                
                roundSummaryText
                .append(ViewUtilities.shorterName(mPlayerNames, summary.getRoundResult().getCaller()))
                .append(' ')
                .append('(')
                .append(summary.getRoundResult().getBid())
                .append(')');
                
                if(summary.getRoundResult().getCaller().equals(summary.getRoundResult().getParter())){
                    roundSummaryText.append(" -- ");
                } else {
                    roundSummaryText
                    .append(',')
                    .append(ViewUtilities.shorterName(mPlayerNames, summary.getRoundResult().getParter()))
                    .append(" - ");
                }
                
                
                
                roundSummaryText.append(summary.getRoundResult().getMade());
                
                if(summary.getRoundResult().getMade() >= summary.getRoundResult().getBid()){
                    roundSummaryText.append('\u2713');
                } else {
                    roundSummaryText.append('\u2717');
                }
                
                
                
                canvas.drawText(roundSummaryText.toString(), getWidth() - ScoresheetBodyView.scaleText(getContext(), ROUND_SUMMARY_WIDTH),  mPaint.getTextSize() * (row + 1), mPaint);
                
                row++;
            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), (int)scaleText(getContext(), TEXT_SIZE) * mRoundSummaries.size());
    }
    
    public void setPlayerNames(List<String> playerNames){
        mPlayerNames = playerNames;
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
    

    

    
    

}
