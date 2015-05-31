
package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.model.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScoresheetRoundScoreView extends View {

    private final Paint mTextPaint;
    private final Paint.FontMetrics mFontMetrics;

    private int mRound;
    private GameStateModel mModel;
    private List<RoundSummary> mRoundSummaries;
    private DrawStrategy mDrawStrategy;
    private float mRoundSummaryWidth;
    private List<RoundSummary> mRoundScores;

    public ScoresheetRoundScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextPaint = ViewUtilities.defaultTextPaint(context);
        mFontMetrics = mTextPaint.getFontMetrics();
        
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

        ArrayList<Player> players = mModel.getPlayers();

        // sort in order of score, if possible
        if (mRoundSummaries.size() > 0) {
            ModelUtilities.sortPlayerNames(players, mModel.getRounds(), mRoundSummaries);
        }

        if (players != null && players.size() > 0) {
            // evenly allocate width to players, draw their names


            canvas.save();
            
            canvas.translate(0, ViewUtilities.computeLineHeight(getContext(), mFontMetrics));

            // display the score per player, and then the round summary
            RoundSummary summary = mRoundSummaries.get(mRound);

            for (int i = 0; i < players.size(); i++) {

                // use paint to clip text
                Player player = players.get(i);

                mDrawStrategy.drawRoundScore(getContext(), canvas, summary.getRoundCumulativeScores().get(player));
                
                //then translate to next slow
                canvas.translate(mDrawStrategy.getWidthPerPlayer(), 0);
            }
            
            canvas.restore();
            
            //we are done painting the player scores. Move to the round summary, ready to draw texty
            canvas.translate(getWidth() - mRoundSummaryWidth, ViewUtilities.computeLineHeight(getContext(), mFontMetrics));

            mDrawStrategy.drawRoundSummary(getContext(), canvas, summary);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mModel == null){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            mDrawStrategy = DrawStrategyFactory.buildDrawStrategy(getContext(), mTextPaint, mModel.getPlayers(), getCachedRoundScores(), width);
            mRoundSummaryWidth = mDrawStrategy.computeRoundSummaryWidth(mRoundSummaries);
            setMeasuredDimension(width, (int)mDrawStrategy.computeHeight() + 5);
        }
    }
    


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setGameStateModel(GameStateModel model) {

        GameStateModel oldModel = mModel;
        mModel = model;
//        mDrawStrategy = DrawStrategyFactory.buildDrawStrategy(getContext(), mTextPaint, mModel.getPlayers(), mModel.computeRoundScores(), getWidth());

        if(!mModel.equals(oldModel)){
            scoreUpdated();
        }


    }

    public void scoreUpdated() {
        mRoundSummaries = mModel.computeRoundScores();
        invalidate();
        requestLayout();
        
    }

    private List<RoundSummary> getCachedRoundScores() {
        if(mRoundScores != null){
            return mRoundScores;
        } else {
            return mModel.computeRoundScores();
        }
    }
    
    public void setRoundScores(List<RoundSummary> computeRoundScores) {
        mRoundScores = computeRoundScores;
    }
    


}
