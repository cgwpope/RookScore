package pss.rookscore.core.webapi;


import pss.rookscore.core.model.Player;

public class WebApiPlayer extends Player {
    private final int mPlayerID;
    private final int mId;

    public WebApiPlayer(String firstName, String lastName, int id, int playerId) {

        super(firstName, lastName);
        mPlayerID = playerId;
        mId = id;
    }


    public int getmPlayerID() {
        return mPlayerID;
    }

    @Override
    public int getId() {
        return mId;
    }

}
