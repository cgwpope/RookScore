package pss.rookscore.modules.remote_players;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ca.cgwpope.jsonobjectwrapper.JSONObjectFactoryImpl;
import ca.cgwpope.jsonobjectwrapper.UnderscoreSeparationJSONNamingStrategy;
import pss.rookscore.fragments.views.ViewUtilities;
import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.Player;

/**
 * Created by cgwpope on 2015-03-13.
 */
class WebApiHelper {

    RemoteGame buildWebServiceGame(Map<Player, Integer> lastRoundScores, List<GameStateModel.RoundResult> rounds) {
        JSONObjectFactoryImpl impl = new JSONObjectFactoryImpl(new UnderscoreSeparationJSONNamingStrategy(false));
        final RemoteGame rg = impl.newInstance(RemoteGame.class);

        //2015-01-11T23:58:13.520281Z
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSZ");

        rg.setPlayedDate(sdf.format(new Date()));
        rg.setEnteredDate(sdf.format(new Date()));


        RemoteGameScore scores[] = new RemoteGameScore[lastRoundScores.size()];
        int i = 0;
        for (Map.Entry<Player, Integer> entry : lastRoundScores.entrySet()) {
            RemoteGameScore score = impl.newInstance(RemoteGameScore.class);
            RemotePlayer player = impl.newInstance(RemotePlayer.class);
            player.setFirstName(entry.getKey().getFirstname());
            player.setLastName(entry.getKey().getLastname());
            player.setPlayerId(((WebApiPlayer) entry.getKey()).getId());
            score.setPlayer(player);
            score.setScore(entry.getValue());
            score.setMadeBid(ViewUtilities.playerHasWonARound(entry.getKey(), rounds));
            scores[i++] = score;
        }
        rg.setScores(scores);
        return rg;
    }

    RemotePlayer[] parseRemotePlayers(String json) throws JSONException {
        JSONArray jarray = new JSONArray(json);
        return new JSONObjectFactoryImpl(new UnderscoreSeparationJSONNamingStrategy(false)).wrap(RemotePlayer.class, jarray);
    }
}
