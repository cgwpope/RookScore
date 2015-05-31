
package pss.rookscore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.TextView;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.io.Serializable;
import java.util.Collections;

import pss.rookscore.NFCLifecycleCallbacks.RookScoreNFCBroadcaster;
import pss.rookscore.core.events.GameOverEvent;
import pss.rookscore.core.events.GameStateChangedEvent;
import pss.rookscore.core.events.SpectatorsChangedEvent;
import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.ruleset.RoundStateModel;
import pss.rookscore.fragments.PlayRoundFragment;
import pss.rookscore.fragments.ScoresheetFragment;

public class GameActivity extends Activity implements RookScoreNFCBroadcaster, PlayRoundFragment.PlayRoundFragmentParent {

    private static final String GAME_STATE_MODEL_KEY = GameActivity.class.getName() + ".GameStateModel";

    public static final String PLAYER_LIST_KEY = GameActivity.class.getName() + ".PlayerList";

    private static final int PLAY_ROUND_REQUEST = 1;

    public static final String PLAY_ROUND_STATE = GameActivity.class.getName() + ".PlayRoundState";

    private GameStateModel mGameModel = new GameStateModel();

    private EventBus mEventBus;
    private boolean mSwichedOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Player players[] = (Player[]) getIntent().getSerializableExtra(PLAYER_LIST_KEY);
        mGameModel.getPlayers().clear();
        Collections.addAll(mGameModel.getPlayers(), players);

        setContentView(R.layout.game_activity);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    // events
    @Subscribe
    public void handleSpectatorsChanged(SpectatorsChangedEvent e) {
        // push out a game state change so that all spectators are guaranteed to
        // be up to date
        mEventBus.post(new GameStateChangedEvent(mGameModel));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start the listener service when starting a game. No effect if called
        // 2x
        Intent bluetoothServiceIntent = new Intent(this, BluetoothBroadcastService.class);
        startService(bluetoothServiceIntent);

        mEventBus = ((RookScoreApplication) getApplication()).getEventBus();
        mEventBus.register(this);
    }

    @Override
    public void onBackPressed() {
        PlayRoundFragment playRoundFragment = (PlayRoundFragment)getFragmentManager().findFragmentById(R.id.playerRoundFragment);
        if(playRoundFragment != null && playRoundFragment.backPressed()){
            return;
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.game_activity_end_game_dialog_title)
                    .setMessage(R.string.game_activity_end_game_dialog_message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            endGame();
                            GameActivity.super.onBackPressed();
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();

        }


    }

    protected void endGame() {
        mEventBus.post(new GameOverEvent(mGameModel));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //only if PlayRoundFragment is not present as a child
        Fragment f = getFragmentManager().findFragmentById(R.id.playerRoundFragment);
        if(f == null || !f.isAdded()){
            getMenuInflater().inflate(R.menu.game_activity, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }




    @Override
    protected void onResume() {
        super.onResume();

        ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        scoresheetFragment.setGameStateModel(mGameModel);


        PlayRoundFragment playRoundFragment = (PlayRoundFragment)getFragmentManager().findFragmentById(R.id.playerRoundFragment);
        if(playRoundFragment != null && playRoundFragment.isAdded()){
            if(mSwichedOrientation){
                playRoundFragment.initRound();
                mSwichedOrientation = false;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionBarNewRound) {
            startNewRound();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void startNewRound() {
        Runnable newRoundRunnable = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(GameActivity.this, PlayRoundActivity.class);
                i.putExtra(PlayRoundActivity.GAME_STATE_MODEL, mGameModel);
                startActivityForResult(i, PLAY_ROUND_REQUEST);
                overridePendingTransition(0, 0);

            }
        };

        // but before all that... let's have some fun.
        ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        scoresheetFragment.runExitAnimation(newRoundRunnable);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLAY_ROUND_REQUEST) {
            if (resultCode == RESULT_OK) {
                // build up a new round from the intend
                RoundStateModel rsm = (RoundStateModel) data.getSerializableExtra(PlayRoundFragment.ROUND_STATE_MODEL);

                doneRound(rsm);

                // onResume() will be called since we're just about to show view
                // -
                // that will cause the view to be updated with the latest model
            }
            
            
            final ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
            scoresheetFragment.getView().getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                
                @Override
                public boolean onPreDraw() {
                    scoresheetFragment.getView().getViewTreeObserver().removeOnPreDrawListener(this);        
                    scoresheetFragment.runEnterAnimation();
                    return true;
                }
            });
            


        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(GAME_STATE_MODEL_KEY, mGameModel);


        //could have been hosting PlayRoundFragment
        //if so - get it to save its state so that it can be resumed by running PlayRoundActivity
        PlayRoundFragment prf = (PlayRoundFragment) getFragmentManager().findFragmentById(R.id.playerRoundFragment);

        if(prf != null && prf.isAdded()){

            if(prf.hasStartedBidding()){
                Bundle b = new Bundle();

                prf.onSaveInstanceState(b);

                //If in the middle of bidding...
                outState.putBundle(PLAY_ROUND_STATE, b);
            }
        }

        outState.putBoolean("test", mSwichedOrientation);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSwichedOrientation = savedInstanceState.getBoolean("test");
        Serializable gameState = savedInstanceState.getSerializable(GAME_STATE_MODEL_KEY);
        if (gameState != null) {
            mGameModel = (GameStateModel) gameState;
        }

        if(savedInstanceState.containsKey(PLAY_ROUND_STATE)){
            //read this state, switch to playroundactivity
            Bundle b = savedInstanceState.getBundle(PLAY_ROUND_STATE);
            if(b != null){

                Intent i = new Intent(GameActivity.this, PlayRoundActivity.class);
                i.putExtra(PlayRoundActivity.GAME_STATE_MODEL, mGameModel);
                i.putExtra(PLAY_ROUND_STATE, b);


                startActivityForResult(i, PLAY_ROUND_REQUEST);

                mSwichedOrientation = true;

            }
        }
    }

    public void removeRound(int position) {
        mGameModel.getRounds().remove(position);

        // TODO: Should be handled by event push
        ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        scoresheetFragment.setGameStateModel(mGameModel);

        mEventBus.post(new GameStateChangedEvent(mGameModel));
    }

    private void doneRound(RoundStateModel rsm) {
        mGameModel.getRounds().add(rsm.getRoundResult());
        mEventBus.post(new GameStateChangedEvent(mGameModel));
    }


    @Override
    public GameStateModel getGameStateModel() {
        return mGameModel;
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }



    @Override
    public void doneRound() {
        //only called when PlayRoundFragment is a child of this activity
        PlayRoundFragment playRoundFragment = (PlayRoundFragment)getFragmentManager().findFragmentById(R.id.playerRoundFragment);
        if(playRoundFragment != null){
            doneRound(playRoundFragment.getRoundController().getRoundState());
            playRoundFragment.startNewRound();

            ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
            scoresheetFragment.setGameStateModel(mGameModel);
        }

    }

    @Override
    public void updateBidSummary() {
        //only called when PlayRoundFragment is a child of this activity and round summary textview is available
        PlayRoundFragment playRoundFragment = (PlayRoundFragment)getFragmentManager().findFragmentById(R.id.playerRoundFragment);
        TextView roundSummaryText = (TextView)findViewById(R.id.roundSummaryText);
        if(roundSummaryText != null && playRoundFragment != null){
            GameStateModel.RoundResult roundResult = playRoundFragment.getRoundController().getRoundState().getRoundResult();
            roundSummaryText.setText("Bid: " + ModelUtilities.summarizeCompleteRoundResult(roundResult, mGameModel.getPlayers()));
        }

    }

    @Override
    public void broadcastGameState() {

    }
}
