package pss.rookscore.fragments;

import pss.rookscore.PlayRoundActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class InRoundPlayerListFragment extends PlayerListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setUseMultiSelect(false);
    }
    

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

//        ((PlayRoundActivity)getActivity()).updateBidSummary();
    }
    
}
