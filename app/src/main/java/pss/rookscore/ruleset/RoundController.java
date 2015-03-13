
package pss.rookscore.ruleset;


import pss.rookscore.model.Player;

public class RoundController {

    private RoundStateModel mRoundState;
    private RookRuleSet mRules;

    
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
        mRoundState = new RoundStateModel(mRules);
        mRoundState.setState(state);
    }

    public RoundState nextState() {

        switch (mRoundState.getState()) {
            case COLLECT_CALLER:
                return RoundState.COLLECT_BID;
            case COLLECT_BID:
                if (mRules.hasFixedPartners()) {
                    return RoundState.COLLECT_MADE_BID;
                } else {
                    return RoundState.COLLECT_PARTNER;
                }
            case COLLECT_PARTNER:
                if (mRoundState.getRoundResult().getPartners().size() < mRules.getNumberOfPartners()) {
                    return RoundState.COLLECT_PARTNER;
                } else {
                    return RoundState.COLLECT_MADE_BID;
                }
            default:
                return null;
        }
    }

    public RookRuleSet getRules() {
        return mRules;
    }
    
    
    public RoundStateModel getRoundState() {
        return mRoundState;
    }
    
    public void setRoundState(RoundStateModel roundState) {
        mRoundState = roundState;
    }

    
    public void playerSelected(Player player) {
        if(mRoundState.getState() == RoundState.COLLECT_CALLER){
            mRoundState.getRoundResult().setCaller(player);
        } else if(mRoundState.getState() == RoundState.COLLECT_PARTNER){
            mRoundState.getRoundResult().getPartners().add(player);
        }
    }

    public void applyBid(int bid) {
        if(mRoundState.getState() == RoundState.COLLECT_BID){
            mRoundState.getRoundResult().setBid(bid);
        } else if(mRoundState.getState() == RoundState.COLLECT_MADE_BID){
            mRoundState.getRoundResult().setMade(bid);
        }
    }
}
