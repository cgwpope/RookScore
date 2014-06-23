
package pss.rookscore.ruleset;

import java.util.ArrayList;
import java.util.List;

public class RoundController {

    private final RookRuleSet mRules;
    private RoundState mState;
    private String mCaller;
    private int mBid;
    private List<String> mPartners = new ArrayList<String>();
    private int mMade;

    public static enum RoundState {
        COLLECT_CALLER,
        COLLECT_BID,
        COLLECT_PARTNER,
        COLLECT_MADE_BID
    }

    public RoundController(RookRuleSet rules) {
        this(rules, RoundState.COLLECT_CALLER);
    }

    public RoundController(RookRuleSet rules, RoundState state) {
        mRules = rules;
        mState = state;
    }

    public RoundState nextState() {

        switch (mState) {
            case COLLECT_CALLER:
                return RoundState.COLLECT_BID;
            case COLLECT_BID:
                if (mRules.hasFixedPartners()) {
                    return RoundState.COLLECT_MADE_BID;
                } else {
                    return RoundState.COLLECT_PARTNER;
                }
            case COLLECT_PARTNER:
                if (mPartners.size() < mRules.getNumberOfPartners()) {
                    return RoundState.COLLECT_PARTNER;
                } else {
                    return RoundState.COLLECT_MADE_BID;
                }
            default:
                return null;
        }
    }

    public int getBid() {
        return mBid;
    }

    public void setBid(int bid) {
        mBid = bid;
    }

    public int getMade() {
        return mMade;
    }

    public void setMade(int made) {
        mMade = made;
    }

    public List<String> getPartners() {
        return mPartners;
    }

    public String getCaller() {
        return mCaller;
    }

    public void setCaller(String caller) {
        mCaller = caller;
    }

    public RoundState getState() {
        return mState;
    }
    
    public void setState(RoundState state) {
        mState = state;
    }

    public void playerSelected(String playerName) {
        if(mState == RoundState.COLLECT_CALLER){
            mCaller = playerName;
        } else if(mState == RoundState.COLLECT_PARTNER){
            mPartners.add(playerName);
        }
    }

    public void applyBid(int bid) {
        if(mState == RoundState.COLLECT_BID){
            mBid = bid;
        } else if(mState == RoundState.COLLECT_MADE_BID){
            mMade =  bid;
        }
    }

}
