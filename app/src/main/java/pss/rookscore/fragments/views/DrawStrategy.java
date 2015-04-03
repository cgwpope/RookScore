package pss.rookscore.fragments.views;

import java.util.List;

import pss.rookscore.core.model.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;

public interface DrawStrategy {
    public float computeHeight();
    public void drawRoundSummary(Context context, Canvas c, RoundSummary summary);
    public void drawRoundScore(Context context, Canvas c, int score);
    float computeRoundSummaryWidth(List<RoundSummary> roundSummaries);
    public float getWidthPerPlayer();

}
