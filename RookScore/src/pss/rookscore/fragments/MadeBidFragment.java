package pss.rookscore.fragments;

import pss.rookscore.ruleset.RookRuleSet;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

public class MadeBidFragment extends BidFragment {
	/**
	 * When making the bid, highlight the +/- from the starting bid
	 */
	@Override
	protected Spannable getLabelText(int bid, int startingBid, RookRuleSet rrs) {
		String made = "" + bid;
		int diff = bid - startingBid;
		String diffText = "" + diff;
		if (diff > 0) {
			diffText = "+" + diffText;
		} else if (diff < 0) {
			// Will already have a minus sign
		} else {
			// Even, nothing to do
		}

		String lost = "";
		if(rrs != null){
		    lost = "Lost:" + (rrs.getMaximumBid() - bid);		    
		}
		
		
		
		// Now, apply some colour
		diffText = " (" + diffText + ")";
        Spannable span = new SpannableString(made + diffText + "\n" + lost);

		if (diff >= 0) {
			span.setSpan(new ForegroundColorSpan(Color.GREEN),
					made.length() + 1,made.length() + diffText.length() + 1,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else if (diff < 0) {
			span.setSpan(new ForegroundColorSpan(Color.RED), made.length() + 1,
					made.length() + diffText.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return span;
	}
}
