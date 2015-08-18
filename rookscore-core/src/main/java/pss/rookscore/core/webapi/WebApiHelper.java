package pss.rookscore.core.webapi;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ca.cgwpope.jsonobjectwrapper.JSONObjectFactoryImpl;
import ca.cgwpope.jsonobjectwrapper.UnderscoreSeparationJSONNamingStrategy;
import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.ModelUtilities;
import pss.rookscore.core.model.Player;

/**
 * Created by cgwpope on 2015-03-13.
 */
public class WebApiHelper {

    public RemoteGame buildWebServiceGame(GameStateModel gameModel, Map<Player, Integer> lastRoundScores, List<GameStateModel.RoundResult> rounds) {
        JSONObjectFactoryImpl impl = new JSONObjectFactoryImpl(new UnderscoreSeparationJSONNamingStrategy(false));
        final RemoteGame rg = impl.newInstance(RemoteGame.class);

        //2015-01-11T23:58:13.520281Z
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSZ");

        rg.setPlayedDate(sdf.format(new Date()));
        rg.setEnteredDate(sdf.format(new Date()));


        //provide a scoring summary
        RemoteGameScore scores[] = new RemoteGameScore[lastRoundScores.size()];
        int i = 0;
        for (Map.Entry<Player, Integer> entry : lastRoundScores.entrySet()) {
            RemoteGameScore score = impl.newInstance(RemoteGameScore.class);
            RemotePlayer player = impl.newInstance(RemotePlayer.class);
            player.setFirstName(entry.getKey().getFirstname());
            player.setLastName(entry.getKey().getLastname());
            player.setId(((WebApiPlayer) entry.getKey()).getId());
            player.setPlayerId(((WebApiPlayer) entry.getKey()).getmPlayerID());
            score.setPlayer(player);
            score.setScore(entry.getValue());
            score.setMadeBid(ModelUtilities.playerHasWonARound(entry.getKey(), rounds));
            scores[i++] = score;
        }
        rg.setScores(scores);

        //now, provide the scoring history;
        RemoteRound remoteRounds[] = new RemoteRound[rounds.size()];
        for(i = 0; i < rounds.size(); i++){
            RemoteRound rr = impl.newInstance(RemoteRound.class);
            GameStateModel.RoundResult result = rounds.get(i);
            rr.setCaller(result.getCaller().getId());

            // Partners
            List<Integer> partners = new ArrayList<>();
            for(int j = 0; j < result.getPartners().size(); j++){
                partners.add(result.getPartners().get(j).getId());
            }
            rr.setPartners(partners.toArray(new Integer[partners.size()]));

            // Opponents
            List<Integer> opponents = new ArrayList<>();
            for(Player p : gameModel.getPlayers()){
                // Add anyone who's not a partner or the caller
                if(partners.contains(p.getId()) && p.getId() != result.getCaller().getId()){
                    opponents.add(p.getId());
                }
            }
            rr.setOpponents(opponents.toArray(new Integer[opponents.size()]));

            // Bidding details
            rr.setPointsBid(result.getBid());
            rr.setPointsMade(result.getMade());

            rr.setHandNumber(i + 1);
            remoteRounds[i] = rr;

        }

        rg.setBids(remoteRounds);

        return rg;
    }

    public void submitRemoteGame(String gamesResourceURL, String username, String password, RemoteGame rg) throws IOException {
        HttpURLConnection connection = null;
        PrintWriter w = null;
        try {
            connection = (HttpURLConnection) new URL(gamesResourceURL).openConnection();
            connection.setInstanceFollowRedirects(true);
//            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Api-Key", "12345");

            w = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
            String json = rg.getDelegate().toString();
            w.print(json);
            w.flush();

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            //System.out.println("Copy of input stream:");
            //ByteStreams.copy(connection.getInputStream(), System.out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(w != null) {
                w.close();
            }
        }
    }

    public RemotePlayer[] parseRemotePlayers(String json)  {
        JSONArray jarray = new JSONArray(json);
        return new JSONObjectFactoryImpl(new UnderscoreSeparationJSONNamingStrategy(false)).wrap(RemotePlayer.class, jarray);
    }
}
