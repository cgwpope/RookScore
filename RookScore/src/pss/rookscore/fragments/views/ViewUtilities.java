
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

    static final char X_CHAR = '\u2717';
    static final char CHECKMARK_CHAR = '\u2713';
    static final int TEXT_SIZE = 16;

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

    static float scaleText(Context c, int size) {
        return c.getResources().getDisplayMetrics().density * size;
    }

    public static void sortPlayerNames(ArrayList<String> playerNames, final List<RoundResult> roundResults, List<RoundSummary> roundScores) {
        final RoundSummary latestScores = roundScores.get(roundScores.size() - 1);

        Collections.sort(playerNames, new Comparator<String>() {

            @Override
            public int compare(String lhs, String rhs) {
                boolean lhsWonRound = playerHasWonARound(lhs, roundResults);
                boolean rhsWonRound = playerHasWonARound(rhs, roundResults);

                if (lhsWonRound == rhsWonRound) {
                    // need to compare scores
                    return latestScores.getRoundCumulativeScores().get(rhs) - latestScores.getRoundCumulativeScores().get(lhs);
                } else {
                    if (lhsWonRound && !rhsWonRound) {
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

    public static void summarizeCompleteRoundResult(StringBuilder roundSummaryText, RoundResult roundResult, List<String> players) {
        summarizeFirstLineRoundSummary(roundSummaryText, players, roundResult);
        roundSummaryText.append(' ');
        summarizeSecondLineRoundResult(roundSummaryText, players, roundResult);
    }



    static void summarizeSecondLineRoundResult(StringBuilder roundSummaryText, List<String> playerNames, RoundResult roundResult) {
        if (roundResult.getCaller() != null) {

            if (roundResult.getBid() > 0) {
            
                // check for going alone
                Set<String> offense = new HashSet<String>();
                offense.add(roundResult.getCaller());
                for (String partner : roundResult.getParters()) {
                    offense.add(partner);
                }

                if (offense.size() == 1) {
                    roundSummaryText.append(" -- ");
                } else {
                    offense.remove(roundResult.getCaller());
                    boolean firstPartner = true;
                    for (String partner : offense) {
                        if(!firstPartner){
                            roundSummaryText.append(',');    
                        } else {
                            firstPartner = false;
                        }
                        roundSummaryText.append(ViewUtilities.shorterName(playerNames, partner));
                    }
                    roundSummaryText.append(" - ");
                }

                // require a made bid include in summary
                if (roundResult.getMade() > 0) {
                    roundSummaryText.append(roundResult.getMade());
                }
            }
        }
    }

    static void summarizeFirstLineRoundSummary(StringBuilder roundSummaryText, List<String> playerNames, RoundResult roundResult) {
        if (roundResult.getCaller() != null) {
            roundSummaryText.append(ViewUtilities.shorterName(playerNames, roundResult.getCaller())).append(' ');

            if (roundResult.getBid() > 0) {
                roundSummaryText.append('(').append(roundResult.getBid()).append(')');
            }
        }
    }

    public static float computeLineHeight(Context context, Paint paint) {
        return -paint.getFontMetrics().ascent + paint.getFontMetrics().descent + ViewUtilities.scaleText(context, 4);
    }

    public static Paint defaultTextPaint(Context context) {
        Paint defaultTextPaint = new Paint(); 
        defaultTextPaint.setTextSize(ViewUtilities.scaleText(context, ViewUtilities.TEXT_SIZE));
        defaultTextPaint.setAntiAlias(true);
        return defaultTextPaint;

    }
}
