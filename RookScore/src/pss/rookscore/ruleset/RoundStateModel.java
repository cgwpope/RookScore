package pss.rookscore.ruleset;

import java.io.Serializable;
import java.util.ArrayList;

import pss.rookscore.model.GameStateModel.RoundResult;
import pss.rookscore.ruleset.RoundController.RoundState;

public class RoundStateModel implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private RoundState mState;
//    private String mCaller;
//    private int mBid;
//    private List<String> mPartners= new ArrayList<String>();
//    private int mMade;

    private RoundResult mRoundResult;
    private RookRuleSet mRules;
    
    public RoundStateModel(RookRuleSet rules) {
        mRoundResult = new RoundResult(rules, null, new ArrayList<String>(), 0, 0);
        mRules = rules;
    }

    public RoundStateModel(RoundStateModel toCopy) {
        mState = toCopy.mState;
        mRoundResult = new RoundResult(toCopy.getRules(), toCopy.getRoundResult().getCaller(), toCopy.getRoundResult().getPartners(), toCopy.getRoundResult().getBid(), toCopy.getRoundResult().getMade());
    }
    
    public RookRuleSet getRules() {
        return mRules;
    }
    
    public RoundState getState() {
        return mState;
    }

    public void setState(RoundState state) {
        mState = state;
    }
    
    public RoundResult getRoundResult() {
        return mRoundResult;
    }


}