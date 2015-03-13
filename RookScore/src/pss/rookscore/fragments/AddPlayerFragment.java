package pss.rookscore.fragments;

import pss.rookscore.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class AddPlayerFragment extends Fragment {
    
    
    public static interface AddPlayerListener {
        public void addPlayer();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_player_fragment , container, false);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if(!(getActivity() instanceof AddPlayerListener)){
            throw new IllegalArgumentException("Parent activity must implement " + AddPlayerListener.class.getName());
        }
        
        ((Button)getView().findViewById(R.id.addPlayerButton)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddPlayerListener)getActivity()).addPlayer();
            }
        });
    }

}
