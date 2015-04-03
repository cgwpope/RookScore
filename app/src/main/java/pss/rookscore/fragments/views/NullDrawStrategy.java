package pss.rookscore.fragments.views;

import java.util.List;

import pss.rookscore.core.model.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;

public class NullDrawStrategy implements DrawStrategy {

    @Override
    public float computeHeight() {
        return 0;
    }

    @Override
    public void drawRoundSummary(Context context, Canvas c, RoundSummary summary) {

    }

    @Override
    public void drawRoundScore(Context context, Canvas c, int score) {

    }

    @Override
    public float computeRoundSummaryWidth(List<RoundSummary> roundSummaries) {
        return 0;
    }

    @Override
    public float getWidthPerPlayer() {
        return 0;
    }

}
