
package pss.rookscore;

import java.util.ArrayList;

import pss.rookscore.fragments.BidFragment;
import pss.rookscore.fragments.BidFragment.BidSelectionListener;
import pss.rookscore.fragments.MadeBidFragment;
import pss.rookscore.fragments.PlayerListFragment;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;
import pss.rookscore.ruleset.CambridgeFivePlayerRookRuleSet;
import pss.rookscore.ruleset.CambridgeFourPlayerRookRuleSet;
import pss.rookscore.ruleset.CambridgeSixPlayerRookRuleSet;
import pss.rookscore.ruleset.RoundController;
import pss.rookscore.ruleset.RoundController.RoundState;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class PlayRoundActivity extends Activity implements PlayerSelectionListener,
        BidSelectionListener {

    /*
     * Collect: Bidder Bid Partner Made Rely on: PlayerListFragment for player
     * selection BigFragment for bid/score selection
     */

    private ArrayList<String> mPlayerList;

    private RoundController mRoundController;

    public static final String CALLER_KEY = PlayRoundActivity.class.getName() + " .Caller";
    public static final String PARTNER_KEY = PlayRoundActivity.class.getName() + " .Partner";
    public static final String BID_KEY = PlayRoundActivity.class.getName() + " .Bid";
    public static final String MADE_KEY = PlayRoundActivity.class.getName() + " .Made";
    private static final String STATE_KEY = PlayRoundActivity.class.getName() + " .State";

    public static final String PLAYER_LIST_KEY = PlayRoundActivity.class.getName() + ".PlayerList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_round_activity);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // intent should include list of potential players
        mPlayerList = getIntent().getStringArrayListExtra(PLAYER_LIST_KEY);
        if(mPlayerList.size() == 4){
            mRoundController = new RoundController(new CambridgeFourPlayerRookRuleSet());    
        } else if(mPlayerList.size() == 5){
            mRoundController = new RoundController(new CambridgeFivePlayerRookRuleSet());
        } else if(mPlayerList.size() == 6){
            mRoundController = new RoundController(new CambridgeSixPlayerRookRuleSet());
        }
        
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        serializeState(outState);
        outState.putStringArrayList(PLAYER_LIST_KEY, mPlayerList);
    }

    private void serializeState(Bundle outState) {
        outState.putString(CALLER_KEY, mRoundController.getCaller());
        outState.putInt(BID_KEY, mRoundController.getBid());
        String partners[] = (String[]) mRoundController.getPartners().toArray(
                new String[mRoundController.getPartners().size()]);
        outState.putStringArray(PARTNER_KEY, partners);
        outState.putInt(MADE_KEY, mRoundController.getMade());
        outState.putSerializable(STATE_KEY, mRoundController.getState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mRoundController.setState((RoundState) savedInstanceState.getSerializable(STATE_KEY));
        mRoundController.setCaller(savedInstanceState.getString(CALLER_KEY));
        mRoundController.setBid(savedInstanceState.getInt(BID_KEY));

        String partners[] = savedInstanceState.getStringArray(PARTNER_KEY);
        for (String partner : partners) {
            mRoundController.getPartners().add(partner);
        }

        mRoundController.setMade(savedInstanceState.getInt(MADE_KEY));
        mPlayerList = savedInstanceState.getStringArrayList(PLAYER_LIST_KEY);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // based on state, and fragment mode, choose fragment to display
        switchToCorrectFragment();
    }

    private void switchToCorrectFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // assume single fragment for now
        Fragment newFragment;

        RoundState state = mRoundController.getState();
        if(state == null){
            newFragment = null;
        } else {
            switch (state) {
                case COLLECT_CALLER:
                    newFragment = prepareSelectCallerFragment();
                    break;
                case COLLECT_BID:
                    newFragment = prepareSelectBidFragment();
                    break;
                case COLLECT_PARTNER:
                    newFragment = prepareSelectPartnerFragment();
                    break;
                case COLLECT_MADE_BID:
                    newFragment = prepareSelectMadeBidFragment();
                    break;
                default:
                    newFragment = null;
            }

        }

        if (newFragment != null) {
            fragmentTransaction.replace(R.id.playRoundActivityFragmentParent, newFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            // we have everything - return to calling activity
            Intent i = new Intent();
            Bundle b = new Bundle();
            serializeState(b);
            i.putExtras(b);
            setResult(RESULT_OK, i);
            finish();
        }

    }

    private Fragment prepareSelectMadeBidFragment() {
        BidFragment bidFragment = new MadeBidFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BidFragment.kStartingBidArg, mRoundController.getBid());
        bidFragment.setArguments(bundle);
        setTitle("Points Made");
        return bidFragment;

    }

    private Fragment prepareSelectPartnerFragment() {
        PlayerListFragment playerListFragment = new PlayerListFragment();
        playerListFragment.setPlayerList(mPlayerList);
        setTitle("Select Partner");
        return playerListFragment;
    }

    private Fragment prepareSelectBidFragment() {
        BidFragment bidFragment = new BidFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BidFragment.kStartingBidArg, 150);
        bidFragment.setArguments(bundle);
        setTitle("Enter Bid");
        return bidFragment;

    }

    private Fragment prepareSelectCallerFragment() {
        // start with showing the PlayerSelectionFragment
        PlayerListFragment playerListFragment = new PlayerListFragment();
        playerListFragment.setPlayerList(mPlayerList);
        setTitle("Select Caller");
        return playerListFragment;
    }

    @Override
    public void playerSelected(String playerName) {
        mRoundController.playerSelected(playerName);
        mRoundController.setState(mRoundController.nextState());
        
        switchToCorrectFragment();
    }

    @Override
    public void bidSelected(int bid) {
        // assume done in order - needs to be improved...
        mRoundController.applyBid(bid);
        mRoundController.setState(mRoundController.nextState());


        switchToCorrectFragment();

    }

    @Override
    public void playerRemoved(String playerName) {
        // meaningless
    }

}
