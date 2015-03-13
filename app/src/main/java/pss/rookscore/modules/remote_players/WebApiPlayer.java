package pss.rookscore.modules.remote_players;

import pss.rookscore.model.Player;


public class WebApiPlayer extends Player {
    private final int mPlayerID;

    public WebApiPlayer(String firstName, String lastName, int playerId) {

        super(firstName, lastName);
        mPlayerID = playerId;
    }


    @Override
    public int getId() {
        return mPlayerID;
    }

}
