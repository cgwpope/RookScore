package pss.rookscore.core.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cgwpope on 2015-04-03.
 */
public class ModelUtilities {
    public static String shorterName(List<Player> allPlayers, Player player) {

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

    public static void sortPlayerNames(List<Player> playerNames, final List<GameStateModel.RoundResult> roundResults, List<RoundSummary> roundScores) {
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

    public static boolean playerHasWonARound(Player player, List<GameStateModel.RoundResult> rounds) {
        for (GameStateModel.RoundResult rr : rounds) {
            if (player.equals(rr.getCaller()) && rr.getMade() >= rr.getBid()) {
                return true;
            }
        }

        return false;
    }



    public static String summarizeCompleteRoundResult(GameStateModel.RoundResult roundResult, List<Player> players) {
        StringBuilder sb = new StringBuilder();
        ModelUtilities.summarizeCompleteRoundResult(sb, roundResult, players);
        return sb.toString();
    }


    public static void summarizeCompleteRoundResult(StringBuilder roundSummaryText, GameStateModel.RoundResult roundResult, List<Player> players) {
        summarizeFirstLineRoundSummary(roundSummaryText, players, roundResult);
        roundSummaryText.append(' ');
        summarizeSecondLineRoundResult(roundSummaryText, players, roundResult);
    }

    public static void summarizeSecondLineRoundResult(StringBuilder roundSummaryText, List<Player> playerNames, GameStateModel.RoundResult roundResult) {
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
                        roundSummaryText.append(shorterName(playerNames, partner));
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

    public static void summarizeFirstLineRoundSummary(StringBuilder roundSummaryText, List<Player> playerNames, GameStateModel.RoundResult roundResult) {
        if (roundResult.getCaller() != null) {
            roundSummaryText.append(shorterName(playerNames, roundResult.getCaller())).append(' ');

            if (roundResult.getBid() > 0) {
                roundSummaryText.append('(').append(roundResult.getBid()).append(')');
            }
        }
    }
}
