package pss.rookscore.core.webapi;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import org.json.JSONArray;
import org.json.JSONException;

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
            score.setPlayer(player.getId());
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

            int partners[] = new int[result.getPartners().size()];
            for(int j = 0; j < partners.length; j++){
                partners[j] = result.getPartners().get(j).getId();
            }


            rr.setPartners(partners);
            int opponents[] = new int[gameModel.getPlayers().size() - partners.length - 1];
            int j = 0;
            for(Player p : gameModel.getPlayers()){
                if(!Ints.contains(partners, p.getId()) && p.getId() != result.getCaller().getId()){
                    opponents[j++] = p.getId();
                }
            }

            rr.setOpponents(opponents);


            rr.setPointsBid(result.getBid());
            rr.setPointsMade(result.getMade());

            rr.setHandNumber(i + 1);
            remoteRounds[i] = rr;

        }

        rg.setBids(remoteRounds);

        return rg;
    }

    public void submitRemoteGame(String gamesResourceURL, String username, String password, RemoteGame rg) throws IOException {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        URL gameResourceURL = new URL(gamesResourceURL);
        String authResourceURL = "http://" + gameResourceURL.getHost() + "/api-auth/login/";


        //to work around the security model, need to perform HTTP Get first - this will provide us with a CRSF token
        //so just GET from the game resource first
        HttpURLConnection connection = (HttpURLConnection) new URL(authResourceURL).openConnection();
        connection.setInstanceFollowRedirects(false);
        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){

            //read the csrf token

            String csrftoken = parseCSRFToken(connection.getHeaderFields().get("Set-Cookie"));


            //perform the POST to logig
            String payload = "username=" + username + "&password="+password+"&csrfmiddlewaretoken=" + csrftoken;


            connection = (HttpURLConnection) new URL(authResourceURL).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            connection.setRequestProperty("Cookie", "csrftoken="+ csrftoken.trim());

            PrintWriter w = null;
            try {

                w = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
                w.print(payload);
                w.flush();
                int responseCode = connection.getResponseCode();

                if(responseCode != HttpURLConnection.HTTP_FORBIDDEN){

                    csrftoken = parseCSRFToken(connection.getHeaderFields().get("Set-Cookie"));

                    connection = (HttpURLConnection) new URL(gamesResourceURL).openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");

                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("X-CSRFToken", csrftoken);

                    //should not be denied this time, cookie should be carried over.
                    //use javax.net.debug to test



                    w = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
                    w.print(rg.getDelegate().toString());
                    w.flush();
                    ByteStreams.copy(connection.getInputStream(), System.out);
                    responseCode = connection.getResponseCode();
                    System.out.println(responseCode);

                } else {
                    //also, copy any response to system.out
                    ByteStreams.copy(connection.getInputStream(), System.out);

                    throw new IOException("Unable to submit - HTTP Response:" + responseCode);
                }
            } finally {
                if(w != null){
                    w.close();
                }
            }
        }
    }

    private String parseCSRFToken(List<String> headerFieldValues) {
        for(String headerFieldValue : headerFieldValues){
            try {
                List<HttpCookie> cookies = HttpCookie.parse(headerFieldValue);
                for (HttpCookie cookie : cookies) {
                    if ("csrftoken".equals(cookie.getName())) {
                        return cookie.getValue();

                    }
                }
            } catch (IllegalArgumentException e){
                continue;
            }

        }
        return "";
    }

    public RemotePlayer[] parseRemotePlayers(String json)  {
        JSONArray jarray = new JSONArray(json);
        return new JSONObjectFactoryImpl(new UnderscoreSeparationJSONNamingStrategy(false)).wrap(RemotePlayer.class, jarray);
    }
}
