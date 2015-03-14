package pss.rookscore;

import android.app.Application;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.preference.PreferenceManager;

import com.google.common.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.modules.local_players.LocalPlayerModule;
import pss.rookscore.modules.remote_players.RemotePlayerModule;
import pss.rookscore.ruleset.AllanFourPlayerRookRuleSet;
import pss.rookscore.ruleset.CambridgeFivePlayerRookRuleSet;
import pss.rookscore.ruleset.CambridgeFourPlayerRookRuleSet;
import pss.rookscore.ruleset.CambridgeSixPlayerRookRuleSet;
import pss.rookscore.ruleset.RookRuleSet;
import pss.rookscore.ruleset.RoundController;

public class RookScoreApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private EventBus mEventBus;
    private List<Module> mModules = new ArrayList<>();
    //provide eventbus for sharing between running components



    public RookRuleSet buildRookRuleSet(int numPlayers){
        if(numPlayers == 4){

            //read the pref value
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            final String ruleset = sharedPref.getString("four_player_ruleset", CambridgeFourPlayerRookRuleSet.class.getName());

            //can do with reflection if it gets more complex
            if(AllanFourPlayerRookRuleSet.class.getName().equals(ruleset)){
                return new AllanFourPlayerRookRuleSet();
            } else {
                return new CambridgeFourPlayerRookRuleSet();
            }

        } else if(numPlayers == 5){
            return new CambridgeFivePlayerRookRuleSet();
        } else if(numPlayers == 6){
            return new CambridgeSixPlayerRookRuleSet();
        } else {
            return null;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mEventBus = new EventBus();
        
        
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        
        if(nfcAdapter != null){
            registerActivityLifecycleCallbacks(new NFCLifecycleCallbacks(mEventBus, nfcAdapter));    
        }


        Module m = new LocalPlayerModule();
        mModules.add(m);
        m.initialize(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        evaluateRemoteModulePreference(sharedPref);

        sharedPref.registerOnSharedPreferenceChangeListener(this);


    }

    private void evaluateRemoteModulePreference(SharedPreferences sharedPref) {
        if(sharedPref.getBoolean(SettingsActivity.ENABLE_WEB_API_KEY, false)){
            Module m = new RemotePlayerModule(
                    sharedPref.getString(SettingsActivity.WEB_PLAYER_LIST_URL, "http://rook2.chruszcz.ca/api/players"),
                    sharedPref.getString(SettingsActivity.WEB_GAME_LIST_URL, "http://rook2.chruszcz.ca/api/games/"));
            mModules.add(m);
            m.initialize(this);
        }
    }


    public EventBus getEventBus() {
        return mEventBus;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //if shared prefs change, re-evaluate whether remote module should be present

        if(SettingsActivity.ENABLE_WEB_API_KEY.equals(key) || SettingsActivity.WEB_GAME_LIST_URL.equals(key) || SettingsActivity.WEB_PLAYER_LIST_URL.equals(key)) {
            Module m = findModuleOfClass(RemotePlayerModule.class);
            if (m != null) {
                m.cleanup(this);
                mModules.remove(m);
            }

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            evaluateRemoteModulePreference(sharedPref);
        }

    }

    private Module findModuleOfClass(Class<RemotePlayerModule> remotePlayerModuleClass) {
        for(Module m : mModules){
            if(m.getClass().equals(remotePlayerModuleClass)){
                return m;
            }
        }
        return null;
    }


    public static interface Module {
        public void initialize(RookScoreApplication app);
        public void cleanup(RookScoreApplication app);
    }
    
    

}
