package pss.rookscore.fragments.views;

import java.util.List;

import pss.rookscore.model.RoundSummary;
import android.content.Context;
import android.graphics.Canvas;

public class NullDrawStrategy implements DrawStrategy {

    @Override
    public float computeHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void drawRoundSummary(Context context, Canvas c, RoundSummary summary) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawRoundScore(Context context, Canvas c, int score) {
        // TODO Auto-generated method stub

    }

    @Override
    public float computeRoundSummaryWidth(List<RoundSummary> roundSummaries) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getWidthPerPlayer() {
        // TODO Auto-generated method stub
        return 0;
    }

}
