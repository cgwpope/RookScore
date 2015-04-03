package pss.rookscore.core.events;


import pss.rookscore.core.model.Player;

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
