package pss.rookscore.core.events;


import pss.rookscore.core.model.Player;

/**
 * Created by t890428 on 2015-02-08.
 */
public class PlayerAddedEvent {
    private final Player mPlayer;

    public PlayerAddedEvent(Player player) {
        mPlayer = player;
    }

    public Player getPlayer() {
        return mPlayer;
    }
}
