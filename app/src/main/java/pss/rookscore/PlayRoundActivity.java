
package pss.rookscore;

import java.util.List;
import java.util.Stack;

import pss.rookscore.NFCLifecycleCallbacks.RookScoreNFCBroadcaster;
import pss.rookscore.core.events.GameStateChangedEvent;
import pss.rookscore.core.events.SpectatorsChangedEvent;
import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.fragments.BidFragment;
import pss.rookscore.fragments.BidFragment.BidSelectionListener;
import pss.rookscore.fragments.InRoundPlayerListFragment;
import pss.rookscore.fragments.MadeBidFragment;
import pss.rookscore.fragments.PlayerListFragment;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;
import pss.rookscore.fragments.views.ScoresheetHeaderView;
import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.GameStateModel.RoundResult;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.ruleset.RookRuleSet;
import pss.rookscore.core.ruleset.RoundController;
import pss.rookscore.core.ruleset.RoundController.RoundState;
import pss.rookscore.core.ruleset.RoundStateModel;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class PlayRoundActivity extends Activity implements PlayerSelectionListener, BidSelectionListener, RookScoreNFCBroadcaster {

    
    private static enum AnimateRequest {
        DO_NOT_ANIMATE(0),
        ANIMATE_FORWARD (1),
        ANIMATE_REVERSE (-1);
        
        private final int mValue;

        private AnimateRequest(int value){
            mValue = value;
        }
        
        public int getValue() {
            return mValue;
        }
    }
    
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

        RookRuleSet rrs = ((RookScoreApplication)getApplication()).buildRookRuleSet(mModel.getPlayers().size());
        if(rrs == null){
            throw new IllegalArgumentException("Unable to determine rule set for this game");
        }
        mRoundController = new RoundController(rrs);
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
            updateBidView(AnimateRequest.ANIMATE_REVERSE);
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
        updateBidView(AnimateRequest.DO_NOT_ANIMATE);
        
        ScoresheetHeaderView shv = (ScoresheetHeaderView)findViewById(R.id.scoresheetHeaderView);
        shv.setGameStateModel(mModel);
        shv.setFractionReservedForSummaryColumn(0f);
    }

    private void updateBidView(final AnimateRequest animateRequest) {
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
            final Fragment finalNewFragment = newFragment;
            
            final Runnable fragmentSwitchRunnable = new Runnable() {
                @Override
                public void run() {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.playRoundActivityFragmentParent, finalNewFragment);
                    //don't add to back-stack, we are supplying custom back-stack handling
                    fragmentTransaction.commit();
                                        
                }
            };
            
            
            if(!animateRequest.equals(AnimateRequest.DO_NOT_ANIMATE)){
                final View v = findViewById(R.id.playRoundActivityFragmentParent);
                v.animate()
                .translationX(-1 * animateRequest.getValue() * v.getWidth())
                .setDuration(250)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //the animation listener is added to the view, so without removing the listener, it will fire when the second animation finishes as well - we don't want this!
                        v.animate().setListener(null);

                        
                        fragmentSwitchRunnable.run();
                        
                        v.setTranslationX(animateRequest.getValue() * v.getWidth());
                        
                        v.animate()
                        .translationX(0)
                        .setDuration(250)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                    }
                    
                })
                .start(); 
            } else {
                fragmentSwitchRunnable.run();
            }
            
            

            
        } else {
            // we have everything - return to calling activity
            Intent i = new Intent();
            Bundle b = new Bundle();
            serializeState(b);
            i.putExtras(b);
            setResult(RESULT_OK, i);
            
            
            finish();

            overridePendingTransition(0, 0);
            
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
        ModelUtilities.summarizeCompleteRoundResult(sb, roundResult, mModel.getPlayers());
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
    public void playerSelected(List<Player> playerNames) {
        //TODO: Ensure size == 1
        
        if(playerNames.size() == 1){
            mRoundController.playerSelected(playerNames.get(0));
            
            //store state
            mRoundStateStack.push(new RoundStateModel(mRoundController.getRoundState()));
            
            //advance to next
            mRoundController.getRoundState().setState(mRoundController.nextState());
            
            updateBidView(AnimateRequest.ANIMATE_FORWARD);
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


        updateBidView(AnimateRequest.ANIMATE_FORWARD);

    }

    @Override
    public void playerRemoved(List<Player> playerNames) {
        // meaningless
    }
    
    

}
