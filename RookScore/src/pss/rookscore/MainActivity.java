
package pss.rookscore;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

import pss.rookscore.fragments.PlayerListFragment;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements PlayerSelectionListener {

    private static final int GET_PLAYER_TO_ADD = 0;
    private static final int REQUEST_ENABLE_BT = 1;

    private static final String PLAYER_LIST_KEY = MainActivity.class.getName() + ".PlayerList";

    protected static final UUID ROOK_SCORE_BLUETOOTH_SERVICE_UUID = UUID.fromString("ebe061c0-1926-11e4-8c21-0800200c9a66");

    private ArrayList<String> mPlayerList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ((Button) findViewById(R.id.addPlayerButton)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                addPlayer();
            }
        });

        // if the bluetooth receiver service is running, stop it, it must be for
        // an old game
        Intent startReceiverIntent = new Intent(this, BluetoothReceiverService.class);
        stopService(startReceiverIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGameSetup();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.getStringArray(PLAYER_LIST_KEY) != null) {
            for (String player : savedInstanceState.getStringArray(PLAYER_LIST_KEY)) {
                mPlayerList.add(player);
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionBarStartGameAction) {
            startGame();
            return true;
        } else if (item.getItemId() == R.id.actionBarObserveGameAction) {
            startObservingGame();
        }
        return super.onOptionsItemSelected(item);
    }


    private void startObservingGame() {
        Intent intent = new Intent(this, ObserveGameActivity.class);
        startActivity(intent);
    }

    private void startGame() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.PLAYER_LIST_KEY, mPlayerList);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String players[] = (String[]) mPlayerList.toArray(new String[mPlayerList.size()]);
        outState.putStringArray(PLAYER_LIST_KEY, players);
    }

    public void addPlayer() {
        Intent intent = new Intent(this, AddPlayerActivity.class);
        startActivityForResult(intent, GET_PLAYER_TO_ADD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_PLAYER_TO_ADD && resultCode == RESULT_OK) {
            String newPlayer = data.getStringExtra(AddPlayerActivity.ADD_PLAYER_RESULT_KEY);
            addPlayer(newPlayer);
        }

    }

    private void addPlayer(String newPlayer) {
        if (!mPlayerList.contains(newPlayer)) {
            mPlayerList.add(newPlayer);
            ((PlayerListFragment) getFragmentManager().findFragmentById(R.id.playerListFragment)).addPlayer(newPlayer);
        } else {
            Toast.makeText(this, newPlayer + " is already in the game.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGameSetup() {
        ((PlayerListFragment) getFragmentManager().findFragmentById(R.id.playerListFragment)).setPlayerList(mPlayerList);

    }

    @Override
    public void playerSelected(String playerName) {
        addPlayer(playerName);
    }

    @Override
    public void playerRemoved(String playerName) {
        mPlayerList.remove(playerName);
        ((PlayerListFragment) getFragmentManager().findFragmentById(R.id.playerListFragment)).removePlayer(playerName);
    }

}
