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

    /**
     *
     * @param sb - StringBuilder that result will be appended to
     * @param allPlayers
     * @param player
     */
    public static void writeShorterName(StringBuilder sb, List<Player> allPlayers, Player player) {

        // go with initials
        // if conflict with another set of initials, add next letter from first
        // of each name section until no match

        if(player.getFirstname().length() > 0){
            sb.append(player.getFirstname().charAt(0));
        }

        if(player.getLastname().length() > 0){
            sb.append(player.getLastname().charAt(0));
        }

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
//                Set<Player> offense = new HashSet<Player>();
//                offense.add(roundResult.getCaller());

                boolean hasPartner = false;
                for (int i = 0; i < roundResult.getPartners().size(); i++) {
                    if(!roundResult.getPartners().get(i).equals(roundResult.getCaller())){
                        hasPartner = true;
                        break;
                    }
                }


                if (!hasPartner) {
                    roundSummaryText.append(" -- ");
                } else {
                    boolean firstPartner = true;
                    for (int i = 0; i < roundResult.getPartners().size(); i++) {
                        Player partner = roundResult.getPartners().get(i);
                        if(!firstPartner){
                            roundSummaryText.append(',');
                        } else {
                            firstPartner = false;
                        }
                        writeShorterName(roundSummaryText, playerNames, partner);
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
            writeShorterName(roundSummaryText, playerNames, roundResult.getCaller());
            roundSummaryText.append(' ');

            if (roundResult.getBid() > 0) {
                roundSummaryText.append('(').append(roundResult.getBid()).append(')');
            }
        }
    }
}
