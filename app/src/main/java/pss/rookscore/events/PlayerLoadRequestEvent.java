package pss.rookscore.events;

import pss.rookscore.model.Player;


public class PlayerLoadRequestEvent {

    private final PlayerSink mPlayerSink;

    public static interface PlayerSink {
        public void addPlayer(Player localStorePlayer);
    }


    public PlayerLoadRequestEvent(PlayerSink sink){
        mPlayerSink = sink;
    }

    public PlayerSink getPlayerSink() {
        return mPlayerSink;
    }
}
