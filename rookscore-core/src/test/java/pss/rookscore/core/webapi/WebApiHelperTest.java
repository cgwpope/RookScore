package pss.rookscore.core.webapi;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import pss.rookscore.core.model.GameStateModel;
import pss.rookscore.core.model.Player;
import pss.rookscore.core.model.RoundSummary;
import pss.rookscore.core.ruleset.CambridgeFourPlayerRookRuleSet;

public class WebApiHelperTest  {
    private Player[] mPlayers;
    private GameStateModel.RoundResult mRoundResult;
    private GameStateModel mGameStateModel;


    private static  final String SAMPLE_JSON  = "[{\"id\":7,\"player_id\":1111,\"first_name\":\"Allan\",\"last_name\":\"B\"},{\"id\":1,\"player_id\":1235,\"first_name\":\"Brad\",\"last_name\":\"C\"},{\"id\":2,\"player_id\":1234,\"first_name\":\"Chris\",\"last_name\":\"P\"},{\"id\":3,\"player_id\":132421,\"first_name\":\"Jeremy\",\"last_name\":\"vdM\"},{\"id\":5,\"player_id\":1230,\"first_name\":\"John\",\"last_name\":\"K\"},{\"id\":6,\"player_id\":10,\"first_name\":\"John\",\"last_name\":\"S\"},{\"id\":4,\"player_id\":31312,\"first_name\":\"Martin\",\"last_name\":\"V\"},{\"id\":8,\"player_id\":9912,\"first_name\":\"Ray\",\"last_name\":\"F\"}]";

    @Before
    public void setUp() {

        mPlayers = new Player[] {
                new WebApiPlayer("Allan", "B", 7, 1111),
                new WebApiPlayer("Bradley", "Chruszcz", 1, 12345),
                new WebApiPlayer("Chris", "Pope", 2, 1234),
                new WebApiPlayer("Jeremy", "van der Munnik", 3, 132421)
        };



        //single round
        mRoundResult = new GameStateModel.RoundResult(
                new CambridgeFourPlayerRookRuleSet(),
                mPlayers[0],
                Arrays.asList(mPlayers[1]),
                150, 160);


        mGameStateModel = new GameStateModel();
        mGameStateModel.getPlayers().addAll(Arrays.asList(mPlayers));
        mGameStateModel.getRounds().add(mRoundResult);


    }


    public void tearDown() {

    }


    @Test
    public void testJSONParsing() throws JSONException {
        final RemotePlayer[] remotePlayers = new WebApiHelper().parseRemotePlayers(SAMPLE_JSON);
        Assert.assertEquals(8, remotePlayers.length);
        Assert.assertEquals(7, remotePlayers[0].getId());
        Assert.assertEquals("Allan", remotePlayers[0].getFirstName());
        Assert.assertEquals("B", remotePlayers[0].getLastName());

        Assert.assertEquals(8, remotePlayers[7].getId());
        Assert.assertEquals("Ray", remotePlayers[7].getFirstName());
        Assert.assertEquals("F", remotePlayers[7].getLastName());
    }

    @Test
    public void testSimpleJSONGeneration() throws JSONException, IOException {

        WebApiHelper helper = new WebApiHelper();

        final List<RoundSummary> roundSummaries = mGameStateModel.computeRoundScores();
        final RoundSummary lastRound = roundSummaries.get(roundSummaries.size() - 1);

        final RemoteGame remoteGameModel = helper.buildWebServiceGame(mGameStateModel, lastRound.getRoundCumulativeScores(), mGameStateModel.getRounds());


        //test  that remoteGameModel looks OK
        //ignore some fields
        Assert.assertNotNull(remoteGameModel.getEnteredDate());
        Assert.assertNotNull(remoteGameModel.getPlayedDate());

        Assert.assertEquals(4, remoteGameModel.getScores().length);

        System.out.println(remoteGameModel.getDelegate().toString(2));

        helper.submitRemoteGame("http://beta.rook2.chruszcz.ca/api/games/", "chris", "chrispope",remoteGameModel);
    }
}