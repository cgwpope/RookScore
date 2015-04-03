
package pss.rookscore.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pss.rookscore.core.ruleset.RookRuleSet;


public class GameStateModel implements Serializable {

    
    private static final long serialVersionUID = 1L;

    public static class RoundResult implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private Player mCaller;
        private List<Player> mPartners;
        private int mBid;
        private int mMade;

        private RookRuleSet mRuleSet;

        public RoundResult(RookRuleSet ruleSet, Player caller, List<Player> partners, int bid, int made) {
            super();
            mCaller = caller;
            mPartners = partners;
            mBid = bid;
            mMade = made;
            mRuleSet = ruleSet;
        }
        
        public RoundResult(RoundResult toCopy){
            this(toCopy.getRuleSet(), toCopy.getCaller(), toCopy.getPartners(), toCopy.getBid(), toCopy.getMade());
        }

        public Player getCaller() {
            return mCaller;
        }

        public void setCaller(Player caller) {
            mCaller = caller;
        }

        public List<Player> getPartners() {
            return mPartners;
        }

        public void setPartners(List<Player> partners) {
            mPartners = partners;
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
        
        public RookRuleSet getRuleSet() {
            return mRuleSet;
        }
    }

    private ArrayList<Player> mPlayers = new ArrayList<Player>();
    private List<RoundResult> mRounds = new ArrayList<RoundResult>();

    
    public GameStateModel() {
        
    }
    
    public GameStateModel(GameStateModel toCopy){
        mPlayers.addAll(toCopy.getPlayers());
        for (RoundResult round : toCopy.getRounds()) {
            mRounds.add(new RoundResult(round));
        }
    }
    
    public ArrayList<Player> getPlayers() {
        return mPlayers;
    }

    public List<RoundResult> getRounds() {
        return mRounds;
    }
    
    

    public List<RoundSummary> computeRoundScores() {
        List<RoundSummary> rounds = new ArrayList<RoundSummary>();

        for (int i = 0; i < getRounds().size(); i++) {

            Map<Player, Integer> previousRoundScores;
            if (i == 0) {
                previousRoundScores = new HashMap<Player, Integer>();
                for (Player player : getPlayers()) {
                    previousRoundScores.put(player, 0);
                }
            } else {
                previousRoundScores = rounds.get(i - 1).getRoundCumulativeScores();
            }
            
            

            RoundResult roundResult = getRounds().get(i);
            if(roundResult.getMade() > 0){
                Map<Player, Integer> newRoundScore = computeRoundScore(getPlayers(), previousRoundScores, roundResult);
                rounds.add(new RoundSummary(roundResult, newRoundScore));
            } else {
                
                //round bidding is not complete. Just use scores from previous round.
                rounds.add(new RoundSummary(roundResult, previousRoundScores));
            }

            
        }

        return rounds;
    }
    private Map<Player, Integer> computeRoundScore(ArrayList<Player> players, Map<Player, Integer> previousRoundScores, RoundResult roundResult) {
        //main rook logic
        
        /*
         * 1. Determine calling side
         * 2. Determine defending side
         * 3. Determine delta for calling side
         * 4. Apply to calling side players
         * 5. Determine delate for defending side
         * 6. Apply to defending side players
         */
        
        Map<Player, Integer> newScores = new HashMap<Player, Integer>();
        Set<Player> callers = new HashSet<Player>();
        Set<Player> defenders = new HashSet<Player>();
        
        if(roundResult.getCaller() != null){
            callers.add(roundResult.getCaller());
        }
        
        for (Player partner : roundResult.getPartners()) {
            callers.add(partner);
        }
        
        
        for (Player player : players) {
            if(!callers.contains(player)){
                defenders.add(player);
            }
        }
        
        int callingDelta;
        int defendingDelta;
        
        if(roundResult.getMade() >= roundResult.getBid()){
            callingDelta = roundResult.getMade();
            defendingDelta = roundResult.getRuleSet().getMaximumBid() - roundResult.getMade(); 
        } else {
            callingDelta = -1 * roundResult.getBid();
            defendingDelta = roundResult.getRuleSet().getMaximumBid() - roundResult.getMade(); 
        }
        
        
        //alone bonus!
        if(callers.size() == 1){
            if(callingDelta == roundResult.getRuleSet().getMaximumBid()){
                callingDelta += (20 + (players.size() - 4) * 10);
            }
        }
        
        for (Player defender : defenders) {
            newScores.put(defender, previousRoundScores.get(defender) + defendingDelta);
        }
        
        for (Player caller : callers) {
            newScores.put(caller, previousRoundScores.get(caller) + callingDelta);
        }
        
        
        return newScores;
    }

}
