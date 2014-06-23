package pss.rookscore.ruleset;

public interface RookRuleSet {
    
    public boolean hasFixedPartners();
    public boolean allowNoPartners();
    public int getMaximumBid();
    public int getMinimumReasonableBid();
    public int getNumberOfPartners();
    public int getAloneBonus();
    public boolean requireMadeBidToWin();
}
