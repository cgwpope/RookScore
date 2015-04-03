package pss.rookscore.core.webapi;

import com.google.common.io.ByteStreams;

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

    public RemoteGame buildWebServiceGame(Map<Player, Integer> lastRoundScores, List<GameStateModel.RoundResult> rounds) {
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
            player.setId(((WebApiPlayer) entry.getKey()).getId());
            player.setPlayerId(((WebApiPlayer) entry.getKey()).getmPlayerID());
            score.setPlayer(player);
            score.setScore(entry.getValue());
            score.setMadeBid(ModelUtilities.playerHasWonARound(entry.getKey(), rounds));
            scores[i++] = score;
        }
        rg.setScores(scores);
        rg.setBids(new String[0]);
        return rg;
    }

    public void submitRemoteGame(String gamesResourceURL, String username, String password, RemoteGame rg) throws IOException {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        //to work around the security model, need to perform HTTP Get first - this will provide us with a CRSF token
        //so just GET from the game resource first
        HttpURLConnection connection = (HttpURLConnection) new URL("http://rook2.chruszcz.ca/api-auth/login/").openConnection();
        connection.setInstanceFollowRedirects(false);
        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){

            //read the csrf token

            String csrftoken = parseCSRFToken(connection.getHeaderFields().get("Set-Cookie"));


            //perform the POST to logig
            String payload = "username=" + username + "&password="+password+"&csrfmiddlewaretoken=" + csrftoken;


            connection = (HttpURLConnection) new URL("http://rook2.chruszcz.ca/api-auth/login/").openConnection();
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
