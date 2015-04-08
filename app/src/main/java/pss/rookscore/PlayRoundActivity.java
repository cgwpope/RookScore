
package pss.rookscore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import pss.rookscore.core.events.GameStateChangedEvent;
import pss.rookscore.core.events.SpectatorsChangedEvent;
import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.fragments.PlayRoundFragment;
import pss.rookscore.fragments.views.ScoresheetHeaderView;

public class PlayRoundActivity extends Activity implements  PlayRoundFragment.PlayRoundFragmentParent {




    /*
     * Collect: Bidder Bid Partner Made Rely on: PlayerListFragment for player
     * selection BigFragment for bid/score selection
     */



    public static final String GAME_STATE_MODEL = PlayRoundActivity.class.getName() + " .GameModel";
    private GameStateModel mModel;
    private PlayRoundFragment mPlayRoundFragment;


    //annoying to have to maintain your own stack, but android backstack alone doesn't provide all that we need
//    private final Stack<RoundStateModel> mRoundStateStack = new Stack<RoundStateModel>();
    private MenuItem mBidSummaryMenuItem;
    private EventBus mEventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = (GameStateModel)getIntent().getSerializableExtra(GAME_STATE_MODEL);
        setContentView(R.layout.play_round_activity);
        mPlayRoundFragment = (PlayRoundFragment)getFragmentManager().findFragmentById(R.id.playerRoundFragment);
    }


    @Override
    public GameStateModel getGameStateModel() {
        return  mModel;
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public void doneRound() {
        // we have everything - return to calling activity
        Intent i = new Intent();
        Bundle b = new Bundle();

        b.putSerializable(PlayRoundFragment.ROUND_STATE_MODEL, mPlayRoundFragment.getRoundController().getRoundState());

        i.putExtras(b);
        setResult(RESULT_OK, i);

        finish();

        overridePendingTransition(0, 0);
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

    public void broadcastGameState() {
        //each time we're through here, there has been a change to the latest round state.
        //copy the game state model without this round, add the round in progres and broadcast
        GameStateModel copy = new  GameStateModel(mModel);
        copy.getRounds().add(mPlayRoundFragment.getRoundController().getRoundState().getRoundResult());
        mEventBus.post(new GameStateChangedEvent(copy));
    }


    @Override
    public void onBackPressed() {
        if(mPlayRoundFragment.backPressed()){

        } else {
            super.onBackPressed();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.play_round_menu, menu);
        mBidSummaryMenuItem = menu.findItem(R.id.bidSummaryHolder);
        updateBidSummary();
        return true;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(GAME_STATE_MODEL, mModel);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mModel = (GameStateModel)savedInstanceState.getSerializable(GAME_STATE_MODEL);
    }


    public void updateBidSummary() {
        if(mBidSummaryMenuItem != null){
            GameStateModel.RoundResult roundResult = mPlayRoundFragment.getRoundController().getRoundState().getRoundResult();
            mBidSummaryMenuItem.setTitleCondensed("Bid: " +  ModelUtilities.summarizeCompleteRoundResult(roundResult, mModel.getPlayers()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScoresheetHeaderView shv = (ScoresheetHeaderView)findViewById(R.id.scoresheetHeaderView);
        shv.setGameStateModel(mModel);
        shv.setFractionReservedForSummaryColumn(0f);

    }
}
