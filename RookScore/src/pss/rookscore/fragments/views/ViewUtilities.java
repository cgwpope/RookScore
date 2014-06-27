
package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pss.rookscore.model.GameStateModel.RoundResult;
import pss.rookscore.model.RoundSummary;
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

    public static void summarizeRoundResult(StringBuilder roundSummaryText, RoundResult result,
            List<String> players) {

        if (result.getCaller() != null) {
            roundSummaryText
                    .append(ViewUtilities.shorterName(players, result
                            .getCaller()))
                    .append(' ');

            if (result.getBid() > 0) {
                roundSummaryText.append('(')
                        .append(result.getBid())
                        .append(')');

                // check for going alone
                Set<String> offense = new HashSet<String>();
                offense.add(result.getCaller());
                for (String partner : result.getParters()) {
                    offense.add(partner);
                }

                if (offense.size() == 1) {
                    roundSummaryText.append(" -- ");
                } else {
                    offense.remove(result.getCaller());
                    for (String partner : offense) {
                        roundSummaryText
                                .append(',')
                                .append(ViewUtilities.shorterName(players, partner));

                    }
                    roundSummaryText.append(" - ");
                }

                // require a made bid include in summary
                if (result.getMade() > 0) {

                    roundSummaryText.append(result.getMade());

                    if (result.getMade() >= result.getBid()) {
                        roundSummaryText.append('\u2713'); // checkmark
                    } else {
                        roundSummaryText.append('\u2717'); // X
                    }
                }

            }

        }

    }

    static float computeRowHeight(Paint p, Context c) {
        return -p.getFontMetrics().ascent + p.getFontMetrics().descent + scaleText(c, 4);
    }

    public static float computeRoundSummaryWidth(List<RoundSummary> roundSummaries, Paint paint,
            List<String> players) {
        float maxWidth = DEFAULT_ROUND_SUMMARY_WIDTH;
        StringBuilder sb = new StringBuilder();
        for (RoundSummary roundSummary : roundSummaries) {
            summarizeRoundResult(sb, roundSummary.getRoundResult(), players);
            float length = paint.measureText(sb.toString());

            // add a bit of padding around text
            length += 10;

            if (length > maxWidth) {
                maxWidth = length;
            }
            sb.setLength(0);
        }

        return maxWidth;

    }

    static float scaleText(Context c, int size) {
        return c.getResources().getDisplayMetrics().density * size;
    }

    public static void sortPlayerNames(ArrayList<String> playerNames, final List<RoundResult> roundResults, List<RoundSummary> roundScores) {
        final RoundSummary latestScores = roundScores.get(roundScores.size() - 1);
        
        Collections.sort(playerNames, new Comparator<String>(){

            @Override
            public int compare(String lhs, String rhs) {
                boolean lhsWonRound = playerHasWonARound(lhs, roundResults);
                boolean rhsWonRound = playerHasWonARound(rhs, roundResults);
                
                if(lhsWonRound == rhsWonRound){
                    //need to compare scores
                    return latestScores.getRoundCumulativeScores().get(rhs) - latestScores.getRoundCumulativeScores().get(lhs);
                } else {
                    if(lhsWonRound && !rhsWonRound){
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
            
        });        
    }
    
    public static boolean playerHasWonARound(String playerName, List<RoundResult> rounds) {
        for (RoundResult rr : rounds) {
            if (playerName.equals(rr.getCaller()) && rr.getMade() >= rr.getBid()) {
                return true;
            }
        }

        return false;
    }
}
