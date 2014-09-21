
package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScoresheetRoundScoreView extends View {

    private Paint mTextPaint;
    private int mRound;
    private GameStateModel mModel;
    private List<RoundSummary> mRoundSummaries;
    private DrawStrategy mDrawStrategy;

    public ScoresheetRoundScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextPaint = ViewUtilities.defaultTextPaint(context);
        
        //by default, assume a single-line draw strategy, but alter once more data available
//        mDrawStrategy = new SingleLineDrawStrategy(context, mTextPaint, new ArrayList<String>(), new ArrayList<RoundSummary>(), getWidth());

    }

    public void setRound(int round) {
        mRound = round;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mModel == null) {
            // not ready to draw yet
            return;
        }

        ArrayList<String> playerNames = mModel.getPlayers();

        // sort in order of score, if possible
        if (mRoundSummaries.size() > 0) {
            ViewUtilities.sortPlayerNames(playerNames, mModel.getRounds(), mRoundSummaries);
        }

        if (playerNames != null && playerNames.size() > 0) {
            // evenly allocate width to players, draw their names

            float roundSummaryWidth = mDrawStrategy.computeRoundSummaryWidth(mRoundSummaries);

            canvas.save();
            
            canvas.translate(0, ViewUtilities.computeLineHeight(getContext(), mTextPaint));

            // display the score per player, and then the round summary
            RoundSummary summary = mRoundSummaries.get(mRound);

            for (int i = 0; i < playerNames.size(); i++) {

                // use paint to clip text
                String playerName = playerNames.get(i);

                mDrawStrategy.drawRoundScore(getContext(), canvas, summary.getRoundCumulativeScores().get(playerName));
                
                //then translate to next slow
                canvas.translate(mDrawStrategy.getWidthPerPlayer(), 0);
            }
            
            canvas.restore();
            
            //we are done painting the player scores. Move to the round summary, ready to draw texty
            canvas.translate(getWidth() - roundSummaryWidth, ViewUtilities.computeLineHeight(getContext(), mTextPaint));

            mDrawStrategy.drawRoundSummary(getContext(), canvas, summary);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        mDrawStrategy = DrawStrategyFactory.buildDrawStrategy(getContext(), mTextPaint, mModel.getPlayers(), mModel.computeRoundScores(), width);
        setMeasuredDimension(width, (int)mDrawStrategy.computeHeight() + 5);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setGameStateModel(GameStateModel model) {
        mModel = model;
//        mDrawStrategy = DrawStrategyFactory.buildDrawStrategy(getContext(), mTextPaint, mModel.getPlayers(), mModel.computeRoundScores(), getWidth());
        scoreUpdated();
    }

    public void scoreUpdated() {
        mRoundSummaries = mModel.computeRoundScores();
        invalidate();
        requestLayout();
        
    }
    


}
