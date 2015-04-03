package pss.rookscore.core.ruleset;

public class CambridgeFourPlayerRookRuleSet implements RookRuleSet {

    @Override
    public boolean hasFixedPartners() {
        return false;
    }

    @Override
    public boolean allowNoPartners() {
        return true;
    }

    @Override
    public int getMaximumBid() {
        return 180;
    }

    @Override
    public int getMinimumReasonableBid() {
        return 100;
    }

    @Override
    public int getNumberOfPartners() {
        return 1;
    }

    @Override
    public int getAloneBonus() {
        return 20;
    }

    @Override
    public boolean requireMadeBidToWin() {
        return true;
    }
    

}
