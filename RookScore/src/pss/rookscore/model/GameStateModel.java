
package pss.rookscore.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pss.rookscore.ruleset.RookRuleSet;

public class GameStateModel implements Serializable {

    
    private static final long serialVersionUID = 1L;

    public static class RoundResult implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private String mCaller;
        private List<String> mParters;
        private int mBid;
        private int mMade;

        private RookRuleSet mRuleSet;

        public RoundResult(RookRuleSet ruleSet, String caller, List<String> partners, int bid, int made) {
            super();
            mCaller = caller;
            mParters = partners;
            mBid = bid;
            mMade = made;
            mRuleSet = ruleSet;
        }
        
        public RoundResult(RoundResult toCopy){
            this(toCopy.getRuleSet(), toCopy.getCaller(), toCopy.getPartners(), toCopy.getBid(), toCopy.getMade());
        }

        public String getCaller() {
            return mCaller;
        }

        public void setCaller(String caller) {
            mCaller = caller;
        }

        public List<String> getPartners() {
            return mParters;
        }

        public void setParters(List<String> partners) {
            mParters = partners;
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

    private ArrayList<String> mPlayers = new ArrayList<String>();
    private List<RoundResult> mRounds = new ArrayList<RoundResult>();

    
    public GameStateModel() {
        
    }
    
    public GameStateModel(GameStateModel toCopy){
        mPlayers.addAll(toCopy.getPlayers());
        for (RoundResult round : toCopy.getRounds()) {
            mRounds.add(new RoundResult(round));
        }
    }
    
    public ArrayList<String> getPlayers() {
        return mPlayers;
    }

    public List<RoundResult> getRounds() {
        return mRounds;
    }
    
    

    public List<RoundSummary> computeRoundScores() {
        List<RoundSummary> rounds = new ArrayList<RoundSummary>();

        for (int i = 0; i < getRounds().size(); i++) {

            Map<String, Integer> previousRoundScores;
            if (i == 0) {
                previousRoundScores = new HashMap<String, Integer>();
                for (String player : getPlayers()) {
                    previousRoundScores.put(player, 0);
                }
            } else {
                previousRoundScores = rounds.get(i - 1).getRoundCumulativeScores();
            }
            
            

            RoundResult roundResult = getRounds().get(i);
            if(roundResult.getMade() > 0){
                Map<String, Integer> newRoundScore = computeRoundScore(getPlayers(), previousRoundScores, roundResult);
                rounds.add(new RoundSummary(roundResult, newRoundScore));
            } else {
                
                //round bidding is not complete. Just use scores from previous round.
                rounds.add(new RoundSummary(roundResult, previousRoundScores));
            }

            
        }

        return rounds;
    }
    private Map<String, Integer> computeRoundScore(ArrayList<String> players, Map<String, Integer> previousRoundScores, RoundResult roundResult) {
        //main rook logic
        
        /*
         * 1. Determine calling side
         * 2. Determine defending side
         * 3. Determine delta for calling side
         * 4. Apply to calling side players
         * 5. Determine delate for defending side
         * 6. Apply to defending side players
         */
        
        Map<String, Integer> newScores = new HashMap<String, Integer>();
        Set<String> callers = new HashSet<String>();
        Set<String> defenders = new HashSet<String>();
        
        if(roundResult.getCaller() != null){
            callers.add(roundResult.getCaller());
        }
        
        for (String partner : roundResult.getPartners()) {
            callers.add(partner);
        }
        
        
        for (String player : players) {
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
        
        for (String defender : defenders) {
            newScores.put(defender, previousRoundScores.get(defender) + defendingDelta);
        }
        
        for (String caller : callers) {
            newScores.put(caller, previousRoundScores.get(caller) + callingDelta);
        }
        
        
        return newScores;
    }

}
