package pss.rookscore.events;

import java.util.List;

import pss.rookscore.model.Player;


public class PlayersRemovedEvent {
    private final List<Player> mPlayers;

    public PlayersRemovedEvent(List<Player> players) {
        mPlayers = players;
    }

    public List<Player> getPlayers() {
        return mPlayers;
    }
}
