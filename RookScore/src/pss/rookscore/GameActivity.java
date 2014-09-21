
package pss.rookscore;

import java.io.Serializable;
import java.util.ArrayList;

import pss.rookscore.events.BluetoothBroadcastStartedEvent;
import pss.rookscore.events.GameOverEvent;
import pss.rookscore.events.GameStateChangedEvent;
import pss.rookscore.events.SpectatorsChangedEvent;
import pss.rookscore.fragments.ScoresheetFragment;
import pss.rookscore.model.GameStateModel;
import pss.rookscore.ruleset.RoundStateModel;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class GameActivity extends Activity implements RookScoreNFCBroadcaster {

    private static final String GAME_STATE_MODEL_KEY = GameActivity.class.getName() + ".GameStateModel";

    public static final String PLAYER_LIST_KEY = GameActivity.class.getName() + ".PlayerList";

    private static final int PLAY_ROUND_REQUEST = 1;

    private GameStateModel mGameModel = new GameStateModel();

    private EventBus mEventBus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        ArrayList<String> players = getIntent().getStringArrayListExtra(PLAYER_LIST_KEY);
        mGameModel.getPlayers().clear();
        mGameModel.getPlayers().addAll(players);
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

    protected void endGame() {
        mEventBus.post(new GameOverEvent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        scoresheetFragment.setGameStateModel(mGameModel);
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
        Intent i = new Intent(this, PlayRoundActivity.class);
        i.putExtra(PlayRoundActivity.GAME_STATE_MODEL, mGameModel);
        startActivityForResult(i, PLAY_ROUND_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLAY_ROUND_REQUEST && resultCode == RESULT_OK) {
            // build up a new round from the intend
            RoundStateModel rsm = (RoundStateModel) data.getSerializableExtra(PlayRoundActivity.ROUND_STATE_MODEL);
            mGameModel.getRounds().add(rsm.getRoundResult());

            mEventBus.post(new GameStateChangedEvent(mGameModel));

            // onResume() will be called since we're just about to show view -
            // that will cause the view to be updated with the latest model
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(GAME_STATE_MODEL_KEY, mGameModel);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Serializable gameState = savedInstanceState.getSerializable(GAME_STATE_MODEL_KEY);
        if (gameState != null) {
            mGameModel = (GameStateModel) gameState;
        }
    }

    public void removeRound(int position) {
        mGameModel.getRounds().remove(position);

        // TODO: Should be handled by event push
        ScoresheetFragment scoresheetFragment = (ScoresheetFragment) getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        scoresheetFragment.setGameStateModel(mGameModel);

        mEventBus.post(new GameStateChangedEvent(mGameModel));
    }

    
    @Subscribe
    public void handleBluetoothBroadcastStarted(BluetoothBroadcastStartedEvent e) {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.setNdefPushMessage(new NdefMessage(RookScoreNFCBroadcaster.RookScoreNFCUtils.newTextRecord(e.mAddress)), this);
        }
    }




}
