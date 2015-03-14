package pss.rookscore.modules.remote_players;

import junit.framework.TestCase;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

import pss.rookscore.model.GameStateModel;
import pss.rookscore.model.Player;
import pss.rookscore.model.RoundSummary;
import pss.rookscore.ruleset.CambridgeFourPlayerRookRuleSet;

public class WebApiHelperTest extends TestCase {


    private Player[] mPlayers;
    private GameStateModel.RoundResult mRoundResult;
    private GameStateModel mGameStateModel;


    private static  final String SAMPLE_JSON  = "[{\"id\":7,\"player_id\":1111,\"first_name\":\"Allan\",\"last_name\":\"B\"},{\"id\":1,\"player_id\":1235,\"first_name\":\"Brad\",\"last_name\":\"C\"},{\"id\":2,\"player_id\":1234,\"first_name\":\"Chris\",\"last_name\":\"P\"},{\"id\":3,\"player_id\":132421,\"first_name\":\"Jeremy\",\"last_name\":\"vdM\"},{\"id\":5,\"player_id\":1230,\"first_name\":\"John\",\"last_name\":\"K\"},{\"id\":6,\"player_id\":10,\"first_name\":\"John\",\"last_name\":\"S\"},{\"id\":4,\"player_id\":31312,\"first_name\":\"Martin\",\"last_name\":\"V\"},{\"id\":8,\"player_id\":9912,\"first_name\":\"Ray\",\"last_name\":\"F\"}]";

    public void setUp() {
        
        mPlayers = new Player[4];
        for(int i = 0; i < mPlayers.length; i++){
            mPlayers[i] = new WebApiPlayer("fn"  + i, "ln" + i, i);
        }

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


    public void testJSONParsing() throws JSONException {
        final RemotePlayer[] remotePlayers = new WebApiHelper().parseRemotePlayers(SAMPLE_JSON);
        assertEquals(8, remotePlayers.length);
        assertEquals(1111, remotePlayers[0].getPlayerId());
        assertEquals("Allan", remotePlayers[0].getFirstName());
        assertEquals("B", remotePlayers[0].getLastName());

        assertEquals(9912, remotePlayers[7].getPlayerId());
        assertEquals("Ray", remotePlayers[7].getFirstName());
        assertEquals("F", remotePlayers[7].getLastName());
    }


    public void testSimpleJSONGeneration() throws JSONException {

        WebApiHelper helper = new WebApiHelper();

        final List<RoundSummary> roundSummaries = mGameStateModel.computeRoundScores();
        final RoundSummary lastRound = roundSummaries.get(roundSummaries.size() - 1);

        final RemoteGame remoteGameModel = helper.buildWebServiceGame(lastRound.getRoundCumulativeScores(), mGameStateModel.getRounds());


        //test  that remoteGameModel looks OK
        //ignore some fields
        assertNotNull(remoteGameModel.getEnteredDate());
        assertNotNull(remoteGameModel.getPlayedDate());

        assertEquals(4, remoteGameModel.getScores().length);

        System.out.println(remoteGameModel.getDelegate().toString(2));
    }

}