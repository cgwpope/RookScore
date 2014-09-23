
package pss.rookscore;

import java.util.List;
import java.util.Stack;

import pss.rookscore.NFCLifecycleCallbacks.RookScoreNFCBroadcaster;
import pss.rookscore.events.GameStateChangedEvent;
import pss.rookscore.events.SpectatorsChangedEvent;
import pss.rookscore.fragments.BidFragment;
import pss.rookscore.fragments.BidFragment.BidSelectionListener;
import pss.rookscore.fragments.InRoundPlayerListFragment;
import pss.rookscore.fragments.MadeBidFragment;
import pss.rookscore.fragments.PlayerListFragment;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;
import pss.rookscore.fragments.views.ScoresheetHeaderView;
import pss.rookscore.fragments.views.ViewUtilities;
import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.GameStateModel.RoundResult;
import pss.rookscore.ruleset.CambridgeFivePlayerRookRuleSet;
import pss.rookscore.ruleset.CambridgeFourPlayerRookRuleSet;
import pss.rookscore.ruleset.CambridgeSixPlayerRookRuleSet;
import pss.rookscore.ruleset.RoundController;
import pss.rookscore.ruleset.RoundController.RoundState;
import pss.rookscore.ruleset.RoundStateModel;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class PlayRoundActivity extends Activity implements PlayerSelectionListener,
        BidSelectionListener, RookScoreNFCBroadcaster {

    /*
     * Collect: Bidder Bid Partner Made Rely on: PlayerListFragment for player
     * selection BigFragment for bid/score selection
     */



    public static final String GAME_STATE_MODEL = PlayRoundActivity.class.getName() + " .GameModel";
    public static final String ROUND_STATE_MODEL = PlayRoundActivity.class.getName() + " .RoundStateModel";
    public static final String ROUND_BACK_STACK = PlayRoundActivity.class.getName() + " .RoundBackStack";
    
    
    
    private RoundController mRoundController;

    private GameStateModel mModel;

    //annoying to have to maintain your own stack, but android backstack alone doesn't provide all that we need
    private final Stack<RoundStateModel> mRoundStateStack = new Stack<RoundStateModel>();
    private MenuItem mBidSummaryMenuItem;
    private EventBus mEventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mModel = (GameStateModel)getIntent().getSerializableExtra(GAME_STATE_MODEL);
        if(mModel == null){
            throw new IllegalArgumentException("Game state must be provided to " + getClass());
        }
        
        if(mModel.getPlayers().size() == 4){
            mRoundController = new RoundController(new CambridgeFourPlayerRookRuleSet());
//            mRoundController = new RoundController(new AllanFourPlayerRookRuleSet());
        } else if(mModel.getPlayers().size() == 5){
            mRoundController = new RoundController(new CambridgeFivePlayerRookRuleSet());
        } else if(mModel.getPlayers().size() == 6){
            mRoundController = new RoundController(new CambridgeSixPlayerRookRuleSet());
        }

        
        setContentView(R.layout.play_round_activity);
    }
    
    
    @Override
    protected void onStart() {
        super.onStart();
        mEventBus = ((RookScoreApplication)getApplication()).getEventBus();
        mEventBus.register(this);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }
    
    
    @Subscribe
    public void handleSpectatorsChange(SpectatorsChangedEvent e){
        //push out the current game state, including the up-to-date bidding progress
        broadcastGameState();
    }
    
    private void broadcastGameState() {
        //each time we're through here, there has been a change to the latest round state.
        //copy the game state model without this round, add the round in progres and broadcast
        GameStateModel copy = new  GameStateModel(mModel);
        copy.getRounds().add(mRoundController.getRoundState().getRoundResult());
        mEventBus.post(new GameStateChangedEvent(copy));
    }


    @Override
    public void onBackPressed() {
        if(mRoundStateStack.size() > 0){
            mRoundController.setRoundState(mRoundStateStack.pop());
            updateBidView();
        } else {
            super.onBackPressed();    
        }
        
    }

    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.play_round_menu, menu);
        mBidSummaryMenuItem = menu.findItem(R.id.bidSummaryHolder);
        return true;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        serializeState(outState);
        outState.putSerializable(GAME_STATE_MODEL, mModel);
    }

    private void serializeState(Bundle outState) {
        outState.putSerializable(ROUND_STATE_MODEL, mRoundController.getRoundState());
        outState.putSerializable(ROUND_BACK_STACK, mRoundStateStack);
        outState.putSerializable(GAME_STATE_MODEL, mModel);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mRoundController.setRoundState((RoundStateModel)savedInstanceState.getSerializable(ROUND_STATE_MODEL));
        mModel = (GameStateModel)savedInstanceState.getSerializable(GAME_STATE_MODEL);
        mRoundStateStack.addAll((Stack<RoundStateModel>)savedInstanceState.getSerializable(ROUND_BACK_STACK));
    }
    
    
    
    @Override
    protected void onResume() {
        super.onResume();

        // based on state, and fragment mode, choose fragment to display
        updateBidView();
        
        ScoresheetHeaderView shv = (ScoresheetHeaderView)findViewById(R.id.scoresheetHeaderView);
        shv.setGameStateModel(mModel);
        shv.setUseFullWidth(true);
    }

    private void updateBidView() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // assume single fragment for now
        Fragment newFragment;

        RoundState state = mRoundController.getRoundState().getState();
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

        
        broadcastGameState();
        
        
        if (newFragment != null) {
            fragmentTransaction.replace(R.id.playRoundActivityFragmentParent, newFragment);
            //don't add to back-stack, we are supplying custom back-stack handling
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
        
        
        updateBidSummary();
        
    }

    public void updateBidSummary() {
        if(mBidSummaryMenuItem != null){
            mBidSummaryMenuItem.setTitleCondensed("Bid: " +    getRoundSummaryString());
        }
    }

    
    private String getRoundSummaryString() {
        RoundResult roundResult = mRoundController.getRoundState().getRoundResult();
        StringBuilder sb = new StringBuilder();
        ViewUtilities.summarizeCompleteRoundResult(sb, roundResult, mModel.getPlayers());
        return sb.toString();
    }
    
    private Fragment prepareSelectMadeBidFragment() {
        BidFragment bidFragment = new MadeBidFragment();
        bidFragment.setRuleSet(mRoundController.getRules());
        Bundle bundle = new Bundle();
        bundle.putInt(BidFragment.kStartingBidArg, mRoundController.getRoundState().getRoundResult().getBid());
        bidFragment.setArguments(bundle);
        setTitle("Points Made");
        return bidFragment;

    }



    private Fragment prepareSelectPartnerFragment() {
        PlayerListFragment playerListFragment = new InRoundPlayerListFragment();
        playerListFragment.setPlayerList(mModel.getPlayers());
        setTitle("Select Partner");
        return playerListFragment;
    }

    private Fragment prepareSelectBidFragment() {
        BidFragment bidFragment = new BidFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BidFragment.kStartingBidArg, mRoundController.getRoundState().getRoundResult().getBid() == 0 ? 150 : mRoundController.getRoundState().getRoundResult().getBid());
        bidFragment.setArguments(bundle);
        bidFragment.setRuleSet(mRoundController.getRules());
        setTitle("Enter Bid");
        return bidFragment;

    }

    private Fragment prepareSelectCallerFragment() {
        // start with showing the PlayerSelectionFragment
        PlayerListFragment playerListFragment = new InRoundPlayerListFragment();
        playerListFragment.setPlayerList(mModel.getPlayers());
        setTitle("Select Caller");
        return playerListFragment;
    }

    @Override
    public void playerSelected(List<String> playerNames) {
        //TODO: Ensure size == 1
        
        if(playerNames.size() == 1){
            mRoundController.playerSelected(playerNames.get(0));
            
            //store state
            mRoundStateStack.push(new RoundStateModel(mRoundController.getRoundState()));
            
            //advance to next
            mRoundController.getRoundState().setState(mRoundController.nextState());
            
            updateBidView();
        }
        
    }

    @Override
    public void bidSelected(int bid) {
        // assume done in order - needs to be improved...
        mRoundController.applyBid(bid);
        
        //store state
        mRoundStateStack.push(new RoundStateModel(mRoundController.getRoundState()));
        
        
        //advance to next state
        mRoundController.getRoundState().setState(mRoundController.nextState());


        updateBidView();

    }

    @Override
    public void playerRemoved(List<String> playerNames) {
        // meaningless
    }
    
    

}
