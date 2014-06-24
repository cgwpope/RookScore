package pss.rookscore.model;

import java.util.Map;

import pss.rookscore.model.GameStateModel.RoundResult;

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
    
    public Map<String, Integer> getRoundCumulativeScores() {
        return mRoundScores;
    }
}