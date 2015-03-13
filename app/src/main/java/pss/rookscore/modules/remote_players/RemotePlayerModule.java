package pss.rookscore.modules.remote_players;

import com.google.common.eventbus.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ca.cgwpope.jsonobjectwrapper.JSONObjectFactoryImpl;
import ca.cgwpope.jsonobjectwrapper.UnderscoreSeparationJSONNamingStrategy;
import pss.rookscore.RookScoreApplication;
import pss.rookscore.events.GameOverEvent;
import pss.rookscore.events.PlayerAddedEvent;
import pss.rookscore.events.PlayerLoadRequestEvent;
import pss.rookscore.events.PlayersRemovedEvent;
import pss.rookscore.fragments.views.ViewUtilities;
import pss.rookscore.model.Player;
import pss.rookscore.model.RoundSummary;

public class RemotePlayerModule implements RookScoreApplication.Module {
    private RookScoreApplication mApplication;
    private String mPlayerAPIURL;
    private String mGameAPIUrl;

    //register the appropriate event listeners
    //When player list load is requested, populate with entries from local store

    public RemotePlayerModule(String playerAPIURL, String gameAPIURL) {
        mPlayerAPIURL = playerAPIURL;
        mGameAPIUrl = gameAPIURL;
    }


    public void initialize(RookScoreApplication app) {
        mApplication = app;
        app.getEventBus().register(this);
    }

    @Override
    public void cleanup(RookScoreApplication app) {
        app.getEventBus().unregister(this);

    }


    @Subscribe
    public void handlePlayerListLoad(final PlayerLoadRequestEvent ev) {
        new Thread() {
            public void run() {
                try {
                    JSONArray jarray = new JSONArray(readJSON(new URL(mPlayerAPIURL)));
                    RemotePlayer players[] = new JSONObjectFactoryImpl(new UnderscoreSeparationJSONNamingStrategy(false)).wrap(RemotePlayer.class, jarray);
                    for (RemotePlayer player : players) {
                        ev.getPlayerSink().addPlayer(new WebApiPlayer(player.getFirstName(), player.getLastName(), player.getPlayerId()));
                    }
                } catch (JSONException e) {
                    e.printStackTrace(System.out);
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
            }
        }.start();
    }


    @Subscribe
    public void handleGameFinished(GameOverEvent ev) {
        if (ev.getGameModel() == null) {
            return;
        }




        JSONObjectFactoryImpl impl = new JSONObjectFactoryImpl(new UnderscoreSeparationJSONNamingStrategy(false));
        final RemoteGame rg = impl.newInstance(RemoteGame.class);

        //2015-01-11T23:58:13.520281Z
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSZ");

        rg.setPlayedDate(sdf.format(new Date()));
        rg.setEnteredDate(sdf.format(new Date()));

        final List<RoundSummary> roundSummaries = ev.getGameModel().computeRoundScores();

        if (roundSummaries.size() > 0) {
            final Map<Player, Integer> lastRoundScores = roundSummaries.get(roundSummaries.size() - 1).getRoundCumulativeScores();

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
                score.setMadeBid(ViewUtilities.playerHasWonARound(entry.getKey(), ev.getGameModel().getRounds()));
                scores[i++] = score;
            }
            rg.setScores(scores);



            //post the game results to website
            new Thread() {
                public void run() {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(mGameAPIUrl).openConnection();
                        connection.setDoInput(true);
                        connection.setDoOutput(true);
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        connection.setRequestProperty("Accept", "application/json");

                        try (Writer w = new OutputStreamWriter(connection.getOutputStream())) {
                            w.append(rg.getDelegate().toString());
                        }

                    } catch (IOException e) {
                        e.printStackTrace(System.out);
                    }
                }
            }.start();
        }

    }

    private String readJSON(URL u) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.connect();

        try (InputStream i = connection.getInputStream()) {

            if (connection.getResponseCode() != 200) {
                throw new IOException(String.format("Unexpected failure from web service - status %d returned", connection.getResponseCode()));
            }

            return readStream(i);

        }
    }


    protected String readStream(InputStream i) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(i)) {
            int length = 0;
            char buf[] = new char[1024];
            while ((length = isr.read(buf)) >= 0) {
                sb.append(buf, 0, length);
            }
        }

        return sb.toString();
    }
}
