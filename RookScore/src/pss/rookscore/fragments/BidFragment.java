
package pss.rookscore.fragments;

import pss.rookscore.PlayRoundActivity;
import pss.rookscore.R;
import pss.rookscore.ruleset.RookRuleSet;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener;

public class BidFragment extends Fragment {

    private static final int STARTING_BID = 160;
    public static final String kStartingBidArg = "kStartingBidArg";
    private static final String RULE_SET_KEY = BidFragment.class.getName() + ".RuleSet";
    private static final String BID_DISPLAY_MODE_KEY = BidFragment.class.getName()
            + ".BidDisplayMode";;

    public interface BidSelectionListener {

        void bidSelected(int bid);

    }

    protected TextView mLabel;
    private SeekArc mSlider;
    private Button mAddButton;
    private Button mSubtractButton;
    private RookRuleSet mRuleset;
    private boolean mShowAllBidsMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bid_layout, container, false);

        if (savedInstanceState != null) {
            mRuleset = (RookRuleSet) savedInstanceState.getSerializable(RULE_SET_KEY);
            mShowAllBidsMode = savedInstanceState.getBoolean(BID_DISPLAY_MODE_KEY);
        }

        return v;
    }

    public void setRuleSet(RookRuleSet rrs) {
        mRuleset = rrs;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RULE_SET_KEY, mRuleset);
        outState.putBoolean(BID_DISPLAY_MODE_KEY, mShowAllBidsMode);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!(getActivity() instanceof BidSelectionListener)) {
            throw new IllegalArgumentException("Parent activity must implement "
                    + BidSelectionListener.class.getName());
        }

        final int startingBid = getArguments().getInt(kStartingBidArg, STARTING_BID);

        mSlider = (SeekArc) getView().findViewById(R.id.bidSelectorSeekArc);
        mLabel = (TextView) getView().findViewById(R.id.seekArcProgress);
        mAddButton = (Button) getView().findViewById(R.id.increaseBy5Button);
        mSubtractButton = (Button) getView().findViewById(R.id.reduceBy5Button);

        mSlider.setMax((mRuleset.getMaximumBid() - mRuleset.getMinimumReasonableBid()) / 5);

        OnTouchListener deadspotTouchListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }
        };
        ((ViewGroup) getView().findViewById(R.id.scoreIncreaseButtonContainer))
                .setOnTouchListener(deadspotTouchListener);
        ((ViewGroup) getView().findViewById(R.id.scoreDecreaseButtonContainer))
                .setOnTouchListener(deadspotTouchListener);

        mSlider.setProgress(mapBidToProgress(startingBid));
        mLabel.setText(getLabelText(mapProgressToBid(mSlider.getProgress()), startingBid, mRuleset));

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
                mLabel.setText(getLabelText(mapProgressToBid(progress), startingBid, mRuleset));
            }
        });

        mLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BidSelectionListener) getActivity()).bidSelected(mapProgressToBid(mSlider
                        .getProgress()));
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

    private int mapProgressToBid(int progress) {
        int minBid = mShowAllBidsMode ? 0 : mRuleset.getMinimumReasonableBid();
        return progress * 5 + minBid;
    }

    private int mapBidToProgress(int startingBid) {
        int minBid = mShowAllBidsMode ? 0 : mRuleset.getMinimumReasonableBid();
        return (startingBid - minBid) / 5;
    }

    /**
     * In the default view, we don't care about the starting bid, just what is
     * selected
     * 
     * @param ruleset
     * @param progress
     * @param targetProgress
     * @return
     */
    protected Spannable getLabelText(int bid, int startingBid, RookRuleSet ruleset) {
        return new SpannableString("" + bid);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bid_menu, menu);

        // add listner to item
        MenuItem item = menu.findItem(R.id.changeToAllPossibleBidsMenuItem);
        item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                int bid = mapProgressToBid(mSlider.getProgress());
                mSlider.setMax(mRuleset.getMaximumBid() / 5);
                mShowAllBidsMode = true;
                mSlider.setProgress(mapBidToProgress(bid));
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
        
        ((PlayRoundActivity)getActivity()).updateBidSummary();

    }
}
