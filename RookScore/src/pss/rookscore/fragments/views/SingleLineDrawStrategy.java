
package pss.rookscore.fragments.views;

import java.util.List;

import pss.rookscore.model.GameStateModel.RoundResult;
import pss.rookscore.model.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;

public class SingleLineDrawStrategy implements DrawStrategy {

    private final int mTotalWidth;
    private final List<String> mPlayerNames;
    private final Paint mPaint;
    private Context mContext;
    private List<RoundSummary> mRoundSummaries;
    private Paint mCheckMarkPaint;
    private Paint mXMarkPaint;

    static final int DEFAULT_ROUND_SUMMARY_WIDTH = 100;

    public SingleLineDrawStrategy(Context context, Paint p, List<String> playerNames, List<RoundSummary> roundSummaries, int totalWidth) {
        mContext = context;
        mTotalWidth = totalWidth;
        mPlayerNames = playerNames;
        mRoundSummaries = roundSummaries;
        mPaint = p;
        
        mCheckMarkPaint = new Paint();
        mCheckMarkPaint.setStyle(Style.FILL);

        
        mXMarkPaint = new Paint();
        mXMarkPaint.setStyle(Style.FILL);

    }

    @Override
    public float computeHeight() {
        return ViewUtilities.computeLineHeight(mContext, mPaint);
    }

    @Override
    public void drawRoundSummary(Context context, Canvas c, RoundSummary roundSummary) {
        StringBuilder roundSummaryText = new StringBuilder();
        summarizeRoundResult(roundSummaryText, roundSummary.getRoundResult(), mPlayerNames);
        c.drawText(roundSummaryText.toString(), 0, -ViewUtilities.scaleText(context, 4), mPaint);
        
        Paint fillPaint;
        if(roundSummary.getRoundResult().getMade() >= roundSummary.getRoundResult().getBid()){
            fillPaint = mCheckMarkPaint;
            fillPaint.setShader(new LinearGradient(0, 0, computeRoundSummaryWidth(mRoundSummaries), 0, 0x00006600, 0x80006600, TileMode.MIRROR));
        } else {
            fillPaint = mXMarkPaint;
            fillPaint.setShader(new LinearGradient(0, 0, computeRoundSummaryWidth(mRoundSummaries), 0, 0x00ff0000, 0x60ff0000, TileMode.MIRROR));
        }
        
        c.drawRect(-ViewUtilities.scaleText(context, 4), -computeHeight() - ViewUtilities.scaleText(context, 4), mTotalWidth - computeRoundSummaryWidth(mRoundSummaries) + ViewUtilities.scaleText(context, 4), ViewUtilities.scaleText(context, 4), fillPaint);

    }

    @Override
    public void drawRoundScore(Context context,  Canvas canvas, int score) {
        String textToDraw = "" + score;
        float textWidth = mPaint.measureText(textToDraw);

        float widthPerPlayer = getWidthPerPlayer();
        
        canvas.drawText(textToDraw,
                ViewUtilities.computeCentredStringStart(0, widthPerPlayer, textWidth),
                -ViewUtilities.scaleText(context, 4),
                mPaint);
        

    }


    @Override
    public float computeRoundSummaryWidth(List<RoundSummary> roundSummaries) {
        float maxWidth = 0;

        for (RoundSummary roundSummary : roundSummaries) {
            float length;
            StringBuilder sb = new StringBuilder();
            summarizeRoundResult(sb, roundSummary.getRoundResult(), mPlayerNames);
            length = mPaint.measureText(sb.toString());

            // add a bit of padding around text
            length += 10;

            if (length > maxWidth) {
                maxWidth = length;
            }
        }
        return maxWidth;
    }

    private void summarizeRoundResult(StringBuilder roundSummaryText, RoundResult roundResult,
            List<String> playerNames) {
        ViewUtilities.summarizeCompleteRoundResult(roundSummaryText, roundResult, mPlayerNames);
    }
    
    @Override
    public float getWidthPerPlayer() {
        return (mTotalWidth - computeRoundSummaryWidth(mRoundSummaries))/mPlayerNames.size();
    }

}