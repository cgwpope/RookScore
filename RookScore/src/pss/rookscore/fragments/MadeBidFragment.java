package pss.rookscore.fragments;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

public class MadeBidFragment extends BidFragment {
	/**
	 * When making the bid, highlight the +/- from the starting bid
	 */
	@Override
	protected Spannable getLabelText(int bid, int startingBid) {
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

		// Now, apply some colour
		Spannable span = new SpannableString(made + " (" + diffText + ")");

		if (diff >= 0) {
			span.setSpan(new ForegroundColorSpan(Color.GREEN),
					made.length() + 1, span.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else if (diff < 0) {
			span.setSpan(new ForegroundColorSpan(Color.RED), made.length() + 1,
					span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return span;
	}
}
