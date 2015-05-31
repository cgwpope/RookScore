
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

import com.google.common.base.Optional;

public class ScoresheetRoundScoreView extends View {

    public static class DrawStrategyHolder {
        private DrawStrategy mDS;

        public DrawStrategy getDS() {
            return mDS;
        }

        public void setDS(DrawStrategy DS) {
            mDS = DS;
        }
    }


    private final Paint mTextPaint;
    private final Paint.FontMetrics mFontMetrics;

    private int mRound;



    private float mRoundSummaryWidth;
    private List<Player> mPlayers;
    private List<RoundSummary> mRoundScores;

    private DrawStrategyHolder mDrawStrategy = new DrawStrategyHolder();





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

        if (!initialized()) {
            // not ready to draw yet
            return;
        }

        if (mPlayers != null && mPlayers.size() > 0) {
            // evenly allocate width to players, draw their names


            canvas.save();
            
            canvas.translate(0, ViewUtilities.computeLineHeight(getContext(), mFontMetrics));

            // display the score per player, and then the round summary
            RoundSummary summary = mRoundScores.get(mRound);

            for (int i = 0; i < mPlayers.size(); i++) {

                // use paint to clip text
                Player player = mPlayers.get(i);

                mDrawStrategy.getDS().drawRoundScore(getContext(), canvas, summary.getRoundCumulativeScores().get(player));
                
                //then translate to next slow
                canvas.translate(mDrawStrategy.getDS().getWidthPerPlayer(), 0);
            }
            
            canvas.restore();
            
            //we are done painting the player scores. Move to the round summary, ready to draw texty
            canvas.translate(getWidth() - mRoundSummaryWidth, ViewUtilities.computeLineHeight(getContext(), mFontMetrics));

            mDrawStrategy.getDS().drawRoundSummary(getContext(), canvas, summary);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(!initialized()){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int width = View.MeasureSpec.getSize(widthMeasureSpec);

            if(mDrawStrategy.getDS() == null){
                mDrawStrategy.setDS(DrawStrategyFactory.buildDrawStrategy(getContext(), mTextPaint, mPlayers, mRoundScores, width));
            }

            mRoundSummaryWidth = mDrawStrategy.getDS().computeRoundSummaryWidth(mRoundScores);
            setMeasuredDimension(width, (int)mDrawStrategy.getDS().computeHeight() + 5);
        }
    }

    private boolean initialized() {
        return mPlayers != null && mRoundScores != null;
    }

    public void scoreUpdated() {
        invalidate();
        requestLayout();
        
    }

    public void setDrawStrategy(DrawStrategyHolder drawStrategy) {
        mDrawStrategy = drawStrategy;
    }

    public void setRoundScores(List<RoundSummary> computeRoundScores) {
        mRoundScores = computeRoundScores;
    }

    public void setPlayers(List<Player> players) {
        mPlayers = players;
    }
}
