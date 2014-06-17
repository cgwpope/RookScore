
package pss.rookscore.fragments;

import java.util.ArrayList;
import java.util.List;

import pss.rookscore.R;
import pss.rookscore.fragments.BidFragment.BidSelectionListener;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class PlayerListFragment extends Fragment {

    /**
     * PlayerListFragment will display a list of players and allow selection of
     * one - Parent activity must implement PlayerSelectionListener - Array of
     * names for select expected to be provided
     */

    public static interface PlayerSelectionListener {
        public void playerSelected(String playerName);

        public void playerRemoved(String playerName);
    }

    private static final String PLAYER_LIST = PlayerListFragment.class.getName() + ".PlayerList";

    private ArrayAdapter<String> mListAdapter;
    private List<String> mPlayerList = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.player_list_fragment, container, false);

        mListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

        ((ListView) v.findViewById(R.id.playerList)).setAdapter(mListAdapter);

        ((ListView) v.findViewById(R.id.playerList))
                .setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        ((PlayerSelectionListener) getActivity()).playerSelected(mListAdapter.getItem(arg2));
                    }
                });

        ((ListView) v.findViewById(R.id.playerList))
                .setOnItemLongClickListener(new OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

                        final String playerName = mListAdapter.getItem(arg2);
                        ((PlayerSelectionListener) getActivity()).playerRemoved(playerName);

                        return true;
                    }
                });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(PLAYER_LIST, new ArrayList<String>(mPlayerList));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPlayerList = savedInstanceState.getStringArrayList(PLAYER_LIST);
        }
    }

    public void setPlayerList(List<String> playerList) {
        mPlayerList = playerList;
        if (mListAdapter != null) {
            populateList();
        }
    }

    private void populateList() {
        mListAdapter.clear();
        mListAdapter.addAll(mPlayerList);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if(!(getActivity() instanceof PlayerSelectionListener)){
            throw new IllegalArgumentException("Parent activity must implement " + PlayerSelectionListener.class.getName());
        }

        
        populateList();
    }




}
