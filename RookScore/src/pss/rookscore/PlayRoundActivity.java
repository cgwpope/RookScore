package pss.rookscore;

import java.util.ArrayList;

import com.triggertrap.seekarc.SeekArc;

import pss.rookscore.fragments.BidFragment;
import pss.rookscore.fragments.BidFragment.BidSelectionListener;
import pss.rookscore.fragments.PlayerListFragment;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class PlayRoundActivity extends Activity implements PlayerSelectionListener, BidSelectionListener{
    
    /*
     * Collect:
     * Bidder
     * Bid
     * Partner
     * Made
     * 
     * Rely on:
     * PlayerListFragment for player selection
     * BigFragment for bid/score selection
     */
    
    private String mCaller;
    private int mBid;
    private String mPartner;
    private int mMade;
    private ArrayList<String> mPlayerList;
    
    public static final String CALLER_KEY = PlayRoundActivity.class.getName() + " .Caller";
    public static final String PARTNER_KEY = PlayRoundActivity.class.getName() + " .Partner";
    public static final String BID_KEY = PlayRoundActivity.class.getName() + " .Bid";
    public static final String MADE_KEY = PlayRoundActivity.class.getName() + " .Made";
    
    
    public static final String PLAYER_LIST_KEY = PlayRoundActivity.class.getName() + ".PlayerList";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_round_activity);
        
        //intet should include list of potential playewes
        
        mPlayerList = getIntent().getStringArrayListExtra(PLAYER_LIST_KEY);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        serializeState(outState);
        
        outState.putStringArrayList(PLAYER_LIST_KEY, mPlayerList);
    }

    private void serializeState(Bundle outState) {
        outState.putString(CALLER_KEY, mCaller);
        outState.putInt(BID_KEY, mBid);
        outState.putString(PARTNER_KEY, mPartner);
        outState.putInt(MADE_KEY, mMade);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCaller = savedInstanceState.getString(CALLER_KEY);
        mBid = savedInstanceState.getInt(BID_KEY);
        mPartner = savedInstanceState.getString(PARTNER_KEY);
        mMade = savedInstanceState.getInt(MADE_KEY);
        
        mPlayerList = savedInstanceState.getStringArrayList(PLAYER_LIST_KEY);
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        //based on state, and fragment mode, choose fragment to display
        switchToCorrectFragment();
    }

    private void switchToCorrectFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //assume single fragment for now
        Fragment newFragment;
        if(mCaller == null){
            //start with showing the PlayerSelectionFragment
            PlayerListFragment playerListFragment = new PlayerListFragment();
            playerListFragment.setPlayerList(mPlayerList);
            newFragment = playerListFragment;
        } else if(mBid == 0){
            newFragment = new BidFragment();            
        }  else if(mPartner == null){
            PlayerListFragment playerListFragment = new PlayerListFragment();
            playerListFragment.setPlayerList(mPlayerList);
            newFragment = playerListFragment;
        } else if(mMade == 0) {
            newFragment = new BidFragment();            
        } else {
            newFragment = null;
        }
        
        if(newFragment != null){
            fragmentTransaction.replace(R.id.playRoundActivityFragmentParent, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            //we have everything - return to calling activity
            Intent i = new Intent();
            Bundle b = new Bundle();
            serializeState(b);
            i.putExtras(b);
            setResult(RESULT_OK, i);
            finish();
        }
        
    }

    @Override
    public void playerSelected(String playerName) {
        //assume done in order - needs to be improved...
        if(mCaller == null){
            mCaller = playerName;
        } else {
            mPartner = playerName;
        }
        
        switchToCorrectFragment();
    }

    @Override
    public void bidSelected(int bid) {
        //assume done in order - needs to be improved...

        if(mBid == 0){
            mBid = bid;
        } else {
            mMade = bid;
        }
        
        switchToCorrectFragment();
        
    }

    @Override
    public void playerRemoved(String playerName) {
        //meaningless
    }
    
    
    
    
    
    

}
