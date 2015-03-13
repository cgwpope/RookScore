
package pss.rookscore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pss.rookscore.events.PlayerAddedEvent;
import pss.rookscore.events.PlayerLoadRequestEvent;
import pss.rookscore.events.PlayersRemovedEvent;
import pss.rookscore.fragments.PlayerListFragment;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;
import pss.rookscore.model.Player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

public class AddPlayerActivity extends Activity implements PlayerSelectionListener {

    /* package */static final String ADD_PLAYER_RESULT_KEY =  AddPlayerActivity.class.getName() + ".AddPlayerResult";
    public static final String PLAYER_NAMES_KEY = AddPlayerActivity.class.getName() + ".PlayerNames";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_player_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_player, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addNewPlayer) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(R.string.add_player_new_player_dialog_title);
            
            alert.setMessage(R.string.add_player_new_player_dialog_message);

            alert.setView(R.layout.add_player_dialog);

            // Set an EditText view to get user input
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    EditText firstName = (EditText)((AlertDialog)dialog).findViewById(R.id.firstNameText);
                    EditText lastName = (EditText)((AlertDialog)dialog).findViewById(R.id.lastNameText);

                    Player player = new Player(firstName.getText().toString(), lastName.getText().toString());
                    addPlayer(player);
                    playerSelected(Collections.singletonList(player));
                }
            });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            
            
            AlertDialog alertDialog = alert.show();
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    protected void addPlayer(Player player) {
        ((RookScoreApplication)getApplication()).getEventBus().post(new PlayerAddedEvent(player));
        loadPlayerList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlayerList();
    }

    private void loadPlayerList() {
        final PlayerListFragment plf = (PlayerListFragment) getFragmentManager().findFragmentById(R.id.playerListFragment);
        plf.setPlayerList(new ArrayList<Player>());
        ((RookScoreApplication)getApplication()).getEventBus().post(new PlayerLoadRequestEvent(new PlayerLoadRequestEvent.PlayerSink() {
            @Override
            public void addPlayer(final Player localStorePlayer) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        plf.addToPlayerList(localStorePlayer);
                    }
                });

            }
        }));

    }

    @Override
    public void playerSelected(List<Player> playerNames) {
        Intent i = new Intent();
        Player players[] = playerNames.toArray(new Player[playerNames.size()]);
        i.putExtra(ADD_PLAYER_RESULT_KEY, players);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void playerRemoved(final List<Player> players) {
        
        
        Object args[] = new Object[players.size() + 1];
        args[0] = players.size();
        for(int i = 0; i < players.size(); i++){
            args[i + 1] = players.get(i);
        }

        // prompt to delete
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getResources().getQuantityString(R.plurals.add_player_remove_multi_player_dialog_title, players.size(), args))
                .setMessage(getResources().getQuantityString(R.plurals.add_player_remove_multi_player_dialog_message, players.size(), args))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((RookScoreApplication)getApplication()).getEventBus().post(new PlayersRemovedEvent(players));
                        loadPlayerList();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();



    }
}
