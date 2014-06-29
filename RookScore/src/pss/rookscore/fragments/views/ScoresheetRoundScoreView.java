
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

public class ScoresheetRoundScoreView extends View {

    private Paint mTextPaint;
    private Paint mLinePaint;
    private int mRound;
    private GameStateModel mModel;
    private List<RoundSummary> mRoundSummaries;

    public ScoresheetRoundScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextPaint = new Paint();
        mTextPaint.setTextSize(ViewUtilities.scaleText(getContext(), ViewUtilities.TEXT_SIZE));

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.LTGRAY);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(2f);
    }

    public void setRound(int round) {
        mRound = round;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mModel == null){
            //not ready to draw yet
            return;
        }
        
        ArrayList<String> playerNames = mModel.getPlayers();

        // sort in order of score, if possible
        if (mRoundSummaries.size() > 0) {
            ViewUtilities.sortPlayerNames(playerNames, mModel.getRounds(), mRoundSummaries);
        }

        if (playerNames != null && playerNames.size() > 0) {
            // evenly allocate width to players, draw their names

            int roundSummaryWidth = (int) ViewUtilities.computeRoundSummaryWidth(mRoundSummaries, mTextPaint, mModel.getPlayers());
            int widthAvailable = getWidth() - roundSummaryWidth;
            float widthPerPlayer = widthAvailable / playerNames.size();

            StringBuilder roundSummaryText = new StringBuilder();

            RoundSummary summary = mRoundSummaries.get(mRound);

            canvas.translate(0, ViewUtilities.computeRowHeight(mTextPaint, getContext()));

            // display the score per player, and then the round summary
            for (int i = 0; i < playerNames.size(); i++) {

                // use paint to clip text
                String playerName = playerNames.get(i);

                // TODO: Special case for numChars == 0: reduce font size?

                String textToDraw = "" + summary.getRoundCumulativeScores().get(playerName);
                float textWidth = mTextPaint.measureText(textToDraw);

                float leftmost = i * widthPerPlayer;

                canvas.drawText(textToDraw, ViewUtilities.computeCentredStringStart(leftmost,
                        widthPerPlayer, textWidth), -ViewUtilities.scaleText(getContext(), 4),
                        mTextPaint);
            }

            roundSummaryText.setLength(0);

            ViewUtilities.summarizeRoundResult(roundSummaryText, summary.getRoundResult(),
                    playerNames);

            float summaryX = getWidth() - roundSummaryWidth;

            canvas.drawText(roundSummaryText.toString(), summaryX,
                    -ViewUtilities.scaleText(getContext(), 4), mTextPaint);

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec),
                (int) ViewUtilities.computeRowHeight(mTextPaint, getContext()) + 5);
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
