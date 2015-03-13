package pss.rookscore.events;

import pss.rookscore.model.Player;

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
