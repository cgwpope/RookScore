package pss.rookscore.core.ruleset;

public class AllanFourPlayerRookRuleSet implements RookRuleSet {

    @Override
    public boolean hasFixedPartners() {
        //TODO: Should be true, but not yet handled
        return false;
    }

    @Override
    public boolean allowNoPartners() {
        return false;
    }

    @Override
    public int getMaximumBid() {
        return 200;
    }

    @Override
    public int getMinimumReasonableBid() {
        return 75;
    }

    @Override
    public int getNumberOfPartners() {
        return 1;
    }

    @Override
    public int getAloneBonus() {
        return 0;
    }

    @Override
    public boolean requireMadeBidToWin() {
        return false;
    }


}
