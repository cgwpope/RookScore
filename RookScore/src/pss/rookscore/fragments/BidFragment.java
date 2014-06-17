package pss.rookscore.fragments;

import pss.rookscore.R;
import pss.rookscore.fragments.AddPlayerFragment.AddPlayerListener;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener;

public class BidFragment extends Fragment {

    private static final int STARTING_BID = 160;

    public interface BidSelectionListener {

        void bidSelected(int bid);

    }

    private TextView mLabel;
    private SeekArc mSlider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bid_layout, container, false);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        
        if(!(getActivity() instanceof BidSelectionListener)){
            throw new IllegalArgumentException("Parent activity must implement " + BidSelectionListener.class.getName());
        }
        
        mSlider = (SeekArc)getView().findViewById(R.id.bidSelectorSeekArc);
        mLabel = (TextView)getView().findViewById(R.id.seekArcProgress);
        
        mSlider.setProgress(STARTING_BID/5);
        mLabel.setText("" + (mSlider.getProgress() * 5));


        
        mSlider.setOnSeekArcChangeListener(new OnSeekArcChangeListener() {
            
            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                mLabel.setText("" + (progress * 5));
                
            }
        });
        
        mLabel.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                ((BidSelectionListener)getActivity()).bidSelected(mSlider.getProgress() * 5);
            }
        });
        
    }
    
}
