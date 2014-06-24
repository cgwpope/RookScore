package pss.rookscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import pss.rookscore.fragments.ScoresheetFragment;
import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.GameStateModel.RoundResult;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class GameActivity extends Activity {

    private static final String GAME_STATE_MODEL_KEY =  GameActivity.class.getName() + ".GameStateModel";

    public static final String PLAYER_LIST_KEY = GameActivity.class.getName() + ".PlayerList";

    private static final int PLAY_ROUND_REQUEST = 1;
    
    private GameStateModel mGameModel = new GameStateModel();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        
        ArrayList<String> players = getIntent().getStringArrayListExtra(PLAYER_LIST_KEY);
        mGameModel.getPlayers().clear();
        mGameModel.getPlayers().addAll(players);
        
        
        //ok, let's create a fake game model for now
        Random r = new Random();
//        int numRopunds = r.nextInt(15);
//        
//        for(int i = 0; i < numRopunds; i++){
//            //pick caller, parter, bid, made
//            String caller = players.get(r.nextInt(players.size()));
//            String partner = players.get(r.nextInt(players.size()));
//            int bid = (r.nextInt(36) + 1) * 5;
//            int made = (r.nextInt(36) + 1) * 5;
//            mGameModel.getRounds().add(new RoundResult(caller, partner, bid, made));
//        }
        
    }
    
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("End Game")
            .setMessage("Are you sure you want to end the current game?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GameActivity.super.onBackPressed();
            }

        })
        .setNegativeButton("No", null)
        .show();
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(GAME_STATE_MODEL_KEY, mGameModel);
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        ScoresheetFragment scoresheetFragment = (ScoresheetFragment)getFragmentManager().findFragmentById(R.id.scoresheetFragment);
        scoresheetFragment.setGameStateModel(mGameModel);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.actionBarNewRound){
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
        if(requestCode == PLAY_ROUND_REQUEST && resultCode == RESULT_OK){
            //build up a new round from the intend
            RoundResult rr = new RoundResult(
                    data.getStringExtra(PlayRoundActivity.CALLER_KEY), 
                    data.getStringArrayExtra(PlayRoundActivity.PARTNER_KEY), 
                    data.getIntExtra(PlayRoundActivity.BID_KEY, 0), 
                    data.getIntExtra(PlayRoundActivity.MADE_KEY,0));
            mGameModel.getRounds().add(rr);
            
            //onResume() will be called since we're just about to show view - that will cause the view to be updated with the latest model
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Serializable gameState = savedInstanceState.getSerializable(GAME_STATE_MODEL_KEY);
        if(gameState != null){
            mGameModel = (GameStateModel)gameState;
        }
    }
    
    
    
}
