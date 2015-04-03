package pss.rookscore.core.model;

import java.util.Map;

import pss.rookscore.core.model.GameStateModel.RoundResult;

public class RoundSummary {
    private final RoundResult mRoundResult;
    private final Map<Player, Integer> mRoundScores;

    public RoundSummary(RoundResult roundResult, Map<Player, Integer> roundScores) {
        super();
        mRoundResult = roundResult;
        mRoundScores = roundScores;
    }
    
    public RoundResult getRoundResult() {
        return mRoundResult;
    }
    
    public Map<Player, Integer> getRoundCumulativeScores() {
        return mRoundScores;
    }
}