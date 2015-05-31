
package pss.rookscore.fragments.views;

import java.util.List;

import pss.rookscore.core.model.GameStateModel.RoundResult;
import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.model.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;

public class SingleLineDrawStrategy implements DrawStrategy {

    private final int mTotalWidth;
    private final List<Player> mPlayers;
    private final Paint mPaint;
    private final Paint.FontMetrics mFontMetrics;

    private final LinearGradient mSuccessGradient;
    private final LinearGradient mFailGradient;

    private Context mContext;
    private Paint mCheckMarkPaint;
    private Paint mXMarkPaint;
    private float mRoundSummaryWidth;

    private final StringBuilder mScratchStringBuilder = new StringBuilder(100);

    static final int DEFAULT_ROUND_SUMMARY_WIDTH = 100;

    public SingleLineDrawStrategy(Context context, Paint p, List<Player> players, List<RoundSummary> roundSummaries, int totalWidth) {
        mContext = context;
        mTotalWidth = totalWidth;
        mPlayers = players;

        mPaint = p;
        mFontMetrics = mPaint.getFontMetrics();
        
        mCheckMarkPaint = new Paint();
        mCheckMarkPaint.setStyle(Style.FILL);

        
        mXMarkPaint = new Paint();
        mXMarkPaint.setStyle(Style.FILL);
        
        
        mRoundSummaryWidth = computeRoundSummaryWidth(roundSummaries);
        
        mSuccessGradient = new LinearGradient(0, 0, mRoundSummaryWidth, 0, 0x00006600, 0x80006600, TileMode.MIRROR);
        mFailGradient = new LinearGradient(0, 0, mRoundSummaryWidth, 0, 0x00ff0000, 0x60ff0000, TileMode.MIRROR);
        
        

    }

    @Override
    public float computeHeight() {
        return ViewUtilities.computeLineHeight(mContext, mFontMetrics);
    }

    @Override
    public void drawRoundSummary(Context context, Canvas c, RoundSummary roundSummary) {
        mScratchStringBuilder.setLength(0);
        summarizeRoundResult(mScratchStringBuilder, roundSummary.getRoundResult(), mPlayers);
        c.drawText(mScratchStringBuilder, 0, mScratchStringBuilder.length(), 0, -ViewUtilities.scaleText(context, 4), mPaint);
        
        Paint fillPaint;
        if(roundSummary.getRoundResult().getMade() >= roundSummary.getRoundResult().getBid()){
            fillPaint = mCheckMarkPaint;
            fillPaint.setShader(mSuccessGradient);
        } else {
            fillPaint = mXMarkPaint;
            fillPaint.setShader(mFailGradient);
        }
        
        c.drawRect(-ViewUtilities.scaleText(context, 4), -computeHeight() - ViewUtilities.scaleText(context, 4), mTotalWidth - mRoundSummaryWidth + ViewUtilities.scaleText(context, 4), ViewUtilities.scaleText(context, 4), fillPaint);

    }

    @Override
    public void drawRoundScore(Context context,  Canvas canvas, int score) {
        mScratchStringBuilder.setLength(0);
        mScratchStringBuilder.append(score);
        float textWidth = mPaint.measureText(mScratchStringBuilder, 0, mScratchStringBuilder.length());

        float widthPerPlayer = getWidthPerPlayer();
        
        canvas.drawText(mScratchStringBuilder, 0, mScratchStringBuilder.length(),
                ViewUtilities.computeCentredStringStart(0, widthPerPlayer, textWidth),
                -ViewUtilities.scaleText(context, 4),
                mPaint);
        

    }


    @Override
    public float computeRoundSummaryWidth(List<RoundSummary> roundSummaries) {
        float maxWidth = 0;

        for (RoundSummary roundSummary : roundSummaries) {
            float length;
            mScratchStringBuilder.setLength(0);
            summarizeRoundResult(mScratchStringBuilder, roundSummary.getRoundResult(), mPlayers);
            length = mPaint.measureText(mScratchStringBuilder, 0, mScratchStringBuilder.length());

            // add a bit of padding around text
            length += 10;

            if (length > maxWidth) {
                maxWidth = length;
            }
        }
        return maxWidth;
    }

    private void summarizeRoundResult(StringBuilder roundSummaryText, RoundResult roundResult,List<Player> playerS) {
        ModelUtilities.summarizeCompleteRoundResult(roundSummaryText, roundResult, mPlayers);
    }
    
    @Override
    public float getWidthPerPlayer() {
        return (mTotalWidth - mRoundSummaryWidth)/ mPlayers.size();
    }

}
