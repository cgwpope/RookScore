
package pss.rookscore.fragments.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pss.rookscore.model.GameStateModel.RoundResult;
import pss.rookscore.model.Player;
import pss.rookscore.model.RoundSummary;
import android.content.Context;
import android.graphics.Paint;

public class ViewUtilities {

    static final int TEXT_SIZE = 16;

    static String shorterName(List<Player> allPlayers, Player player) {

        // go with initials
        // if conflict with another set of initials, add next letter from first
        // of each name section until no match
        String nameParts[] = new String[] {player.getFirstname(), player.getLastname()};
        StringBuilder initials = new StringBuilder();

        for (String string : nameParts) {
            if(string.length() > 0){
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

    public static void sortPlayerNames(List<Player> playerNames, final List<RoundResult> roundResults, List<RoundSummary> roundScores) {
        final RoundSummary latestScores = roundScores.get(roundScores.size() - 1);

        Collections.sort(playerNames, new Comparator<Player>() {

            @Override
            public int compare(Player lhs, Player rhs) {
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

    public static boolean playerHasWonARound(Player player, List<RoundResult> rounds) {
        for (RoundResult rr : rounds) {
            if (player.equals(rr.getCaller()) && rr.getMade() >= rr.getBid()) {
                return true;
            }
        }

        return false;
    }

    public static void summarizeCompleteRoundResult(StringBuilder roundSummaryText, RoundResult roundResult, List<Player> players) {
        summarizeFirstLineRoundSummary(roundSummaryText, players, roundResult);
        roundSummaryText.append(' ');
        summarizeSecondLineRoundResult(roundSummaryText, players, roundResult);
    }



    static void summarizeSecondLineRoundResult(StringBuilder roundSummaryText, List<Player> playerNames, RoundResult roundResult) {
        if (roundResult.getCaller() != null) {

            if (roundResult.getBid() > 0) {
            
                // check for going alone
                Set<Player> offense = new HashSet<Player>();
                offense.add(roundResult.getCaller());
                for (Player partner : roundResult.getPartners()) {
                    offense.add(partner);
                }

                if (offense.size() == 1) {
                    roundSummaryText.append(" -- ");
                } else {
                    offense.remove(roundResult.getCaller());
                    boolean firstPartner = true;
                    for (Player partner : offense) {
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

    static void summarizeFirstLineRoundSummary(StringBuilder roundSummaryText, List<Player> playerNames, RoundResult roundResult) {
        if (roundResult.getCaller() != null) {
            roundSummaryText.append(ViewUtilities.shorterName(playerNames, roundResult.getCaller())).append(' ');

            if (roundResult.getBid() > 0) {
                roundSummaryText.append('(').append(roundResult.getBid()).append(')');
            }
        }
    }

    public static float computeLineHeight(Context context, Paint paint) {
        return -paint.getFontMetrics().ascent + paint.getFontMetrics().descent + ViewUtilities.scaleText(context, 1);
    }

    public static Paint defaultTextPaint(Context context) {
        Paint defaultTextPaint = new Paint(); 
        defaultTextPaint.setTextSize(ViewUtilities.scaleText(context, ViewUtilities.TEXT_SIZE));
        defaultTextPaint.setAntiAlias(true);
        return defaultTextPaint;

    }
}
