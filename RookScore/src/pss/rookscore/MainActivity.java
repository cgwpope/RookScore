package pss.rookscore;

import java.util.ArrayList;
import java.util.Collections;

import pss.rookscore.fragments.AddPlayerFragment.AddPlayerListener;
import pss.rookscore.fragments.PlayerListFragment;
import pss.rookscore.fragments.PlayerListFragment.PlayerSelectionListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity implements PlayerSelectionListener  {

    private static final String[] PLAYERS = new String[] {
            "Brad Chruszcz",
            "Martin Varady",
            "Jeremy Vander Munnik",
            "Chris Pope", 
            "Ray Fung",
            "John Kooistra",
            "John Swekla",
            "Sean Wells"
    };


    private static final int GET_PLAYER_TO_ADD = 0;


    private static final String PLAYER_LIST_KEY = MainActivity.class.getName() + ".PlayerList";

    
    private ArrayList<String> mPlayerList = new ArrayList<String>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        
        //players?
        //
        
//        mPlayerList.add(PLAYERS[0]);
//        mPlayerList.add(PLAYERS[1]);
//        mPlayerList.add(PLAYERS[2]);
//        mPlayerList.add(PLAYERS[3]);
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateGameSetup();
    }
    
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState.getStringArray(PLAYER_LIST_KEY) != null){
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
        if(item.getItemId() == R.id.actionBarStartGameAction){
            startGame();
            return true;
        } else if(item.getItemId() == R.id.actionBarAddPlayerAction){
            addPlayer();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        ArrayList<String> players = new ArrayList<String>();
        Collections.addAll(players,PLAYERS);
        intent.putExtra(AddPlayerActivity.PLAYER_NAMES_KEY, players);
        
        startActivityForResult(intent, GET_PLAYER_TO_ADD);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_PLAYER_TO_ADD && resultCode == RESULT_OK){
            String newPlayer = data.getStringExtra(AddPlayerActivity.ADD_PLAYER_RESULT_KEY);
            if(!mPlayerList.contains(newPlayer)){
                mPlayerList.add(newPlayer);
                updateGameSetup();
            } else {
                Toast.makeText(this, newPlayer + " is already in the game.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateGameSetup() {
        ((PlayerListFragment)getFragmentManager().findFragmentById(R.id.playerListFragment)).setPlayerList(mPlayerList);
        
    }

    @Override
    public void playerSelected(String playerName) {
        mPlayerList.add(playerName);
        updateGameSetup();

        
    }

    @Override
    public void playerRemoved(String playerName) {
        mPlayerList.remove(playerName);
        updateGameSetup();
    }

}
