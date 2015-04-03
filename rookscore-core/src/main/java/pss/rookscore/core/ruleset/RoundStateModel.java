package pss.rookscore.core.ruleset;

import java.io.Serializable;
import java.util.ArrayList;

import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.Player;


public class RoundStateModel implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private RoundController.RoundState mState;
//    private String mCaller;
//    private int mBid;
//    private List<String> mPartners= new ArrayList<String>();
//    private int mMade;

    private GameStateModel.RoundResult mRoundResult;
    private RookRuleSet mRules;
    
    public RoundStateModel(RookRuleSet rules) {
        mRoundResult = new GameStateModel.RoundResult(rules, null, new ArrayList<Player>(), 0, 0);
        mRules = rules;
    }

    public RoundStateModel(RoundStateModel toCopy) {
        mState = toCopy.mState;
        mRoundResult = new GameStateModel.RoundResult(toCopy.getRules(), toCopy.getRoundResult().getCaller(), toCopy.getRoundResult().getPartners(), toCopy.getRoundResult().getBid(), toCopy.getRoundResult().getMade());
    }
    
    public RookRuleSet getRules() {
        return mRules;
    }
    
    public RoundController.RoundState getState() {
        return mState;
    }

    public void setState(RoundController.RoundState state) {
        mState = state;
    }
    
    public GameStateModel.RoundResult getRoundResult() {
        return mRoundResult;
    }


}