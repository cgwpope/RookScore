package pss.rookscore.modules.local_players;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.common.eventbus.Subscribe;

import java.util.HashSet;
import java.util.Set;

import pss.rookscore.RookScoreApplication;
import pss.rookscore.events.PlayerAddedEvent;
import pss.rookscore.events.PlayerLoadRequestEvent;
import pss.rookscore.events.PlayersRemovedEvent;
import pss.rookscore.model.Player;

public class LocalPlayerModule implements RookScoreApplication.Module {
    private RookScoreApplication mApplication;

    //register the appropriate event listeners
    //When player list load is requested, populate with entries from local store
    //when player is added to player list, update local store
    //when player is removed, update local store


    private static final String STORED_PLAYER_NAMES_PREFS_KEY = LocalPlayerModule.class.getName()+ ".StoredPlayerNames";



    public void initialize(RookScoreApplication app){
        mApplication = app;
        app.getEventBus().register(this);
    }

    @Override
    public void cleanup(RookScoreApplication app) {
        app.getEventBus().unregister(this);
    }


    @Subscribe
    public void handlePlayerListLoad(PlayerLoadRequestEvent ev) {
        // load playerlist from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplication.getApplicationContext());
        Set<String> stringSet = prefs.getStringSet(STORED_PLAYER_NAMES_PREFS_KEY, new HashSet<String>());
        for(String storedName : stringSet){
            if(storedName.contains("\n")){
                String firstName = storedName.substring(0, storedName.indexOf('\n'));
                String lastName = storedName.substring(storedName.indexOf('\n') + 1);
                ev.getPlayerSink().addPlayer(new LocalStorePlayer(firstName, lastName));
            }
        }
    }


    @Subscribe
    public void handlePlayerAdded(PlayerAddedEvent ev){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplication.getApplicationContext());

        //TODO: Move to event handling
        Set<String> stringSet = prefs.getStringSet(STORED_PLAYER_NAMES_PREFS_KEY, new HashSet<String>());
        stringSet.add(ev.getPlayer().getFirstname() + "\n" + ev.getPlayer().getLastname());
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.clear();
        prefEditor.putStringSet(STORED_PLAYER_NAMES_PREFS_KEY, stringSet);
        prefEditor.commit();
    }

    @Subscribe
    public void handlePlayersRemoved(PlayersRemovedEvent ev){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplication.getApplicationContext());
        Set<String> stringSet = prefs.getStringSet(STORED_PLAYER_NAMES_PREFS_KEY, new HashSet<String>());

        for (Player player : ev.getPlayers()) {
            if(player instanceof LocalStorePlayer) {
                stringSet.remove(player.getFirstname() + "\n" + player.getLastname());
            }
        }

        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.clear();
        prefEditor.putStringSet(STORED_PLAYER_NAMES_PREFS_KEY, stringSet);
        prefEditor.commit();
    }

}
