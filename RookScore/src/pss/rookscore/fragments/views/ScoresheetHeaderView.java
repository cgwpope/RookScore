
package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.RoundSummary;
import pss.rookscore.model.GameStateModel.RoundResult;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;

public class ScoresheetHeaderView extends View {

    private final Paint mPaint;
    private GameStateModel mModel;
    private List<RoundSummary> mRoundScores;
    private Drawable mStarDrawable;
    private boolean mUseFullWidth;
    private DrawStrategy mDrawStrategy;

    public ScoresheetHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(ViewUtilities.defaultTextPaint(context));
        mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        mStarDrawable = context.getResources().getDrawable(android.R.drawable.star_on);
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

                    // draw backing star if required
                    if (ViewUtilities.playerHasWonARound(playerName, mModel.getRounds())) {
                        int starX = (int) ViewUtilities.computeCentredStringStart(0,
                                widthPerPlayer, (int) mPaint.getTextSize() * 2);
                        int starWidth = (int) mPaint.getTextSize() * 2;
                        mStarDrawable.setBounds(starX, 0, starX + starWidth, starWidth);
                        mStarDrawable.setAlpha(75);
                        mStarDrawable.draw(canvas);
                    }

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
//        mDrawStrategy = DrawStrategyFactory.buildDrawStrategy(getContext(), mPaint, mModel.getPlayers(), mModel.computeRoundScores(), getWidth());
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

}
