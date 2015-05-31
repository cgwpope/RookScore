package pss.rookscore.fragments.views;

import java.util.List;

import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.model.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;

public class DoubleLineDrawStrategy implements DrawStrategy {

    private Context mContext;

    private final Paint mPaint;
    private final Paint.FontMetrics mFontMetrics;

    private List<Player> mPlayers;
    private List<RoundSummary> mRoundSummaries;
    private int mTotalWidth;
    private Paint mCheckMarkPaint;
    private Paint mXMarkPaint;

    public DoubleLineDrawStrategy(Context context, Paint p, List<Player> players, List<RoundSummary> roundSummaries, int totalWidth) {
        mContext = context;

        mPaint = p;
        mFontMetrics = mPaint.getFontMetrics();
        
        mCheckMarkPaint = new Paint();
        mCheckMarkPaint.setStyle(Style.FILL);
        mCheckMarkPaint.setColor(Color.GREEN);
        mCheckMarkPaint.setAlpha(64);

        mXMarkPaint = new Paint();
        mXMarkPaint.setStyle(Style.FILL);
        mXMarkPaint.setColor(Color.RED);
        mXMarkPaint.setAlpha(64);

        
        mPlayers = players;
        mRoundSummaries = roundSummaries;
        mTotalWidth = totalWidth;
    }

    @Override
    public float computeHeight() {
        return  2 * ViewUtilities.computeLineHeight(mContext, mFontMetrics);
    }


    @Override
    public void drawRoundSummary(Context context, Canvas c, RoundSummary summary) {
        //we are ready to draw the first line
        
         StringBuilder roundSummaryText = new StringBuilder();
         ModelUtilities.summarizeFirstLineRoundSummary(roundSummaryText, mPlayers, summary.getRoundResult());
        
         c.drawText(roundSummaryText.toString(), 0, -ViewUtilities.scaleText(context, 4), mPaint);
         
         c.translate(0, ViewUtilities.computeLineHeight(mContext, mFontMetrics));
         
         roundSummaryText.setLength(0);
         ModelUtilities.summarizeSecondLineRoundResult(roundSummaryText, mPlayers, summary.getRoundResult());
         c.drawText(roundSummaryText.toString(), 0, -ViewUtilities.scaleText(context, 4), mPaint);
         
         
         Paint fillPaint;
         if(summary.getRoundResult().getMade() >= summary.getRoundResult().getBid()){
             fillPaint = mCheckMarkPaint;
             fillPaint.setShader(new LinearGradient(0, 0, computeRoundSummaryWidth(mRoundSummaries), 0, 0x00006600, 0xff006600, TileMode.MIRROR));
         } else {
             fillPaint = mXMarkPaint;
             fillPaint.setShader(new LinearGradient(0, 0, computeRoundSummaryWidth(mRoundSummaries), 0, 0x00ff0000, 0xffff0000, TileMode.MIRROR));
         }
         
         c.drawRect(-ViewUtilities.scaleText(context, 4), -computeHeight() - ViewUtilities.scaleText(context, 4), computeRoundSummaryWidth(mRoundSummaries) + ViewUtilities.scaleText(context, 4), ViewUtilities.scaleText(context, 4), fillPaint);
         
    }

    @Override
    public void drawRoundScore(Context context, Canvas c, int score) {
        c.save();
        c.translate(0, ViewUtilities.computeLineHeight(context, mFontMetrics) * 0.5f);
        
        String textToDraw = "" + score;
        float textWidth = mPaint.measureText(textToDraw);

        float widthPerPlayer = getWidthPerPlayer();
        
        c.drawText(textToDraw,
                ViewUtilities.computeCentredStringStart(0, widthPerPlayer, textWidth),
                -ViewUtilities.scaleText(context, 4),
                mPaint);
        
        c.restore();
    }

    @Override
    public float computeRoundSummaryWidth(List<RoundSummary> roundSummaries) {
        
        float maxWidth = 0;
        
        StringBuilder roundSummaryText = new StringBuilder();
        
        for (RoundSummary roundSummary : roundSummaries) {
            ModelUtilities.summarizeFirstLineRoundSummary(roundSummaryText, mPlayers, roundSummary.getRoundResult());
           
            maxWidth = Math.max(maxWidth, mPaint.measureText(roundSummaryText.toString()));
            
            roundSummaryText.setLength(0);
            ModelUtilities.summarizeSecondLineRoundResult(roundSummaryText, mPlayers, roundSummary.getRoundResult());
            maxWidth = Math.max(maxWidth, mPaint.measureText(roundSummaryText.toString()));
            roundSummaryText.setLength(0);

        }
        
        return  maxWidth + 5;
    }

    @Override
    public float getWidthPerPlayer() {
        return (mTotalWidth - computeRoundSummaryWidth(mRoundSummaries))/mPlayers.size();
    }

}
