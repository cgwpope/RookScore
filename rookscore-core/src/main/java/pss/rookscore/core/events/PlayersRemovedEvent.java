package pss.rookscore.core.events;

import java.util.List;

import pss.rookscore.core.model.Player;


public class PlayersRemovedEvent {
    private final List<Player> mPlayers;

    public PlayersRemovedEvent(List<Player> players) {
        mPlayers = players;
    }

    public List<Player> getPlayers() {
        return mPlayers;
    }
}
