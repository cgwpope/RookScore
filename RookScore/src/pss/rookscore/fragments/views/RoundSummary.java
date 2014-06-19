package pss.rookscore.fragments.views;

import java.util.Map;

import pss.rookscore.GameStateModel.RoundResult;

public class RoundSummary {
    private final RoundResult mRoundResult;
    private final Map<String, Integer> mRoundScores;

    public RoundSummary(RoundResult roundResult, Map<String, Integer> roundScores) {
        super();
        mRoundResult = roundResult;
        mRoundScores = roundScores;
    }
    
    public RoundResult getRoundResult() {
        return mRoundResult;
    }
    
    public Map<String, Integer> getRoundScores() {
        return mRoundScores;
    }
}