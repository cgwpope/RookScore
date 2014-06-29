package pss.rookscore.ruleset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pss.rookscore.ruleset.RoundController.RoundState;

public class RoundStateModel implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private RoundState mState;
    private String mCaller;
    private int mBid;
    private List<String> mPartners= new ArrayList<String>();
    private int mMade;

    public RoundStateModel() {
    }

    public RoundStateModel(RoundStateModel toCopy) {
        mState = toCopy.mState;
        mCaller = toCopy.mCaller;
        mBid = toCopy.mBid;
        mPartners.addAll(toCopy.mPartners);
        mMade = toCopy.mMade;
    }
    
    public RoundState getState() {
        return mState;
    }

    public void setState(RoundState state) {
        mState = state;
    }

    public String getCaller() {
        return mCaller;
    }

    public void setCaller(String caller) {
        mCaller = caller;
    }

    public int getBid() {
        return mBid;
    }

    public void setBid(int bid) {
        mBid = bid;
    }

    public List<String> getPartners() {
        return mPartners;
    }

    public void setPartners(List<String> partners) {
        mPartners = partners;
    }

    public int getMade() {
        return mMade;
    }

    public void setMade(int made) {
        mMade = made;
    }
}