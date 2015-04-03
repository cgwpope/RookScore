
package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.model.RoundSummary;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ScoresheetHeaderView extends View {

    private final Paint mPaint;
    private GameStateModel mModel;
    private List<RoundSummary> mRoundScores;
    
    private float mFractionReservedForSummaryColumn = 1f;
    
    private DrawStrategy mDrawStrategy;
    private StarPath mStarPath;
    private ValueAnimator mAnimator;
    private List<Player> mPlayerNames;
    private float mCalculatedRoundSummaryWidth;
    private ArrayList<String> mShorterPlayerNames;
    private boolean[] mPlayerHasWonRounds;

    public ScoresheetHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(ViewUtilities.defaultTextPaint(context));
        mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        mStarPath = new StarPath(context);
        
        mAnimator = ValueAnimator.ofInt(-30, 30);
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
        
        
        mAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mModel != null) {
            
            

            if (mPlayerNames != null && mPlayerNames.size() > 0) {
                // evenly allocate width to players, draw their names

                float roundSummaryWidth = mCalculatedRoundSummaryWidth * mFractionReservedForSummaryColumn;

                float widthAvailable = getWidth() - roundSummaryWidth;

                float widthPerPlayer = widthAvailable / mPlayerNames.size();

                for (int i = 0; i < mPlayerNames.size(); i++) {

                    
                    // use paint to clip text
                    Player player = mPlayerNames.get(i);
                    
                    
                    // draw backing star if required
                    if (mPlayerHasWonRounds[i]) {
                        
                        float starX = widthPerPlayer / 2;
                        float starY = ViewUtilities.computeLineHeight(getContext(), mPaint); 
                        
                        canvas.save();
                        canvas.translate(starX, starY);
                        
                        mStarPath.drawToCanvas(canvas, mPaint.getTextSize());
                        canvas.restore();
                        
                    }
                    
                    // TODO: Special case for numChars == 0: reduce font size?
                    String textToDraw;
                    if (!willStringFit(widthPerPlayer, player.toString())) {
                        // try initials
                        textToDraw = mShorterPlayerNames.get(i);

                        if(!willStringFit(widthPerPlayer, textToDraw)){
                            int numChars = mPaint.breakText(player.toString(), false, widthPerPlayer, null);
                            textToDraw = player.toString().substring(0, numChars);
                        } 
                        
                    } else {
                        textToDraw = player.toString();
                    }

                    //cache?
                    float playerNameWidth = mPaint.measureText(textToDraw);
                    canvas.drawText(textToDraw, ViewUtilities.computeCentredStringStart(0, widthPerPlayer, playerNameWidth), mPaint.getTextSize(), mPaint);

                    textToDraw = ""
                            + (mRoundScores.size() > 0 ? mRoundScores.get(mRoundScores.size() - 1)
                                    .getRoundCumulativeScores().get(player) : 0);



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

    
    private float mLastWidthPerPlayer;
    private Map<String, Boolean> cachedBreakText = new HashMap<String, Boolean>();
    boolean willStringFit(float widthPerPlayer, String playerName) {
        
        if(widthPerPlayer != mLastWidthPerPlayer){
            mLastWidthPerPlayer = widthPerPlayer;
            cachedBreakText.clear();
        }
        
        if(cachedBreakText.containsKey(playerName)){
            return cachedBreakText.get(playerName);
        } else {
            float width = mPaint.measureText(playerName);
            boolean result = width < widthPerPlayer;
            cachedBreakText.put(playerName, result);
            return result;
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

        mCalculatedRoundSummaryWidth = mDrawStrategy.computeRoundSummaryWidth(mRoundScores);

        setMeasuredDimension(width, (int)ViewUtilities.computeLineHeight(getContext(), mPaint) * 2);
    }

    public void setGameStateModel(GameStateModel model) {
        mModel = model;
        scoreUpdated();
    }

    public void scoreUpdated() {
        mRoundScores = mModel.computeRoundScores();
        
        //initialize invariants
        mPlayerNames = new ArrayList<>(mModel.getPlayers());

        //sort in order of score, if possible

        if(mRoundScores.size() > 0){
            ModelUtilities.sortPlayerNames(mPlayerNames, mModel.getRounds(), mRoundScores);
        }

        
        mShorterPlayerNames = new ArrayList<String>();
        for(int i = 0; i < mPlayerNames.size(); i++){
            mShorterPlayerNames.add(ModelUtilities.shorterName(mPlayerNames, mPlayerNames.get(i)));
        }
        
        
        mPlayerHasWonRounds = new boolean[mPlayerNames.size()];
        for(int i = 0; i < mPlayerNames.size(); i++){
            mPlayerHasWonRounds[i] = ModelUtilities.playerHasWonARound(mPlayerNames.get(i), mModel.getRounds());
        }

        
        invalidate();
        requestLayout();
    }


//    public void setUseFullWidth(boolean useFullWidth) {
//        mUseFullWidth = useFullWidth;
//    }
    
    public void setFractionReservedForSummaryColumn(float fractionReservedForSummaryColumn) {
        mFractionReservedForSummaryColumn = fractionReservedForSummaryColumn;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        mAnimator.end();
    }

}
