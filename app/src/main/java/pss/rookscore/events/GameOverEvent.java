package pss.rookscore.events;

import pss.rookscore.model.GameStateModel;

public class GameOverEvent {

    private final GameStateModel mGameModel;

    public GameOverEvent(GameStateModel gameModel) {
        mGameModel = gameModel;
    }

    public GameStateModel getGameModel() {
        return mGameModel;
    }
}
