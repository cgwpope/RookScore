
package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.RoundSummary;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;

public class ScoresheetHeaderView extends View {

    private final Paint mPaint;
    private GameStateModel mModel;
    private List<RoundSummary> mRoundScores;
    private boolean mUseFullWidth;
    private DrawStrategy mDrawStrategy;
    private StarPath mStarPath;
    private ValueAnimator mAnimator;

    public ScoresheetHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(ViewUtilities.defaultTextPaint(context));
        mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        mStarPath = new StarPath(context);
        
        mAnimator = ValueAnimator.ofInt(-20, 20);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(2000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mStarPath.setAlphaOffset((Integer)animation.getAnimatedValue());
                invalidate();
            }
        });
        
        
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        
//        mAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mModel != null) {
            ArrayList<String> playerNames = mModel.getPlayers();
            
            
            //sort in order of score, if possible
            if(mRoundScores.size() > 0){
                ViewUtilities.sortPlayerNames(playerNames, mModel.getRounds(), mRoundScores);
            }
            

            if (playerNames != null && playerNames.size() > 0) {
                // evenly allocate width to players, draw their names

                float roundSummaryWidth;
                if (mUseFullWidth) {
                    roundSummaryWidth = 0;
                } else {
                    roundSummaryWidth = mDrawStrategy.computeRoundSummaryWidth(mRoundScores);
                }

                float widthAvailable = getWidth() - roundSummaryWidth;

                float widthPerPlayer = widthAvailable / playerNames.size();

                for (int i = 0; i < playerNames.size(); i++) {

                    
                    // use paint to clip text
                    String playerName = playerNames.get(i);
                    
                    
                    // draw backing star if required
                    if (ViewUtilities.playerHasWonARound(playerName, mModel.getRounds())) {
                        
                        float starX = widthPerPlayer / 2;
                        float starY = ViewUtilities.computeLineHeight(getContext(), mPaint); 
                        
                        canvas.save();
                        canvas.translate(starX, starY);
                        
                        mStarPath.drawToCanvas(canvas, mPaint.getTextSize());
                        canvas.restore();
                        
                    }
                    
                    
                    int numChars = mPaint.breakText(playerName, true, widthPerPlayer, null);

                    // TODO: Special case for numChars == 0: reduce font size?

                    String textToDraw;
                    if (numChars < playerName.length()) {
                        // try initials
                        String shorterName = ViewUtilities.shorterName(playerNames, playerName);
                        numChars = mPaint.breakText(shorterName, true, widthPerPlayer, null);
                        if (numChars < shorterName.length()) {
                            textToDraw = playerName.substring(0, numChars);
                        } else {
                            textToDraw = shorterName;
                        }
                    } else {
                        textToDraw = playerName;
                    }

                    float playerNameWidth = mPaint.measureText(textToDraw);
                    canvas.drawText(textToDraw, ViewUtilities.computeCentredStringStart(0, widthPerPlayer, playerNameWidth), mPaint.getTextSize(), mPaint);

                    textToDraw = ""
                            + (mRoundScores.size() > 0 ? mRoundScores.get(mRoundScores.size() - 1)
                                    .getRoundCumulativeScores().get(playerName) : 0);



                    // now draw the player's score
                    float scoreWidth = mPaint.measureText(textToDraw);
                    canvas.drawText(textToDraw,
                            ViewUtilities.computeCentredStringStart(0, widthPerPlayer, scoreWidth),
                            mPaint.getTextSize() * 2, mPaint);

                    canvas.translate(widthPerPlayer, 0);

                }
            }
        }

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        mDrawStrategy = DrawStrategyFactory.buildDrawStrategy(getContext(), ViewUtilities.defaultTextPaint(getContext()), mModel.getPlayers(), mModel.computeRoundScores(), width);
        setMeasuredDimension(width, (int)ViewUtilities.computeLineHeight(getContext(), mPaint) * 2);
    }

    public void setGameStateModel(GameStateModel model) {
        mModel = model;
        scoreUpdated();
    }

    public void scoreUpdated() {
        mRoundScores = mModel.computeRoundScores();
        invalidate();
        requestLayout();
    }

    public boolean getUseFullWidth() {
        return mUseFullWidth;
    }

    public void setUseFullWidth(boolean useFullWidth) {
        mUseFullWidth = useFullWidth;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        mAnimator.end();
    }

}
