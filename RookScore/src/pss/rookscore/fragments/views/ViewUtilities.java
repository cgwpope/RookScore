
package pss.rookscore.fragments.views;

import java.util.List;

import android.content.Context;
import android.graphics.Paint;

public class ViewUtilities {

    static final int TEXT_SIZE = 16;

     static final int DEFAULT_ROUND_SUMMARY_WIDTH = 100;

    static String shorterName(List<String> allNames, String name) {

        // go with initials
        // if conflict with another set of initials, add next letter from first
        // of each name section until no match
        String nameParts[] = name.split("\\s");
        StringBuilder initials = new StringBuilder();

        if (nameParts.length == 0) {
            initials.append(name);
        } else if (nameParts.length == 1) {
            initials.append(nameParts[0].substring(0, Math.max(3, nameParts[0].length())));
        } else {
            for (String string : nameParts) {
                initials.append(string.charAt(0));
            }
        }

        return initials.toString();
    }

    static float computeCentredStringStart(float leftmost, float fullWidth, float textWidth) {
        return (leftmost + fullWidth / 2) - textWidth / 2;
    }

    static void summarizeRoundResult(StringBuilder roundSummaryText, RoundSummary summary, List<String> players) {
        roundSummaryText
                .append(ViewUtilities.shorterName(players, summary.getRoundResult()
                        .getCaller()))
                .append(' ')
                .append('(')
                .append(summary.getRoundResult().getBid())
                .append(')');

        if (summary.getRoundResult().getCaller().equals(summary.getRoundResult().getParter())) {
            roundSummaryText.append(" -- ");
        } else {
            roundSummaryText
                    .append(',')
                    .append(ViewUtilities.shorterName(players, summary.getRoundResult()
                            .getParter()))
                    .append(" - ");
        }

        roundSummaryText.append(summary.getRoundResult().getMade());

        if (summary.getRoundResult().getMade() >= summary.getRoundResult().getBid()) {
            roundSummaryText.append('\u2713'); // checkmark
        } else {
            roundSummaryText.append('\u2717'); // X
        }
    }

    static float computeRowHeight(Paint p, Context c) {
        return -p.getFontMetrics().ascent + p.getFontMetrics().descent + scaleText(c, 4);
    }
    
    public static float computeRoundSummaryWidth(List<RoundSummary> roundSummaries, Paint paint, List<String> players) {
        float maxWidth = DEFAULT_ROUND_SUMMARY_WIDTH;
        StringBuilder sb = new StringBuilder();
        for (RoundSummary roundSummary : roundSummaries) {
            summarizeRoundResult(sb, roundSummary, players);
            float length = paint.measureText(sb.toString());
            
            //add a bit of padding around text
            length += 10;
            
            if(length > maxWidth){
                maxWidth = length;
            }
            sb.setLength(0);
        }
        

        
        
        return maxWidth;
            
        
    }
    
    static float scaleText(Context c, int size){
        return c.getResources().getDisplayMetrics().density * size;
    }
}
