package pss.rookscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameStateModel implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static class RoundResult implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        
        private String mCaller;
        private String[] mParters;
        private int mBid;
        private int mMade;
        
        
        
        public RoundResult(String caller, String[] partners, int bid, int made) {
            super();
            mCaller = caller;
            mParters = partners;
            mBid = bid;
            mMade = made;
        }
        public String getCaller() {
            return mCaller;
        }
        public void setCaller(String caller) {
            mCaller = caller;
        }
        public String[] getParters() {
            return mParters;
        }
        public void setParter(String[] parter) {
            mParters = parter;
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
    }
    
    
    private ArrayList<String> mPlayers = new ArrayList();
    private List<RoundResult> mRounds = new ArrayList<RoundResult>();
    
    public ArrayList<String> getPlayers() {
        return mPlayers;
    }
    
    public List<RoundResult> getRounds() {
        return mRounds;
    }
    
}
