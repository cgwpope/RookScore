package pss.rookscore.modules.remote_players;

import com.google.common.eventbus.Subscribe;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import pss.rookscore.RookScoreApplication;
import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.model.RoundSummary;
import pss.rookscore.core.events.GameOverEvent;
import pss.rookscore.core.events.PlayerLoadRequestEvent;
import pss.rookscore.core.webapi.RemoteGame;
import pss.rookscore.core.webapi.RemotePlayer;
import pss.rookscore.core.webapi.WebApiHelper;
import pss.rookscore.core.webapi.WebApiPlayer;

public class RemotePlayerModule implements RookScoreApplication.Module {
    private final String mUsername;
    private final String mPassword;
    private RookScoreApplication mApplication;
    private String mPlayerAPIURL;
    private String mGameAPIUrl;

    //register the appropriate event listeners
    //When player list load is requested, populate with entries from local store


    public RemotePlayerModule(String playerAPIURL, String gameAPIURL, String username, String password) {
        mPlayerAPIURL = playerAPIURL;
        mGameAPIUrl = gameAPIURL;
        mUsername  = username;
        mPassword = password;

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
                    final String json = readJSON(new URL(mPlayerAPIURL));
                    RemotePlayer[] players = new WebApiHelper().parseRemotePlayers(json);
                    for (RemotePlayer player : players) {
                        ev.getPlayerSink().addPlayer(new WebApiPlayer(player.getFirstName(), player.getLastName(), player.getId(), player.getPlayerId()));
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
            }
        }.start();
    }




    @Subscribe
    public void handleGameFinished(GameOverEvent ev) {

        //Todo: push this into webapihelper

        if (ev.getGameModel() == null) {
            return;
        }





        final List<RoundSummary> roundSummaries = ev.getGameModel().computeRoundScores();

        if (roundSummaries.size() > 0) {
            final Map<Player, Integer> lastRoundScores = roundSummaries.get(roundSummaries.size() - 1).getRoundCumulativeScores();
            final List<GameStateModel.RoundResult> rounds = ev.getGameModel().getRounds();


            final RemoteGame rg = new WebApiHelper().buildWebServiceGame(lastRoundScores, rounds);

            //post the game results to website
            new Thread() {
                public void run() {
                    try {
                        new WebApiHelper().submitRemoteGame(mGameAPIUrl, mUsername, mPassword, rg);
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
