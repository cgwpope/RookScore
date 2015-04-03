package pss.rookscore.core.ruleset;

import java.io.Serializable;

public interface RookRuleSet extends Serializable {
    
    public boolean hasFixedPartners();
    public boolean allowNoPartners();

    public int getMaximumBid();
    
    public int getMinimumReasonableBid();
    public int getNumberOfPartners();
    public int getAloneBonus();
    public boolean requireMadeBidToWin();
}
