package pss.rookscore.fragments;

import pss.rookscore.R;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener;

public class BidFragment extends Fragment {

    private static final int STARTING_BID = 160;
    public static final String kStartingBidArg = "kStartingBidArg";
    
    public interface BidSelectionListener {

        void bidSelected(int bid);

    }

    protected TextView mLabel;
    private SeekArc mSlider;
    private Button mAddButton;
    private Button mSubtractButton;

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
        
        final int startingBid = getArguments().getInt(kStartingBidArg, STARTING_BID);
        
        mSlider = (SeekArc)getView().findViewById(R.id.bidSelectorSeekArc);
        mLabel = (TextView)getView().findViewById(R.id.seekArcProgress);
        mAddButton = (Button)getView().findViewById(R.id.increaseBy5Button);
        mSubtractButton = (Button)getView().findViewById(R.id.reduceBy5Button);
        
        
        
		mSlider.setProgress(startingBid / 5);
		mLabel.setText(getLabelText(mSlider.getProgress() * 5, startingBid));

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
                mLabel.setText(getLabelText(progress * 5, startingBid));
            }
        });
        
        mLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BidSelectionListener)getActivity()).bidSelected(mSlider.getProgress() * 5);
            }
        });
        
        mAddButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mSlider.setProgress(mSlider.getProgress() + 1);
                
            }
        });
        
        mSubtractButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mSlider.setProgress(mSlider.getProgress() - 1);
                
            }
        });
    }
    
    /**
     * In the default view, we don't care about the starting bid, just what is selected
     * 
     * @param progress
     * @param targetProgress
     * @return
     */
	protected Spannable getLabelText(int bid, int startingBid) {
		return new SpannableString("" + bid);
	}
}
