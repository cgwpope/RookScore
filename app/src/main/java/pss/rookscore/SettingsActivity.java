package pss.rookscore;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    public static final String ENABLE_WEB_API_KEY = "enable_web_api";
    public static final String WEB_PLAYER_LIST_URL = "web_player_list_url";
    public static final String WEB_GAME_LIST_URL = "web_game_list_url";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    public static class WebAccessPreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);


            //now they are added, support proper state
            CheckBoxPreference enabledPref = (CheckBoxPreference)findPreference(ENABLE_WEB_API_KEY);

            updatePreferenceEditorStates(enabledPref.isChecked());

            enabledPref.setOnPreferenceChangeListener(this);
        }

        private void updatePreferenceEditorStates(boolean isChecked) {
            //get the rest of the prefs
            String editTextPrefIDs[] = {
                    WEB_PLAYER_LIST_URL,
                    WEB_GAME_LIST_URL,
                    "web_api_username",
                    "web_api_password"
            };

            for(String editPrefID : editTextPrefIDs){
                EditTextPreference playerURLPref = (EditTextPreference)findPreference(editPrefID);
                playerURLPref.setEnabled(isChecked);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(ENABLE_WEB_API_KEY.equals(preference.getKey())){
                updatePreferenceEditorStates((Boolean)newValue);
            }

            return true;
        }
    }


}
